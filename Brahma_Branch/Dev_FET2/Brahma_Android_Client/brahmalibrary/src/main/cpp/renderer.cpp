//
// Copyright 2011 Tero Saarni
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

#include <stdint.h>
#include <unistd.h>
#include <pthread.h>
#include <android/native_window.h> // requires ndk r5 or newer
#include <EGL/egl.h> // requires ndk r5 or newer
#include <GLES/gl.h>
#include <vector>

#include "OpenGLESDispatch/GLESv2Dispatch.h"
#include "OpenGLESDispatch/EGLDispatch.h"
#include "emugl/common/OpenGLDispatchLoader.h"

#include "debug.h"
#include "renderer.h"

#define LOG_TAG "EglSample"

static const EGLDispatch* mEGLDispatcher = emugl::LazyLoadedEGLDispatch::get();
static const GLESv1Dispatch* mGLESv1Dispatcher = emugl::LazyLoadedGLESv1Dispatch::get();
static const GLESv2Dispatch* mGLESv2Dispatcher = emugl::LazyLoadedGLESv2Dispatch::get();

static GLint vertices[][3] = {
        { -0x10000, -0x10000, -0x10000 },
        {  0x10000, -0x10000, -0x10000 },
        {  0x10000,  0x10000, -0x10000 },
        { -0x10000,  0x10000, -0x10000 },
        { -0x10000, -0x10000,  0x10000 },
        {  0x10000, -0x10000,  0x10000 },
        {  0x10000,  0x10000,  0x10000 },
        { -0x10000,  0x10000,  0x10000 }
};

static GLint colors[][4] = {
        { 0x00000, 0x00000, 0x00000, 0x10000 },
        { 0x10000, 0x00000, 0x00000, 0x10000 },
        { 0x10000, 0x10000, 0x00000, 0x10000 },
        { 0x00000, 0x10000, 0x00000, 0x10000 },
        { 0x00000, 0x00000, 0x10000, 0x10000 },
        { 0x10000, 0x00000, 0x10000, 0x10000 },
        { 0x10000, 0x10000, 0x10000, 0x10000 },
        { 0x00000, 0x10000, 0x10000, 0x10000 }
};

GLubyte indices[] = {
        0, 4, 5,    0, 5, 1,
        1, 5, 6,    1, 6, 2,
        2, 6, 7,    2, 7, 3,
        3, 7, 4,    3, 4, 0,
        4, 7, 6,    4, 6, 5,
        3, 0, 1,    3, 1, 2
};


Renderer::Renderer()
        : _msg(MSG_NONE), _display(0), _surface(0), _context(0), _angle(0)
{
    LOG_INFO(LOG_TAG, "Renderer instance created");
    pthread_mutex_init(&_mutex, 0);

    return;
}

Renderer::~Renderer()
{
    LOG_INFO(LOG_TAG, "Renderer instance destroyed");
    pthread_mutex_destroy(&_mutex);
    return;
}

void Renderer::start()
{
    LOG_INFO(LOG_TAG, "Creating renderer thread");
    pthread_create(&_threadId, 0, threadStartCallback, this);
    return;
}

void Renderer::stop()
{
    LOG_INFO(LOG_TAG, "Stopping renderer thread");

    // send message to render thread to stop rendering
    pthread_mutex_lock(&_mutex);
    _msg = MSG_RENDER_LOOP_EXIT;
    pthread_mutex_unlock(&_mutex);

    pthread_join(_threadId, 0);
    LOG_INFO(LOG_TAG, "Renderer thread stopped");

    return;
}

void Renderer::setWindow(ANativeWindow *window)
{
    LOG_INFO(LOG_TAG, "Setting window: %p", window);

    // notify render thread that window has changed
    pthread_mutex_lock(&_mutex);
    _msg = MSG_WINDOW_SET;
    _window = window;
    pthread_mutex_unlock(&_mutex);

    return;
}



void Renderer::renderLoop()
{
    bool renderingEnabled = true;

    LOG_INFO(LOG_TAG, "renderLoop()");

    while (renderingEnabled) {

        pthread_mutex_lock(&_mutex);

        // process incoming messages
        switch (_msg) {

            case MSG_WINDOW_SET:
                initialize();
                //initialize_emugl();
                break;

            case MSG_RENDER_LOOP_EXIT:
                renderingEnabled = false;
                destroy();
                break;

            default:
                break;
        }
        _msg = MSG_NONE;

        LOG_INFO(LOG_TAG, "renderLoop again display==%p...", _display);

        if (_display) {
            drawFrame();
            if (!eglSwapBuffers(_display, _surface)) {
                LOG_ERR(LOG_TAG, "eglSwapBuffers() returned error %d", eglGetError());
            }
        }

        pthread_mutex_unlock(&_mutex);
    }

    LOG_INFO(LOG_TAG, "Render loop exits");

    return;
}

bool Renderer::initialize()
{
    const EGLint attribs[] = {
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_NONE
    };
    EGLDisplay display;
    EGLConfig config;
    EGLint numConfigs;
    EGLint format;
    EGLSurface surface;
    EGLContext context;
    EGLint width;
    EGLint height;
    GLfloat ratio;

    LOG_INFO(LOG_TAG, "Initializing context");

    if ((display = mEGLDispatcher->eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
        LOG_ERR(LOG_TAG, "eglGetDisplay() returned error %d", eglGetError());
        return false;
    } else    {
        LOG_INFO(LOG_TAG, "eglGetDisplay() == %p", display);
    }

    if (!mEGLDispatcher->eglInitialize(display, 0, 0)) {
        LOG_ERR(LOG_TAG, "eglInitialize() returned error %d", eglGetError());
        return false;
    }

    if (!mEGLDispatcher->eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
        LOG_ERR(LOG_TAG, "eglChooseConfig() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!mEGLDispatcher->eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
        LOG_ERR(LOG_TAG, "eglGetConfigAttrib() returned error %d", eglGetError());
        destroy();
        return false;
    }

    ANativeWindow_setBuffersGeometry(_window, 0, 0, format);

    if (!(surface = mEGLDispatcher->eglCreateWindowSurface(display, config, _window, 0))) {
        LOG_ERR(LOG_TAG, "eglCreateWindowSurface() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!(context = mEGLDispatcher->eglCreateContext(display, config, 0, 0))) {
        LOG_ERR(LOG_TAG, "eglCreateContext() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!mEGLDispatcher->eglMakeCurrent(display, surface, surface, context)) {
        LOG_ERR(LOG_TAG, "eglMakeCurrent() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!mEGLDispatcher->eglQuerySurface(display, surface, EGL_WIDTH, &width) ||
        !mEGLDispatcher->eglQuerySurface(display, surface, EGL_HEIGHT, &height)) {
        LOG_ERR(LOG_TAG, "eglQuerySurface() returned error %d", eglGetError());
        destroy();
        return false;
    }

    _display = display;
    _surface = surface;
    _context = context;

    mGLESv1Dispatcher->glDisable(GL_DITHER);
    mGLESv1Dispatcher->glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
    mGLESv1Dispatcher->glClearColor(0, 0, 0, 0);
    mGLESv1Dispatcher->glEnable(GL_CULL_FACE);
    mGLESv1Dispatcher->glShadeModel(GL_SMOOTH);
    mGLESv1Dispatcher->glEnable(GL_DEPTH_TEST);

    mGLESv1Dispatcher->glViewport(0, 0, width, height);

    ratio = (GLfloat) width / height;
    mGLESv1Dispatcher->glMatrixMode(GL_PROJECTION);
    mGLESv1Dispatcher->glLoadIdentity();
    //mGLESv1Dispatcher->glFrustumf(-ratio, ratio, -1, 1, 1, 10);

    return true;
}

bool Renderer::initialize_emugl()
{
    EGLDisplay display;
    EGLConfig config;
    EGLint numConfigs;
    EGLint format;
    EGLSurface surface;
    EGLContext context;
    EGLint eglMajor;
    EGLint eglMinor;
    EGLint width;
    EGLint height;
    GLfloat ratio;

    LOG_INFO(LOG_TAG, "Initializing context");

    if ((display = mEGLDispatcher->eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
        LOG_ERR(LOG_TAG, "eglGetDisplay() returned error %d", eglGetError());
        return false;
    } else    {
        LOG_INFO(LOG_TAG, "eglGetDisplay() == %p", display);
    }

    if (!mEGLDispatcher->eglInitialize(display, &eglMajor, &eglMinor)) {
        LOG_ERR(LOG_TAG, "eglInitialize() returned error %d", eglGetError());
        return false;
    }

    LOG_INFO(LOG_TAG, "EGL Version : %d %d\n", eglMajor, eglMinor);

    GLint surfaceType = EGL_WINDOW_BIT | EGL_PBUFFER_BIT;
    EGLint wantedRedSize = 8;
    EGLint wantedGreenSize = 8;
    EGLint wantedBlueSize = 8;

    const GLint configAttribs[] = {
            EGL_RED_SIZE,        wantedRedSize,
            EGL_GREEN_SIZE,      wantedGreenSize,
            EGL_BLUE_SIZE,       wantedBlueSize,
            EGL_SURFACE_TYPE,    surfaceType,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_NONE};

    EGLint total_num_configs = 0;
    mEGLDispatcher->eglGetConfigs(display, NULL, 0, &total_num_configs);

    std::vector<EGLConfig> all_configs(total_num_configs);
    EGLint total_egl_compatible_configs = 0;
    mEGLDispatcher->eglChooseConfig(display, configAttribs, &all_configs[0],
                    total_num_configs, &total_egl_compatible_configs);

    EGLint exact_match_index = -1;
    for (EGLint i = 0; i < total_egl_compatible_configs; i++) {
        EGLint r, g, b;
        EGLConfig c = all_configs[i];
        mEGLDispatcher->eglGetConfigAttrib(display, c, EGL_RED_SIZE, &r);
        mEGLDispatcher->eglGetConfigAttrib(display, c, EGL_GREEN_SIZE, &g);
        mEGLDispatcher->eglGetConfigAttrib(display, c, EGL_BLUE_SIZE, &b);

        if (r == wantedRedSize && g == wantedGreenSize && b == wantedBlueSize) {
            exact_match_index = i;
            break;
        }
    }

    if (exact_match_index < 0) {
        LOG_ERR(LOG_TAG, "Failed on eglChooseConfig");
        destroy();
        return false;
    }

    config = all_configs[exact_match_index];

    if (!mEGLDispatcher->eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
        LOG_ERR(LOG_TAG, "eglGetConfigAttrib() returned error %d", eglGetError());
        destroy();
        return false;
    }

    ANativeWindow_setBuffersGeometry(_window, 0, 0, format);

    if (!(surface = mEGLDispatcher->eglCreateWindowSurface(display, config, _window, 0))) {
        LOG_ERR(LOG_TAG, "eglCreateWindowSurface() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!(context = mEGLDispatcher->eglCreateContext(display, config, 0, 0))) {
        LOG_ERR(LOG_TAG, "eglCreateContext() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!mEGLDispatcher->eglMakeCurrent(display, surface, surface, context)) {
        LOG_ERR(LOG_TAG, "eglMakeCurrent() returned error %d", eglGetError());
        destroy();
        return false;
    }

    if (!mEGLDispatcher->eglQuerySurface(display, surface, EGL_WIDTH, &width) ||
        !mEGLDispatcher->eglQuerySurface(display, surface, EGL_HEIGHT, &height)) {
        LOG_ERR(LOG_TAG, "eglQuerySurface() returned error %d", eglGetError());
        destroy();
        return false;
    }

    _display = display;
    _surface = surface;
    _context = context;

    mGLESv1Dispatcher->glDisable(GL_DITHER);
    mGLESv1Dispatcher->glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
    mGLESv1Dispatcher->glClearColor(0, 0, 0, 0);
    mGLESv1Dispatcher->glEnable(GL_CULL_FACE);
    mGLESv1Dispatcher->glShadeModel(GL_SMOOTH);
    mGLESv1Dispatcher->glEnable(GL_DEPTH_TEST);

    mGLESv1Dispatcher->glViewport(0, 0, width, height);

    ratio = (GLfloat) width / height;
    mGLESv1Dispatcher->glMatrixMode(GL_PROJECTION);
    mGLESv1Dispatcher->glLoadIdentity();
    //mGLESv1Dispatcher->glFrustumf(-ratio, ratio, -1, 1, 1, 10);

    return true;
}

void Renderer::destroy() {
    LOG_INFO(LOG_TAG, "Destroying context");

    mEGLDispatcher->eglMakeCurrent(_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    mEGLDispatcher->eglDestroyContext(_display, _context);
    mEGLDispatcher->eglDestroySurface(_display, _surface);
    mEGLDispatcher->eglTerminate(_display);

    _display = EGL_NO_DISPLAY;
    _surface = EGL_NO_SURFACE;
    _context = EGL_NO_CONTEXT;

    return;
}

void Renderer::drawFrame()
{
    mGLESv1Dispatcher->glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    mGLESv1Dispatcher->glMatrixMode(GL_MODELVIEW);
    mGLESv1Dispatcher->glLoadIdentity();
    mGLESv1Dispatcher->glTranslatef(0, 0, -3.0f);
    mGLESv1Dispatcher->glRotatef(_angle, 0, 1, 0);
    mGLESv1Dispatcher->glRotatef(_angle*0.25f, 1, 0, 0);

    mGLESv1Dispatcher->glEnableClientState(GL_VERTEX_ARRAY);
    mGLESv1Dispatcher->glEnableClientState(GL_COLOR_ARRAY);

    mGLESv1Dispatcher->glFrontFace(GL_CW);
    mGLESv1Dispatcher->glVertexPointer(3, GL_FIXED, 0, vertices);
    mGLESv1Dispatcher->glColorPointer(4, GL_FIXED, 0, colors);
    mGLESv1Dispatcher->glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indices);

    _angle += 1.2f;
}

void* Renderer::threadStartCallback(void *myself)
{
    Renderer *renderer = (Renderer*)myself;

    renderer->renderLoop();
    pthread_exit(0);

    return 0;
}