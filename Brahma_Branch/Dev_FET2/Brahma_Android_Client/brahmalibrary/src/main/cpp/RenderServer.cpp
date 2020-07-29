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

#include <stdio.h>
#include <stdarg.h>

#include "FrameBuffer.h"
#include "OpenglRender/render_api.h"
#include "NativeSubWindow.h"
#include "android/base/GLObjectCounter.h"
#include "emugl/common/misc.h"
#include "android/utils/sockets.h"
#include "android/base/sockets/SocketUtils.h"
#include "android/base/threads/FunctorThread.h"

#include "debug.h"
#include "OpenglEsStreamHandler.h"

#define EMUGL_LOG_TAG "EMUGL"
#define EMUGL_CTX_LOG_TAG "EMUGL_CTX"
#define TAG "RENDER_SERVER"
#define SOCKET_SERVER_PORT 22468

static void _emugl_logger(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    LOG_VERBOSE(EMUGL_LOG_TAG, fmt, args);
    va_end(args);
}

static void _emugl_ctx_logger(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    LOG_VERBOSE(EMUGL_CTX_LOG_TAG, fmt, args);
    va_end(args);
}

static void _emugl_crash_reporter(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    LOG_ERR_ARGS(TAG, fmt, args);
    va_end(args);

    abort();
}

static android::base::FunctorThread *mWorkerThread;

static int sDisplayWidth = 0, sDisplayHeight = 0;
static int sXOffset = 0, sYOffset = 0, sWinWidth = 0, sWinHeight = 0;
static emugl::RenderLibPtr sRenderLib = nullptr;
static emugl::RendererPtr sRenderer = nullptr;
static emugl::RenderChannelPtr sRenderChannel = nullptr;
//static emugl_logger_struct sEmuglLogFuncs = { _emugl_logger, _emugl_ctx_logger };
//static emugl_logger_struct sEmuglLogFuncs = { _emugl_logger, nullptr };
static emugl_logger_struct sEmuglLogFuncs = {nullptr, nullptr};
static bool sIsFolded = false;

static const QAndroidEmulatorWindowAgent sQAndroidEmulatorWindowAgent = {
        .getEmulatorWindow = [](void) {
            LOG_INFO(TAG, "WindowAgent: .getEmulatorWindow");
            return (EmulatorWindow *) nullptr;
        },
        .rotate90Clockwise = [](void) {
            LOG_INFO(TAG, "WindowAgent:  .rotate90Clockwise");
            return true;
        },
        .rotate = [](SkinRotation rotation) {
            LOG_INFO(TAG, "WindowAgent: .rotate");
            return true;
        },
        .getRotation = [](void) {
            LOG_INFO(TAG, "WindowAgent: .getRotation");
            return SKIN_ROTATION_0;
        },
        .showMessage = [](const char *message, WindowMessageType type, int timeoutMs) {
            LOG_INFO(TAG, "WindowAgent: .showMessage %s", message);
        },
        .showMessageWithDismissCallback =
        [](const char *message, WindowMessageType type,
           const char *dismissText, void *context,
           void (*func)(void *), int timeoutMs) {
            LOG_INFO(TAG, "WindowAgent: .showMessageWithDismissCallback %s", message);
        },
        .fold = [](bool is_fold) {
            LOG_INFO(TAG, "WindowAgent: .fold %d\n", is_fold);
            sIsFolded = is_fold;
            return true;
        },
        .isFolded = [](void) -> bool {
            LOG_INFO(TAG, "WindowAgent: .isFolded ? %d\n", sIsFolded);
            return sIsFolded;
        },
        .setUIDisplayRegion = [](int x_offset, int y_offset, int w, int h) {
            LOG_INFO(TAG, "WindowAgent: .setUIDisplayRegion %d %d %dx%d\n",
                     x_offset, y_offset, w, h);
        },
        .setMultiDisplay = [](uint32_t id, int32_t x, int32_t y, uint32_t w, uint32_t h, bool add) {
            LOG_INFO(TAG, "WindowAgent: .setMultiDisplay id %d %d %d %dx%d %s\n",
                     id, x, y, w, h, add ? "add" : "del");
        },
        .getMonitorRect = [](uint32_t *w, uint32_t *h) {
            LOG_INFO(TAG, "WindowAgent: .getMonitorRect (%d X %d)", sDisplayWidth, sDisplayHeight);
            if (w) *w = sDisplayWidth;
            if (h) *h = sDisplayHeight;
            return true;
        },
        .setNoSkin = [](void) {
            LOG_INFO(TAG, "WindowAgent: .setNoSkin");
        },
        .switchMultiDisplay =
        [](bool add, uint32_t id, int32_t x, int32_t y, uint32_t w, uint32_t h,
           uint32_t dpi, uint32_t flag) {
            LOG_INFO(TAG, "WindowAgent: .switchMultiDisplay id %d %d %d %dx%d "
                          "dpi %d flag %d %s\n",
                     id, x, y, w, h, dpi, flag, add ? "add" : "del");
        }
};

//extern "C" const QAndroidEmulatorWindowAgent* const
//        gQAndroidEmulatorWindowAgent = &sQAndroidEmulatorWindowAgent;

static bool shouldUseHostGpu() {
    bool useHost =
            android::base::System::getEnvironmentVariable("ANDROID_EMU_TEST_WITH_HOST_GPU") == "1";

    LOG_INFO(TAG, "Using host gpu == %s", (useHost) ? "yes" : "no");

    // Also set the global emugl renderer accordingly.
    if (useHost) {
        emugl::setRenderer(SELECTED_RENDERER_HOST);
    } else {
        emugl::setRenderer(SELECTED_RENDERER_SWIFTSHADER_INDIRECT);
    }

    return useHost;
}

static int initRenderLib() {
    sRenderLib = initLibrary();
    if (!sRenderLib) {
        LOG_ERR(TAG, "OpenGLES RenderLib initialization failed!");
        return -1;
    }

    sRenderLib->setLogger(sEmuglLogFuncs);
    sRenderLib->setCrashReporter(_emugl_crash_reporter);
    sRenderLib->setGLObjectCounter(android::base::GLObjectCounter::get());
    sRenderLib->setWindowOps(sQAndroidEmulatorWindowAgent);

    // set android emulator feature control
    // for details, reference data/advancedFeatures.ini
    /* TODO: check why GLESDynamicVersion can NOT be turnned on */
//    android::featurecontrol::setEnabledOverride(
//            android::featurecontrol::GLESDynamicVersion, true);
    android::featurecontrol::setEnabledOverride(
            android::featurecontrol::GLDMA, false);
    android::featurecontrol::setEnabledOverride(
            android::featurecontrol::GLAsyncSwap, false);
    /* TODO: if RefCountPipe is NOT true, Framebuffer's color map (m_colorbuffers)
       will have problem */
    android::featurecontrol::setEnabledOverride(
            android::featurecontrol::RefCountPipe, true);
//    android::featurecontrol::setEnabledOverride(
//            android::featurecontrol::GLDirectMem, true);
//    android::featurecontrol::setEnabledOverride(
//            android::featurecontrol::Vulkan, true);
//    android::featurecontrol::setEnabledOverride(
//            android::featurecontrol::VulkanSnapshots, true);

    sRenderLib->setFeatureController(&android::featurecontrol::isEnabled);

    return 0;
}

int initialize_render_server(int displayWidth, int displayHeight) {
    LOG_INFO(TAG, "Initializing RanderLib with display(%dx%d)...",
             displayWidth, displayHeight);

    sDisplayWidth = displayWidth;
    sDisplayHeight = displayHeight;

    if (initRenderLib()) {
        LOG_ERR(TAG, "RanderLib initialization error...");
        return -1;
    }

    LOG_INFO(TAG, "RanderLib initialized...");

    return 0;
}

static void startListening() {
    //initialize the socket sub-system.
    android_socket_init();

    LOG_DEBUG_S(TAG, "Create server socket on port %d...", SOCKET_SERVER_PORT);
    //const int serverSocket = android::base::socketTcp4LoopbackServer(SOCKET_SERVER_PORT);
    const int serverSocket = socket_anyaddr_server(SOCKET_SERVER_PORT, SOCKET_STREAM);
    if (serverSocket == -1) {
        LOG_ERR(TAG, "Error create local socket server on port %d: %s",
                SOCKET_SERVER_PORT, strerror(errno));
        return;
    }

    for (;;) {
        LOG_DEBUG_S(TAG, "Listening guest connection on port %d...", SOCKET_SERVER_PORT);
        int socketHandle = android::base::socketAcceptAny(serverSocket);
        if (socketHandle == -1) {
            LOG_ERR(TAG, "Error accepting server socket...");
            return;
        }

        LOG_DEBUG_S(TAG, "New guest connection %d established on port %d...",
                    socketHandle, SOCKET_SERVER_PORT);

        sRenderChannel = sRenderer->createRenderChannel();

        OpenglEsStreamHandler *renderer = new OpenglEsStreamHandler(socketHandle, sRenderChannel);
        renderer->start();
    }
}

int start_render_server(FBNativeWindowType rootWindowId, EGLNativeWindowType subWindow,
                        int winWidth, int winHeight) {
    LOG_INFO(TAG, "start_render_server %d x %d", winWidth, winHeight);

    // Configure EMUGL core to use this Android ANativeWindow
    setSubWindow(subWindow);

    bool useHostGpu = shouldUseHostGpu();
    sWinWidth = winWidth;
    sWinHeight = winHeight;

    // useSubWindow == true, egl2egl == false means using host GPU
    // BUT..., as for Android platform, we use egl2egl==true directly....
    //     Prevent from using host Angle engine, Android does NOT have it...
    //sRenderer = sRenderLib->initRenderer(sWinWidth, sWinHeight, true, !useHostGpu);
    sRenderer = sRenderLib->initRenderer(sWinWidth, sWinHeight, true, true);

    bool result = sRenderer->showOpenGLSubwindow(rootWindowId,
                                                 0, 0,
                                                 sWinWidth, sWinHeight, sWinWidth, sWinHeight,
                                                 1.0f,
                                                 0.0f,
                                                 false /* deleteExisting */);

    if (result) {
        LOG_DEBUG_S(TAG, "Successfully showOpenGLSubwindow in Renderer...");
    } else {
        LOG_ERR(TAG, "Error showOpenGLSubwindow in Renderer...");
        return -1;
    }

    mWorkerThread = new android::base::FunctorThread([&]() { startListening(); });
    mWorkerThread->start();

    //FrameBuffer::getFB()->startStingg();

    return 0;
}

int adjust_render_server(int winWidth, int winHeight) {
    return 0;
}