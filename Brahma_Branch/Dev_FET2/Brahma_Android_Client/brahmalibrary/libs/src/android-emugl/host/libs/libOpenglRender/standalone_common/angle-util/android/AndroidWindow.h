/**
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
 *
 * AndroidWindow.h: Definition of the implementation of OSWindow for Android
 */

#ifndef UTIL_ANDROID_WINDOW_H
#define UTIL_ANDROID_WINDOW_H

#include <string>

#include "OSWindow.h"


class AndroidWindow : public OSWindow
{
  public:
    AndroidWindow();
    AndroidWindow(int visualId);
    ~AndroidWindow();

    bool initialize(const std::string &name, size_t width, size_t height) override;
    void destroy() override;

    EGLNativeWindowType getNativeWindow() const override;
    EGLNativeDisplayType getNativeDisplay() const override;
    void* getFramebufferNativeWindow() const override;

    void messageLoop() override;

    void setMousePosition(int x, int y) override;
    bool setPosition(int x, int y) override;
    bool resize(int width, int height) override;
    void setVisible(bool isVisible) override;

    void signalTestEvent() override;

  private:
    EGLNativeDisplayType *mDisplay;
    EGLNativeWindowType mWindow;
    int mRequestedVisualId;
    bool mVisible;
};

#endif // UTIL_ANDROID_WINDOW_H
