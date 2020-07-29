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

#pragma once

#include "OpenglRender/render_api.h"
#include "android/base/synchronization/Lock.h"
#include "android/base/synchronization/ConditionVariable.h"
#include "android/base/threads/Thread.h"
#include "OpenglRender/RenderChannel.h"
#include "android/base/threads/FunctorThread.h"
#include "android/base/containers/BufferQueue.h"

class OpenglEsStreamHandler : public android::base::Thread   {
public:
    OpenglEsStreamHandler(int socket, emugl::RenderChannelPtr& renderChannel);

private:
    int count = 0;
    bool mIsWorking;
    int mSocketHandle;

    emugl::RenderChannelPtr mRenderChannel;

    android::base::Lock mReaderLock;
    android::base::Lock mWriterLock;
    android::base::ConditionVariable mReaderThreadCv;
    android::base::ConditionVariable mWriterThreadCv;
    android::base::FunctorThread mFGWorkerThread;
    android::base::FunctorThread mTGWorkerThread;
    android::base::FunctorThread mRenderChannelReaderThread;

    android::base::Lock mFromGuestQueueLock;
    android::base::Lock mToGuestQueueLock;
    android::base::BufferQueue<emugl::RenderChannel::Buffer> mFromGuest;
    android::base::BufferQueue<emugl::RenderChannel::Buffer> mToGuest;

    void postInitizlize();
    void onChannelHostEvent(emugl::RenderChannel::State state);
    void signalState(emugl::RenderChannel::State state);
    void guestDataWriter();
    void renderChannelWriter();
    void renderChannelReader();

    virtual intptr_t main() override final;
    virtual void onExit() override final;
};