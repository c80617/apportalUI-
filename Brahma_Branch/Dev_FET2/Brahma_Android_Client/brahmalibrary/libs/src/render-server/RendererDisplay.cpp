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

#include "RendererDisplay.h"

RendererDisplay::RendererDisplay(bool useWindow, int width, int height)    {
    if (useWindow) {
        window = CreateOSWindow();
        window->initialize("Renderer Server Display", width, height);
    }
}

RendererDisplay::~RendererDisplay()    {
    if(window)    window->destroy();
}

float RendererDisplay::getDevicePixelRatio() {
    if(window)    return window->getDevicePixelRatio();

    return 1.0f;
}

void* RendererDisplay::getNativeWindow() {
    if(window)    return window->getFramebufferNativeWindow();
    
    return nullptr;
}

void RendererDisplay::loop() {
    if(window)    window->messageLoop();
}

void RendererDisplay::resizeWithRect(int xoffset, int yoffset, int width, int height) {
    if(!window) return;

    window->setPosition(xoffset, yoffset);
    window->resize(width, height);
    window->setVisible(true);
    window->messageLoop();
}
