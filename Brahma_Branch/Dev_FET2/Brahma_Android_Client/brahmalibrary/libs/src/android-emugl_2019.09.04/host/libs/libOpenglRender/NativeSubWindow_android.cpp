/*
* Copyright (C) 2011 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
#include <android/native_window_jni.h>

#include "NativeSubWindow.h"
#include "OpenglCodecCommon/ErrorLog.h"

#include <stdio.h>

// gAndroidNativeDisplayWindow should be configure on Android native side 
static EGLNativeWindowType gAndroidNativeDisplayWindow = 0; 

// this is for Android to set root ANativeWindow for EMUGL
void setSubWindow(EGLNativeWindowType sub_window)    {
    DBG("NativeSubWindow_android:: setSubWindow: %p", sub_window);
    gAndroidNativeDisplayWindow = sub_window;
}

EGLNativeWindowType createSubWindow(FBNativeWindowType p_window,
                                    int x,
                                    int y,
                                    int width,
                                    int height,
                                    SubWindowRepaintCallback repaint_callback,
                                    void* repaint_callback_param) {
    // TODO: handle re-paint callback if Host window system needs to repaint this one

    DBG("NativeSubWindow_android::createSubWindow: %p", gAndroidNativeDisplayWindow);

    if(!gAndroidNativeDisplayWindow)    {
        ERR("The Android App root ANativeWindow has not been configured yet...");
        return 0;
    }

    /* EGL_NATIVE_VISUAL_ID is an attribute of the EGLConfig that is
     * guaranteed to be accepted by ANativeWindow_setBuffersGeometry().
     * As soon as we picked a EGLConfig, we can safely reconfigure the
     * ANativeWindow buffers to match, using EGL_NATIVE_VISUAL_ID. */
    ANativeWindow_setBuffersGeometry(gAndroidNativeDisplayWindow, 0, 0, p_window);

    return gAndroidNativeDisplayWindow;
}

void destroySubWindow(EGLNativeWindowType win) {
}

int moveSubWindow(FBNativeWindowType p_parent_window,
                  EGLNativeWindowType p_sub_window,
                  int x,
                  int y,
                  int width,
                  int height) {
    DBG("NativeSubWindow_android::moveSubWindow: (%d, %d) %dx%d", x, y, width, height);
    return true;
}
