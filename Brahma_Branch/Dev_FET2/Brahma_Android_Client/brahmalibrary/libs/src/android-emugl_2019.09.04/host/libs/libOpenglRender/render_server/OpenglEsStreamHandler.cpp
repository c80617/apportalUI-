// Copyright (C) 2018 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <memory>

#include "debug.h"
#include "OpenglEsStreamHandler.h"
#include "android/utils/sockets.h"
#include "android/base/sockets/SocketUtils.h"

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
    DDDLN("To guest data writer thread has started...");
    emugl::RenderChannel::Buffer buffer;
    for(;;)    {
        {
            android::base::AutoLock lock(mToGuestQueueLock);
            auto popResult = mToGuest.popLocked(&buffer);
            if(android::base::BufferQueueResult::Ok != popResult)    {
                DDD("%d %s: mToGuest.popLocked() failed, returned %d...\n",
                    mSocketHandle, __func__, (int)popResult);
                break;
            }
        }

        int length = buffer.size();
        auto ptr = buffer.data();
        if(!android::base::socketSendAll(mSocketHandle, ptr, length))    {
            DDD("%d Error ending data to guest: %s\n", mSocketHandle, strerror(errno));
            break;
        }

        DDD("%d Sent %d bytes to guest...\n", mSocketHandle, length);
    }
}

void OpenglEsStreamHandler::renderChannelWriter()    {
    DDDLN("To host data writer thread has started...");
    emugl::RenderChannel::Buffer buffer;
    for(;;)    {
        {
            android::base::AutoLock lock(mFromGuestQueueLock);
            auto popResult = mFromGuest.popLocked(&buffer);
            if(android::base::BufferQueueResult::Ok != popResult)    {
                DDD("%d %s: mFromGuest.popLocked() failed, returned %d...\n",
                       mSocketHandle, __func__, (int)popResult);
                break;
            }
            
            // for debug purpose...
            if(--count < 0)
                printf("%d %s: count = %d...\n", mSocketHandle, __func__, count);
            
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
            DDD("%d %s tryWrite failed with %d\n", mSocketHandle, __func__, (int)result);
            break;
        }
            
        DDD("%d %s: wrote %zu bytes to render channel...\n",
            mSocketHandle, __func__, buffer.size());
    }
}

void OpenglEsStreamHandler::renderChannelReader()    {
    DDDLN("Render Channel Reader thread has started...");
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
            DDD("%d %s: error reading data fron render channel, returned %d...\n",
                mSocketHandle, __func__, (int)result);
            break;
        }

        {
            android::base::AutoLock lock(mToGuestQueueLock);
            android::base::BufferQueueResult pushResult = mToGuest.pushLocked(std::move(buffer));
            if(android::base::BufferQueueResult::Ok != pushResult)    {
                printf("%d %s mToGuest.pushLocked() failed, returned %d...\n",
                       mSocketHandle, __func__, (int)pushResult);
                break;
            }
        }
    }
}

// Called when an i/o event occurs on the render channel
void OpenglEsStreamHandler::onChannelHostEvent(emugl::RenderChannel::State state) {
    DDD("%s: events %d (working %d)", __func__, (int)state, (int)mIsWorking);
    // NOTE: This is called from the host-side render thread.
    // but closeFromHost() and signalWake() can be called from
    // any thread.
    if (!mIsWorking) {
        return;
    }

    if ((state & emugl::RenderChannel::State::Stopped) != 0) {
        DDD("%d %s: State::Stopped...\n", mSocketHandle, __func__);
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
        DDD("%s: State::CanRead...", __func__);
        android::base::AutoLock lock(mReaderLock);
        mReaderThreadCv.signal();
    }
 
    if((state & emugl::RenderChannel::State::CanWrite) != 0) {
        DDD("%s: State::CanWrite...", __func__);
        android::base::AutoLock lock(mWriterLock);
        mWriterThreadCv.signal();
    }
}

void OpenglEsStreamHandler::postInitizlize()    {
    mRenderChannel->setEventCallback([this](emugl::RenderChannel::State events)    {
        onChannelHostEvent(events);
    });

    mFGWorkerThread.start();
    mTGWorkerThread.start();
    mRenderChannelReaderThread.start();

/*
    mRenderChannel->setWantedEvents(emugl::RenderChannel::State::CanRead |
                                    emugl::RenderChannel::State::CanWrite |
                                    emugl::RenderChannel::State::Stopped);
*/   
/*
    unsigned int clientFlags;
    if(!android::base::socketRecvAll(mSocketHandle, &clientFlags, sizeof(unsigned int))) {
        fprintf(stderr,"Error reading clientFlags\n");
        exit(-1);
    }

    DDD("Received client flags = 0X%08X...", clientFlags);

    // check if we have been requested to exit while waiting on accept
    // IOSTREAM_CLIENT_EXIT_SERVER
    if((clientFlags & 0x01) != 0)    {
        exit(-1);
    }
*/
}

intptr_t OpenglEsStreamHandler::main()    {
    DDD("Starting OpenglEsStreamHandler: %d...", mSocketHandle);

    printf("%s: Start listening %d\n", __func__, mSocketHandle);
    emugl::RenderChannel::Buffer outBuffer;
    for(;;)    {
        outBuffer.resize_noinit(512);
        auto ptr = outBuffer.data();
        ssize_t ret = android::base::socketRecv(mSocketHandle, ptr, outBuffer.size());
        if(0 >= (int)ret)    {
            DDD("Error receiving socket(%d) data length = %zd: %s\n",
                mSocketHandle, ret, strerror(errno));
            break;
        }

        outBuffer.resize_noinit(ret);

        DDD("%d received %zd byte data from guest, outBuffer.size() = %zu\n",
            mSocketHandle, ret, outBuffer.size());
            //for(int j = 0; j < outBuffer.size(); j++)    {
            //    if(j % 4 == 0)    DDD("\n    ");
            //    DDD("0x%02X  ", ((unsigned char *)ptr)[j]);
            //}
            //DDD("\n\n");
                
        {
            android::base::AutoLock lock(mFromGuestQueueLock);
            if(++count < 1)    printf("%d %s: count = %d...\n", mSocketHandle, __func__, count);
            android::base::BufferQueueResult pushResult = mFromGuest.pushLocked(std::move(outBuffer));
            if(android::base::BufferQueueResult::Ok != pushResult)    {
                DDD("%d: mFromGuest.pushLocked() failed, returned %d...\n",
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

    DDD("Existing OpenglEsStreamHandler thread %d...\n", mSocketHandle);
}

