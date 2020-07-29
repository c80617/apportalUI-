/*
 * Copyright (C) 2015-2017 ICL/ITRI
 * All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ICL/ITRI and its suppliers, if any.
 * The intellectual and technical concepts contained
 * herein are proprietary to ICL/ITRI and its suppliers and
 * may be covered by Taiwan and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from ICL/ITRI.
 */

#include <memory>

#define DEBUG_LEVEL    0
#include "debug.h"

#include "OpenglEsStreamHandler.h"
#include "android/utils/sockets.h"
#include "android/base/sockets/SocketUtils.h"

#define TAG "OpenglEsStreamHandler"

static constexpr size_t kGuestToHostQueueCapacity = 1024U;
static constexpr size_t kHostToGuestQueueCapacity = 64U;

OpenglEsStreamHandler::OpenglEsStreamHandler(int socket, emugl::RenderChannelPtr& renderChannel):
        android::base::Thread(android::base::ThreadFlags::MaskSignals, 128 * 1024 * 1024),
        mSocketHandle(socket),
        mIsWorking(true),
        mRenderChannel(renderChannel),
        mTGWorkerThread([this]() { guestDataWriter(); }),
        mFGWorkerThread([this]() { renderChannelWriter(); }),
        mRenderChannelReaderThread([this]() { renderChannelReader(); }),
        mFromGuest(kGuestToHostQueueCapacity, mFromGuestQueueLock),
        mToGuest(kHostToGuestQueueCapacity, mToGuestQueueLock)    {
    postInitizlize();
}

void OpenglEsStreamHandler::guestDataWriter()    {
    LOG_INFO(TAG, "To guest data writer thread has started...");
    emugl::RenderChannel::Buffer buffer;
    for(;;)    {
        {
            android::base::AutoLock lock(mToGuestQueueLock);
            auto popResult = mToGuest.popLocked(&buffer);
            if(android::base::BufferQueueResult::Ok != popResult)    {
                LOG_DEBUG_S(TAG, "%d %s: mToGuest.popLocked() failed, returned %d...\n",
                            mSocketHandle, __func__, (int)popResult);
                break;
            }
        }

        size_t length = buffer.size();
        auto ptr = buffer.data();
        if(!android::base::socketSendAll(mSocketHandle, ptr, length))    {
            LOG_DEBUG_S(TAG, "%d Error ending data to guest: %s\n", mSocketHandle, strerror(errno));
            break;
        }

        DDEBUG(TAG, "%d Sent %zd bytes to guest...\n", mSocketHandle, length);
    }
}

void OpenglEsStreamHandler::renderChannelWriter()    {
    LOG_INFO(TAG, "To host data writer thread has started...");
    emugl::RenderChannel::Buffer buffer;
    for(;;)    {
        {
            android::base::AutoLock lock(mFromGuestQueueLock);
            auto popResult = mFromGuest.popLocked(&buffer);
            if(android::base::BufferQueueResult::Ok != popResult)    {
                LOG_DEBUG_S(TAG, "%d %s: mFromGuest.popLocked() failed, returned %d...\n",
                            mSocketHandle, __func__, (int)popResult);
                break;
            }

            // for debug purpose...
            if(--count < 0)
                LOG_DEBUG_S(TAG, "%d %s: count = %d...\n", mSocketHandle, __func__, count);

        }

        // Try to writes the data in |buffer| into the channel. On success,
        // return IoResult::Ok and moves |buffer|. On failure, return
        // IoResult::TryAgain if the channel was full, or IoResult::Error
        // if it is stopped.
        emugl::RenderChannel::IoResult result = emugl::RenderChannel::IoResult::Ok;
        while((result = mRenderChannel->tryWrite(std::move(buffer))) ==
              emugl::RenderChannel::IoResult::TryAgain)    {
            {
                android::base::AutoLock lock(mWriterLock);
                mRenderChannel->setWantedEvents(emugl::RenderChannel::State::CanWrite);
                mWriterThreadCv.wait(&mWriterLock);
            }
        }

        if(emugl::RenderChannel::IoResult::Error == result)    {
            LOG_DEBUG_S(TAG, "%d %s tryWrite failed with %d\n", mSocketHandle, __func__, (int)result);
            break;
        }

        DDEBUG(TAG, "%d %s: wrote %zu bytes to render channel...\n",
               mSocketHandle, __func__, buffer.size());
    }
}

void OpenglEsStreamHandler::renderChannelReader()    {
    LOG_INFO(TAG, "Render Channel Reader thread has started...");
    emugl::RenderChannel::Buffer buffer;
    for(;;)    {
        emugl::RenderChannel::IoResult result = emugl::RenderChannel::IoResult::Ok;
        while((result = mRenderChannel->tryRead(&buffer)) ==
              emugl::RenderChannel::IoResult::TryAgain)    {
            {
                android::base::AutoLock lock(mReaderLock);
                mRenderChannel->setWantedEvents(emugl::RenderChannel::State::CanRead);
                mReaderThreadCv.wait(&mReaderLock);
            }
        }

        if(result != emugl::RenderChannel::IoResult::Ok) {
            LOG_DEBUG_S(TAG, "%d %s: error reading data fron render channel, returned %d...\n",
                        mSocketHandle, __func__, (int)result);
            break;
        }

        DDEBUG(TAG, "Render Channel Reader has read %zu bytes...", buffer.size());

        {
            android::base::AutoLock lock(mToGuestQueueLock);
            android::base::BufferQueueResult pushResult = mToGuest.pushLocked(std::move(buffer));
            if(android::base::BufferQueueResult::Ok != pushResult)    {
                LOG_DEBUG_S(TAG, "%d %s mToGuest.pushLocked() failed, returned %d...\n",
                            mSocketHandle, __func__, (int)pushResult);
                break;
            }
        }
    }
}

// Called when an i/o event occurs on the render channel
void OpenglEsStreamHandler::onChannelHostEvent(emugl::RenderChannel::State state) {
    DDEBUG(TAG, "%s: events %d (working %d)", __func__, (int)state, (int)mIsWorking);
    // NOTE: This is called from the host-side render thread.
    // but closeFromHost() and signalWake() can be called from
    // any thread.
    if (!mIsWorking) {
        return;
    }

    if ((state & emugl::RenderChannel::State::Stopped) != 0) {
        DDEBUG(TAG, "%d %s: State::Stopped...\n", mSocketHandle, __func__);
        //this->closeFromHost();
        mIsWorking = false;

        return;
    }

    signalState(state);
}

// Called to signal the guest that read/write wake events occured.
// Note: this can be called from either the guest or host render
// thread.
void OpenglEsStreamHandler::signalState(emugl::RenderChannel::State state) {
    if((state & emugl::RenderChannel::State::CanRead) != 0) {
        DDEBUG(TAG, "%s: State::CanRead...", __func__);
        android::base::AutoLock lock(mReaderLock);
        mReaderThreadCv.signal();
    }

    if((state & emugl::RenderChannel::State::CanWrite) != 0) {
        DDEBUG(TAG, "%s: State::CanWrite...", __func__);
        android::base::AutoLock lock(mWriterLock);
        mWriterThreadCv.signal();
    }
}

void OpenglEsStreamHandler::postInitizlize()    {
    mRenderChannel->setEventCallback([this](emugl::RenderChannel::State events)    {
        onChannelHostEvent(events);
    });

    mRenderChannelReaderThread.start();
    mFGWorkerThread.start();
    mTGWorkerThread.start();

}

intptr_t OpenglEsStreamHandler::main()    {
    LOG_INFO(TAG, "Starting OpenglEsStreamHandler: %d...", mSocketHandle);
    LOG_INFO("%s: Start listening %d\n", __func__, mSocketHandle);
    emugl::RenderChannel::Buffer outBuffer;
    for(;;)    {
        outBuffer.resize_noinit(512);
        unsigned char *ptr = (unsigned char *)outBuffer.data();
        ssize_t ret = android::base::socketRecv(mSocketHandle, ptr, outBuffer.size());
        if(0 >= (int)ret)    {
            LOG_DEBUG(TAG, "Error receiving socket(%d) data length = %zd: %s\n",
                      mSocketHandle, ret, strerror(errno));
            break;
        }

        outBuffer.resize_noinit((unsigned int)ret);

        ptr = (unsigned char *)outBuffer.data();
        DDEBUG(TAG, "%d received %zd byte data from guest, opcode=%d, packetLen=%d, outBuffer.size() = %zu",
               mSocketHandle, ret, *(uint32_t *)ptr, *(int32_t *)(ptr + 4), outBuffer.size());

        {
            android::base::AutoLock lock(mFromGuestQueueLock);
            if(++count < 1)    LOG_DEBUG(TAG, "%d %s: count = %d...\n", mSocketHandle, __func__, count);
            android::base::BufferQueueResult pushResult = mFromGuest.pushLocked(std::move(outBuffer));
            if(android::base::BufferQueueResult::Ok != pushResult)    {
                LOG_DEBUG_S(TAG, "%d: mFromGuest.pushLocked() failed, returned %d...\n",
                            mSocketHandle, (int)pushResult);
                break;
            }
        }

        //mRenderChannel->setWantedEvents(emugl::RenderChannel::State::CanRead |
        //                                emugl::RenderChannel::State::CanWrite);
    }

    return (intptr_t)0;
}

void OpenglEsStreamHandler::onExit()    {
    android::base::socketClose(mSocketHandle);
    mRenderChannel->stop();

    mFromGuest.closeLocked();
    mToGuest.closeLocked();

    mReaderThreadCv.signal();
    mWriterThreadCv.signal();

    mFGWorkerThread.wait();
    mTGWorkerThread.wait();
    mRenderChannelReaderThread.wait();

    LOG_DEBUG_S(TAG, "Existing OpenglEsStreamHandler thread %d...\n", mSocketHandle);
}