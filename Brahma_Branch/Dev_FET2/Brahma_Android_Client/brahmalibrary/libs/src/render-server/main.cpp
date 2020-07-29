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

#include <stdio.h>
#include <stdarg.h>

#include "debug.h"
#include "OpenglEsStreamHandler.h"
#include "RendererDisplay.h"
#include "Vsync.h"

#include "FrameBuffer.h"
#include "OpenglRender/render_api.h"
//#include "android/base/GLObjectCounter.h"
#include "android/base/system/System.h"
#include "emugl/common/misc.h"
#include "android/utils/sockets.h"
#include "android/base/sockets/SocketUtils.h"
#include "emugl/common/logging.h"

#define SOCKET_SERVER_PORT 22468

static void _emugl_logger(const char* fmt, ...)    {
    va_list args;
    va_start(args, fmt);
    vfprintf(stderr, fmt, args);
    va_end(args);
    fprintf(stderr, "\n");
}

static void _emugl_ctx_logger(const char* fmt, ...)    {
    va_list args;
    va_start(args, fmt);
    vfprintf(stderr, fmt, args);
    va_end(args);
}


static RendererDisplay sRendererDisplay(true, 720, 1280);
FBNativeWindowType sWindowId = 0;
static int xOffset = 10, yOffset = 0, winWidth = 720, winHeight = 1280;
static emugl::RenderLibPtr sRenderLib = nullptr;
static emugl::RendererPtr sRenderer = nullptr;
static emugl::RenderChannelPtr sRenderChannel = nullptr;
//static emugl_logger_struct sEmuglLogFuncs = { _emugl_logger, _emugl_ctx_logger };
//static emugl_logger_struct sEmuglLogFuncs = { _emugl_logger, nullptr };
static emugl_logger_struct sEmuglLogFuncs = { nullptr, nullptr };

static bool getMonitorRect(uint32_t* w, uint32_t* h) {
    if(w)    *w = winWidth;
    if(h)    *h = winHeight;

    return true;
}

static void setMultiDisplay(uint32_t id, uint32_t x, uint32_t y,
                             uint32_t w, uint32_t h, bool add)    {
    //Do nothing here...   
}

/*
static const QAndroidEmulatorWindowAgent sQAndroidEmulatorWindowAgent = { 
        .getEmulatorWindow = nullptr,
        .rotate90Clockwise = nullptr,
        .rotate = nullptr,
        .getRotation = nullptr,
        .showMessage = nullptr,
        .showMessageWithDismissCallback = nullptr,
        .fold = nullptr,
        .isFolded = nullptr,
        .setUIDisplayRegion = nullptr,
        .setMultiDisplay = setMultiDisplay, 
        .getMultiDisplay = nullptr,
        .getMonitorRect = getMonitorRect,
        .setNoSkin = nullptr,
};
*/

static bool sIsFolded = false;

static const QAndroidEmulatorWindowAgent sQAndroidEmulatorWindowAgent = {
        .getEmulatorWindow =
                [](void) {
                    printf("OpenglEsStreamHandler: .getEmulatorWindow\n");
                    return (EmulatorWindow*)nullptr;
                },
        .rotate90Clockwise =
                [](void) {
                    printf("OpenglEsStreamHandler: .rotate90Clockwise\n");
                    return true;
                },
        .rotate =
                [](SkinRotation rotation) {
                    printf("OpenglEsStreamHandler: .rotate90Clockwise\n");
                    return true;
                },
        .getRotation =
                [](void) {
                    printf("OpenglEsStreamHandler: .getRotation\n");
                    return SKIN_ROTATION_0;
                },
        .showMessage =
                [](const char* message, WindowMessageType type, int timeoutMs) {
                    printf("OpenglEsStreamHandler: .showMessage %s\n", message);
                },
        .showMessageWithDismissCallback =
                [](const char* message, WindowMessageType type,
                   const char* dismissText, void* context,
                   void (*func)(void*), int timeoutMs) {
                    printf("OpenglEsStreamHandler: .showMessageWithDismissCallback %s\n", message);
                },
};


extern "C" const QAndroidEmulatorWindowAgent* const
        gQAndroidEmulatorWindowAgent = &sQAndroidEmulatorWindowAgent;

static void printUsage(const char *progName)    {
    fprintf(stderr, "Usage: %s -windowid <windowid> [options]\n", progName);
    fprintf(stderr, "    -windowid <windowid>   - decimal window id to render into\n");
    fprintf(stderr, "    -x <num>               - render window x position\n");
    fprintf(stderr, "    -y <num>               - render window y position\n");
    fprintf(stderr, "    -width <num>           - render window width\n");
    fprintf(stderr, "    -height <num>          - render window height\n");
    exit(-1);
}

static void parseInput(int argc, char** argv)    {
    int windowId = 0;

    //
    // Parse command line arguments
    //
    for (int i = 1; i < argc; i++) {
        if (!strcmp(argv[i], "-windowid")) {
            if (++i >= argc || sscanf(argv[i],"%d", &windowId) != 1) {
                printUsage(argv[0]);
            }
        } else if (!strncmp(argv[i], "-x", 2)) {
            if (++i >= argc || sscanf(argv[i],"%d", &xOffset) != 1) {
                printUsage(argv[0]);
            }
        } else if (!strncmp(argv[i], "-y", 2)) {
            if (++i >= argc || sscanf(argv[i],"%d", &yOffset) != 1) {
                printUsage(argv[0]);
            }
        } else if (!strncmp(argv[i], "-width", 6)) {
            if (++i >= argc || sscanf(argv[i],"%d", &winWidth) != 1) {
                printUsage(argv[0]);
            }
        } else if (!strncmp(argv[i], "-height", 7)) {
            if (++i >= argc || sscanf(argv[i],"%d", &winHeight) != 1) {
                printUsage(argv[0]);
            }
        }
    }

    sWindowId = (FBNativeWindowType)windowId;
    //if (!sWindowId) {
    //    // window id must be provided in decimal
    //    printUsage(argv[0]);
    //}
}

static bool shouldUseHostGpu() {
    //bool useHost = android::base::System::getEnvironmentVariable("ANDROID_EMU_TEST_WITH_HOST_GPU") == "1";
   bool useHost = true;

    // Also set the global emugl renderer accordingly.
    if (useHost) {
        emugl::setRenderer(SELECTED_RENDERER_HOST);
    } else {
        emugl::setRenderer(SELECTED_RENDERER_SWIFTSHADER_INDIRECT);
    }

    return useHost;
}

static void initialize()    {
    // some OpenGL implementations may call X functions
    // it is safer to synchronize all X calls made by all the
    // rendering threads. (although the calls we do are locked
    // in the FrameBuffer singleton object).
    //XInitThreads();
}

static void initRenderLib()    {
    sRenderLib = initLibrary();
    if (!sRenderLib) {
        printf("%s: OpenGLES initialization failed!", __func__);
        exit(-1);
    }

    sRenderLib->setLogger(sEmuglLogFuncs);
    //sRenderLib->setGLObjectCounter(android::base::GLObjectCounter::get());
    //sRenderLib->setWindowOps(*gQAndroidEmulatorWindowAgent);

    sRenderLib->setFeatureController(&android::featurecontrol::isEnabled);

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
}

static void vsyncCallback()    {
    sRenderer->repaintOpenGLDisplay();
}

int main(int argc, char** argv) {
    parseInput(argc, argv);

    initialize();
    initRenderLib();

    bool useHostGpu = shouldUseHostGpu();
    // useSubWindow == true, egl2egl == false means using host GPU
    sRenderer = sRenderLib->initRenderer(winWidth, winHeight, true, !useHostGpu);

    bool result = true;
    if(sWindowId)    {
        result = sRenderer->showOpenGLSubwindow(sWindowId,
                                                0, 0,
                                                winWidth, winHeight, winWidth, winHeight,
                                                1.0f, 0.0f, false);
    } else    {
        sRendererDisplay.resizeWithRect(xOffset, yOffset, winWidth, winHeight);
        result = sRenderer->showOpenGLSubwindow((FBNativeWindowType)(uintptr_t)
                                                sRendererDisplay.getNativeWindow(),
                                                0, 0,
                                                winWidth, winHeight, winWidth, winHeight,
                                                sRendererDisplay.getDevicePixelRatio(),
                                                0.0f,
                                                false);
    }
    if(result)    {
        printf("\nSuccessfully showOpenGLSubwindow in Renderer...\n");
    } else    {
        printf("\nError showOpenGLSubwindow in Renderer...\n");
        exit(-1);
    }

    // init & start Vsync...
    //Vsync vsync(60, vsyncCallback);
    //vsync.start(); 

    //initialize the socket sub-system.
    android_socket_init();

    printf("Create server socket on port %d...\n", SOCKET_SERVER_PORT);
    //const int serverSocket = android::base::socketTcp4LoopbackServer(SOCKET_SERVER_PORT);
    const int serverSocket = socket_anyaddr_server(SOCKET_SERVER_PORT, SOCKET_STREAM);
    if(serverSocket == -1)    {
        printf("Error create local socket server on port %d: %s\n",
               SOCKET_SERVER_PORT, strerror(errno));
        exit(-1);
    }

    for(;;)    {
        printf("Accepting guest connection on port %d...\n", SOCKET_SERVER_PORT);
        int socketHandle = android::base::socketAcceptAny(serverSocket);
        if(socketHandle == -1)    {
            printf("Error accepting server socket: %s\n", strerror(errno));
            exit(-1);
        }

        printf("New guest connection %d established on port %d...\n", socketHandle, SOCKET_SERVER_PORT);
  
        sRenderChannel = sRenderer->createRenderChannel();

        OpenglEsStreamHandler *renderer = new OpenglEsStreamHandler(socketHandle, sRenderChannel);
        renderer->start();
    }

    return 0;
}
