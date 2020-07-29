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
    int report = 0;
    int count = 0;
    bool mIsWorking;
    int mSocketHandle;
    
    emugl::RenderChannelPtr mRenderChannel;

    android::base::Lock mLock;
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
    int readFromChannel();
    void guestDataWriter();
    void renderChannelWriter();
    void renderChannelReader();

    virtual intptr_t main() override final;
    virtual void onExit() override final;
};
