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
 * AndroidWindow.cpp: Implementation of OSWindow for Android
 */

#include "android/AndroidWindow.h"

#include "android/base/system/System.cpp"

#include <assert.h>

using android::base::System;

AndroidWindow::AndroidWindow() :
      mDisplay(nullptr),
      mWindow(0),
      mRequestedVisualId(-1),
      mVisible(false)    {
}

AndroidWindow::AndroidWindow(int visualId) : 
      mDisplay(nullptr),
      mWindow(0),
      mRequestedVisualId(visualId),
      mVisible(false)    {
}

AndroidWindow::~AndroidWindow()    {
    destroy();
}

bool AndroidWindow::initialize(const std::string &name, size_t width, size_t height)    {
    destroy();

    mX = 0;
    mY = 0;
    mWidth = width;
    mHeight = height;

    return true;
}

void AndroidWindow::destroy()    {
}

EGLNativeWindowType AndroidWindow::getNativeWindow() const
{
    return mWindow;
}

EGLNativeDisplayType AndroidWindow::getNativeDisplay() const
{
    return mDisplay;
}

void* AndroidWindow::getFramebufferNativeWindow() const
{
    return nullptr;
}

void AndroidWindow::messageLoop()    {
}

void AndroidWindow::setMousePosition(int x, int y)    {
}

OSWindow *CreateOSWindow()    {
    return new AndroidWindow();
}

bool AndroidWindow::setPosition(int x, int y)    {
    return true;
}

bool AndroidWindow::resize(int width, int height)    {
    return true;
}

void AndroidWindow::setVisible(bool isVisible)    {
    mVisible = isVisible;
}

void AndroidWindow::signalTestEvent()    {
}
