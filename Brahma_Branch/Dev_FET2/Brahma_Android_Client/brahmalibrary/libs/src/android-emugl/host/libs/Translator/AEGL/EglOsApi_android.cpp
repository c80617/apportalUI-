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
#include "EglOsApi.h"

#include "GLcommon/GLLibrary.h"
#include "OpenglCodecCommon/ErrorLog.h"
#include "emugl/common/lazy_instance.h"
#include "emugl/common/shared_library.h"

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <memory>

#include <android/native_window_jni.h>

#define DEBUG 0
#if DEBUG
#define D(...) DBG(__VA_ARGS__)
#define CHECK_EGL_ERR                                                 \
        {                                                                 \
            EGLint err = mDispatcher.eglGetError();                       \
            if (err != EGL_SUCCESS)                                       \
                D("%s: %s %d get egl error %d\n", __FUNCTION__, __FILE__, \
                  __LINE__, err);                                         \
        }
#else
#define D(...) ((void)0);
#define CHECK_EGL_ERR ((void)0);
#endif

static const char* kEGLLibName = "libEGL.so";

#ifdef USE_GLESV3
static const char* kGLES2LibName = "libGLESv3.so";
#else
static const char* kGLES2LibName = "libGLESv2.so";
#endif

// List of EGL functions of interest to probe with GetProcAddress()
#define LIST_EGL_FUNCTIONS(X)                                                  \
    X(EGLBoolean, eglChooseConfig,                                             \
      (EGLDisplay display, EGLint const* attrib_list, EGLConfig* configs,      \
       EGLint config_size, EGLint* num_config))                                \
    X(EGLContext, eglCreateContext,                                            \
      (EGLDisplay display, EGLConfig config, EGLContext share_context,         \
       EGLint const* attrib_list))                                             \
    X(EGLSurface, eglCreatePbufferSurface,                                     \
      (EGLDisplay display, EGLConfig config, EGLint const* attrib_list))       \
    X(EGLBoolean, eglDestroyContext, (EGLDisplay display, EGLContext context)) \
    X(EGLBoolean, eglDestroySurface, (EGLDisplay display, EGLSurface surface)) \
    X(EGLBoolean, eglGetConfigAttrib,                                          \
      (EGLDisplay display, EGLConfig config, EGLint attribute,                 \
       EGLint * value))                                                        \
    X(EGLDisplay, eglGetDisplay, (NativeDisplayType native_display))           \
    X(EGLint, eglGetError, (void))                                             \
    X(EGLBoolean, eglInitialize,                                               \
      (EGLDisplay display, EGLint * major, EGLint * minor))                    \
    X(EGLBoolean, eglMakeCurrent,                                              \
      (EGLDisplay display, EGLSurface draw, EGLSurface read,                   \
       EGLContext context))                                                    \
    X(EGLBoolean, eglSwapBuffers, (EGLDisplay display, EGLSurface surface))    \
    X(EGLSurface, eglCreateWindowSurface,                                      \
      (EGLDisplay display, EGLConfig config,                                   \
       EGLNativeWindowType native_window, EGLint const* attrib_list))

namespace {

using namespace EglOS;

class AndroidEglDispatcher {
public:
#define DECLARE_EGL_POINTER(return_type, function_name, signature) \
    return_type(EGLAPIENTRY* function_name) signature = nullptr;
    LIST_EGL_FUNCTIONS(DECLARE_EGL_POINTER);

    AndroidEglDispatcher() {
        D("AndroidEglDispatcher:: loading %s\n", kEGLLibName);
        char error[256];
        mLib = emugl::SharedLibrary::open(kEGLLibName, error, sizeof(error));
        if (!mLib) {
            ERR("AndroidEglDispatcher::%s: Could not open EGL library %s [%s]\n",
                __FUNCTION__, kEGLLibName, error);
        }

#define LOAD_EGL_POINTER(return_type, function_name, signature)    \
    this->function_name =                                          \
            reinterpret_cast<return_type(GL_APIENTRY*) signature>( \
                    mLib->findSymbol(#function_name));             \
    if (!this->function_name) {                                    \
        ERR("%s: Could not find %s in GL library\n", __FUNCTION__, \
            #function_name);                                       \
    }

        LIST_EGL_FUNCTIONS(LOAD_EGL_POINTER);
    }

    ~AndroidEglDispatcher() = default;

private:
    emugl::SharedLibrary* mLib = nullptr;
};

class AndroidGlLibrary : public GlLibrary {
public:
    AndroidGlLibrary() {
        char error[256];
        mLib = emugl::SharedLibrary::open(kGLES2LibName, error, sizeof(error));
        if (!mLib) {
            ERR("AndroidGlLibrary::%s: Could not open GL library %s [%s]\n",
                __FUNCTION__, kGLES2LibName, error);
        } else    {
            INFO("AndroidGlLibrary::%s: Finished loading open GL library %s...",
                 __FUNCTION__, kGLES2LibName);
        }
    }

    GlFunctionPointer findSymbol(const char* name) {
        if (!mLib) {
            return NULL;
        }

        return reinterpret_cast<GlFunctionPointer>(mLib->findSymbol(name));
    }

    ~AndroidGlLibrary() = default;

private:
    emugl::SharedLibrary* mLib = nullptr;
};

class AndroidEglPixelFormat : public EglOS::PixelFormat {
public:
    AndroidEglPixelFormat(EGLConfig configId, EGLint clientCtxVer)
        : mConfigId(configId), mClientCtxVer(clientCtxVer) {}

    PixelFormat* clone() {
        return new AndroidEglPixelFormat(mConfigId, mClientCtxVer);
    }

    EGLConfig mConfigId;
    EGLint mClientCtxVer;
};

class AndroidEglContext : public EglOS::Context {
public:
    AndroidEglContext(AndroidEglDispatcher* dispatcher,
                      EGLDisplay display,
                      EGLContext context) :
        mDispatcher(dispatcher),
        mDisplay(display),
        mNativeCtx(context) { }

    ~AndroidEglContext() {
        D("AndroidEglContext::%s %p\n", __FUNCTION__, mNativeCtx);
        if (!mDispatcher->eglDestroyContext(mDisplay, mNativeCtx)) {
            // TODO: print a better error message
        }
    }

    EGLContext context() const {
        return mNativeCtx;
    }

private:
    AndroidEglDispatcher* mDispatcher = nullptr;
    EGLDisplay mDisplay;
    EGLContext mNativeCtx;
};

class AndroidEglSurface : public EglOS::Surface {
public:
    AndroidEglSurface(SurfaceType type,
                      EGLSurface eglSurface,
                      EGLNativeWindowType win = 0)
        : EglOS::Surface(type), mHndl(eglSurface), mWin(win) {}

    EGLSurface getHndl() { return mHndl; }
    EGLNativeWindowType getWin() { return mWin; }

private:
    EGLSurface mHndl;
    EGLNativeWindowType mWin;
};

class AndroidEglDisplay : public EglOS::Display {
public:
    AndroidEglDisplay();
    ~AndroidEglDisplay();
    virtual EglOS::GlesVersion getMaxGlesVersion();
    void queryConfigs(int renderableType,
                      AddConfigCallback* addConfigFunc,
                      void* addConfigOpaque);
    virtual emugl::SmartPtr<EglOS::Context>
    createContext(EGLint profileMask,
                  const PixelFormat* pixelFormat,
                  EglOS::Context* sharedContext) override;
    Surface* createPbufferSurface(const PixelFormat* pixelFormat,
                                  const PbufferInfo* info);
    Surface* createWindowSurface(PixelFormat* pf, EGLNativeWindowType win);
    bool releasePbuffer(Surface* pb);
    bool makeCurrent(Surface* read, Surface* draw, EglOS::Context* context);
    void swapBuffers(Surface* srfc);
    bool isValidNativeWin(Surface* win);
    bool isValidNativeWin(EGLNativeWindowType win);
    bool checkWindowPixelFormatMatch(EGLNativeWindowType win,
                                     const PixelFormat* pixelFormat,
                                     unsigned int* width,
                                     unsigned int* height);

private:
    EGLDisplay mDisplay;
    AndroidEglDispatcher mDispatcher;
};

AndroidEglDisplay::AndroidEglDisplay() {
    mDisplay = mDispatcher.eglGetDisplay(EGL_DEFAULT_DISPLAY);
    mDispatcher.eglInitialize(mDisplay, nullptr, nullptr);
    CHECK_EGL_ERR
};

AndroidEglDisplay::~AndroidEglDisplay() {
}

EglOS::GlesVersion AndroidEglDisplay::getMaxGlesVersion() {
    // TODO: Detect and return the highest version like in GLESVersionDetector.cpp

    D("AndroidEglDisplay::%s: return ES30, why ???????",  __FUNCTION__);
    return EglOS::GlesVersion::ES30;
}

void AndroidEglDisplay::queryConfigs(int renderableType,
                                     AddConfigCallback* addConfigFunc,
                                     void* addConfigOpaque) {
    D("AndroidEglDisplay::%s, renderableType, == 0x%0x", __FUNCTION__, renderableType);

    // ANGLE does not support GLES1 uses core profile engine.
    // Querying underlying EGL with a conservative set of bits.
#if !(defined(ANDROID) || defined(__ANDROID__))
    renderableType &= ~EGL_OPENGL_ES_BIT;
#endif
    const EGLint attribList[] = {EGL_RENDERABLE_TYPE,
                                 renderableType,
                                 EGL_NONE};
    EGLint numConfigs = 0;
    mDispatcher.eglChooseConfig(mDisplay, attribList, nullptr, 0, &numConfigs);
    CHECK_EGL_ERR
    std::unique_ptr<EGLConfig[]> configs(new EGLConfig[numConfigs]);
    mDispatcher.eglChooseConfig(mDisplay, attribList, configs.get(), numConfigs,
                                &numConfigs);
    CHECK_EGL_ERR
    for (int i = 0; i < numConfigs; i++) {
        const EGLConfig cfg = configs.get()[i];
        ConfigInfo configInfo;
        // We do not have recordable_android
        configInfo.recordable_android = 0;
        EGLint _renderableType;
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_RENDERABLE_TYPE,
                                       &_renderableType);
        // We do emulate GLES1
        configInfo.renderable_type = _renderableType | EGL_OPENGL_ES_BIT;

        configInfo.frmt = new AndroidEglPixelFormat(cfg, _renderableType);

        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_RED_SIZE,
                                       &configInfo.red_size);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_GREEN_SIZE,
                                       &configInfo.green_size);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_BLUE_SIZE,
                                       &configInfo.blue_size);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_ALPHA_SIZE,
                                       &configInfo.alpha_size);

        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_CONFIG_CAVEAT,
                                       (EGLint*)&configInfo.caveat);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_DEPTH_SIZE,
                                       &configInfo.depth_size);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_LEVEL,
                                       &configInfo.frame_buffer_level);

        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_MAX_PBUFFER_WIDTH,
                                       &configInfo.max_pbuffer_width);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_MAX_PBUFFER_HEIGHT,
                                       &configInfo.max_pbuffer_height);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_MAX_PBUFFER_PIXELS,
                                       &configInfo.max_pbuffer_size);

        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_NATIVE_RENDERABLE,
                                       (EGLint*)&configInfo.native_renderable);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_NATIVE_VISUAL_ID,
                                       &configInfo.native_visual_id);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_NATIVE_VISUAL_TYPE,
                                       &configInfo.native_visual_type);

        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_SAMPLES,
                                       &configInfo.samples_per_pixel);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_STENCIL_SIZE,
                                       &configInfo.stencil_size);

        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_SURFACE_TYPE,
                                       &configInfo.surface_type);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_TRANSPARENT_TYPE,
                                       (EGLint*)&configInfo.transparent_type);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg, EGL_TRANSPARENT_RED_VALUE,
                                       &configInfo.trans_red_val);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg,
                                       EGL_TRANSPARENT_GREEN_VALUE,
                                       &configInfo.trans_green_val);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg,
                                       EGL_TRANSPARENT_BLUE_VALUE,
                                       &configInfo.trans_blue_val);
        
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg,
                                       EGL_RECORDABLE_ANDROID,
                                       (EGLint*)&configInfo.recordable_android);
        mDispatcher.eglGetConfigAttrib(mDisplay, cfg,
                                       EGL_FRAMEBUFFER_TARGET_ANDROID,
                                       (EGLint*)&configInfo.framebuffer_target_android);
        CHECK_EGL_ERR

        D("AndroidEglDisplay:: config %p renderable type 0x%x, "
          "red_size(%d), green_size(%d), blue_size(%d), alpha_size(%d), "
          "GL_DEPTH_SIZE(%d), EGL_LEVEL(%d), surface_type(%d), "
          "recordable_android(%d), framebuffer_target_android(%d), native_visual_id(%d)",
          cfg, _renderableType, configInfo.red_size, configInfo.green_size,
          configInfo.blue_size, configInfo.alpha_size, configInfo.depth_size,
          configInfo.frame_buffer_level, configInfo.surface_type,
          configInfo.recordable_android, configInfo.framebuffer_target_android,
          configInfo.native_visual_id);

        addConfigFunc(addConfigOpaque, &configInfo);
    }

    D("EglOsApi_android::%s: Host gets %d configs\n", __FUNCTION__, numConfigs);
}

emugl::SmartPtr<EglOS::Context>
AndroidEglDisplay::createContext(EGLint profileMask,
                                 const PixelFormat* pixelFormat,
                                 EglOS::Context* sharedContext) {
    (void)profileMask;

    const AndroidEglPixelFormat* format = (const AndroidEglPixelFormat*)pixelFormat;

    D("AndroidEglDisplay::%s with EGLConfig == %p, sharedContext == %p",
      __FUNCTION__, format->mConfigId, sharedContext);

    // Always GLES3
    EGLint attrib_list[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
    // TODO: support GLES3.1
    AndroidEglContext* nativeSharedCtx = (AndroidEglContext*)sharedContext;
    EGLContext newNativeCtx =
        mDispatcher.eglCreateContext(
            mDisplay, format->mConfigId,
            nativeSharedCtx ? nativeSharedCtx->context() : nullptr,
            attrib_list);
    CHECK_EGL_ERR
    emugl::SmartPtr<EglOS::Context> res =
        std::make_shared<AndroidEglContext>(
            &mDispatcher, mDisplay, newNativeCtx);
    D("AndroidEglDisplay::%s done\n", __FUNCTION__);

    return res;
}

Surface* AndroidEglDisplay::createPbufferSurface(const PixelFormat* pixelFormat,
                                                 const PbufferInfo* info) {
    D("EglOsApi_android:: %s", __FUNCTION__);
    const AndroidEglPixelFormat* format = (const AndroidEglPixelFormat*)pixelFormat;
    EGLint attrib[] = {EGL_WIDTH, info->width,
                       EGL_HEIGHT, info->height,
                       EGL_LARGEST_PBUFFER, info->largest,
                       EGL_TEXTURE_FORMAT, info->format,
                       EGL_TEXTURE_TARGET, info->target,
                       EGL_MIPMAP_TEXTURE, info->hasMipmap,
                       EGL_NONE};
    EGLSurface surface = mDispatcher.eglCreatePbufferSurface(
            mDisplay, format->mConfigId, attrib);
    CHECK_EGL_ERR
    if (surface == EGL_NO_SURFACE) {
        D("create pbuffer surface failed\n");
        return nullptr;
    }

    return new AndroidEglSurface(EglOS::Surface::PBUFFER, surface);
}

Surface* AndroidEglDisplay::createWindowSurface(PixelFormat* pf,
                                                EGLNativeWindowType win) {
    D("AndroidEglDisplay::%s: win == %p, EGLConfig == %p",
      __FUNCTION__, win, ((AndroidEglPixelFormat*)pf)->mConfigId);
    EGLSurface surface = mDispatcher.eglCreateWindowSurface(
            mDisplay, ((AndroidEglPixelFormat*)pf)->mConfigId, win, nullptr);
    CHECK_EGL_ERR

    if (surface == EGL_NO_SURFACE) {
        D("%s: create window surface failed", __FUNCTION__);
        return nullptr;
    }

    D("AndroidEglDisplay::%s: win == %p, surface == %p\n", __FUNCTION__, win, surface);

    return new AndroidEglSurface(EglOS::Surface::WINDOW, surface, win);
}

bool AndroidEglDisplay::releasePbuffer(Surface* pb) {
    D("%s\n", __FUNCTION__);
    if (!pb)    return false;

    AndroidEglSurface* surface = (AndroidEglSurface*)pb;
    bool ret = mDispatcher.eglDestroySurface(mDisplay, surface->getHndl());
    CHECK_EGL_ERR

    D("%s done\n", __FUNCTION__);

    return ret;
}

bool AndroidEglDisplay::makeCurrent(Surface* read,
                                    Surface* draw,
                                    EglOS::Context* context) {
    D("AndroidEglDisplay::%s...", __FUNCTION__);
    AndroidEglSurface* readSfc = (AndroidEglSurface*)read;
    AndroidEglSurface* drawSfc = (AndroidEglSurface*)draw;
    AndroidEglContext* ctx = (AndroidEglContext*)context;
    if (ctx && !readSfc) {
        D("AndroidEglDisplay::%s: warning: makeCurrent a context without surface", __FUNCTION__);
        return false;
    }

    D("AndroidEglDisplay::%s: %p, draw surface == %p", __FUNCTION__,
      ctx ? ctx->context() : nullptr, drawSfc ? drawSfc->getHndl() : EGL_NO_SURFACE);

    bool ret = mDispatcher.eglMakeCurrent(
            mDisplay, drawSfc ? drawSfc->getHndl() : EGL_NO_SURFACE,
            readSfc ? readSfc->getHndl() : EGL_NO_SURFACE,
            ctx ? ctx->context() : EGL_NO_CONTEXT);
    if (readSfc) {
        D("AndroidEglDisplay::%s: make current surface type %d %d",
          __FUNCTION__, readSfc->type(), drawSfc->type());
    }

    D("AndroidEglDisplay::%s: make current %s", __FUNCTION__, ret ? "successfully" : "failed");
    CHECK_EGL_ERR

    return ret;
}

void AndroidEglDisplay::swapBuffers(Surface* surface) {
    AndroidEglSurface* sfc = (AndroidEglSurface*)surface;

    D("AndroidEglDisplay::%s: mWin==%p, surface==%p\n",
      __FUNCTION__, sfc->getWin(), sfc->getHndl());

    mDispatcher.eglSwapBuffers(mDisplay, sfc->getHndl());

    CHECK_EGL_ERR
}

bool AndroidEglDisplay::isValidNativeWin(Surface* win) {
    if (!win)    {
        D("AndroidEglDisplay::%s: surface == NULL", __FUNCTION__);
        return false;
    }

    AndroidEglSurface* surface = (AndroidEglSurface*)win;

    D("AndroidEglDisplay::%s: type == %d, native window = %p",
      __FUNCTION__, surface->type(), surface->getWin());

    return surface->type() == AndroidEglSurface::WINDOW &&
           isValidNativeWin(surface->getWin());
}

bool AndroidEglDisplay::isValidNativeWin(EGLNativeWindowType win) {
    // TODO
    return true;
}

bool AndroidEglDisplay::checkWindowPixelFormatMatch(EGLNativeWindowType win,
                                                    const PixelFormat* pixelFormat,
                                                    unsigned int* width,
                                                    unsigned int* height) {
    *width = ANativeWindow_getWidth((ANativeWindow *)win);
    *height = ANativeWindow_getHeight((ANativeWindow *)win);

    D("AndroidEglDisplay::%s: native window = %p, %d x %d",
      __FUNCTION__, win, *width, *height);

    /* EGL_NATIVE_VISUAL_ID is an attribute of the EGLConfig that is
     * guaranteed to be accepted by ANativeWindow_setBuffersGeometry().
     * As soon as we picked a EGLConfig, we can safely reconfigure the
     * ANativeWindow buffers to match, using EGL_NATIVE_VISUAL_ID.
    EGLint format;
    s_egl.eglGetConfigAttrib(mDisplay, m_eglConfig , EGL_NATIVE_VISUAL_ID, &format);
    ANativeWindow_setBuffersGeometry(m_subWin, 0, 0, format);
     */

    return true;
}

static emugl::LazyInstance<AndroidEglDisplay> sHostDisplay = LAZY_INSTANCE_INIT;

class AndroidEngine : public EglOS::Engine {
public:
    AndroidEngine() = default;
    ~AndroidEngine() = default;

    EglOS::Display* getDefaultDisplay() {
        D("AndroidEngine::%s", __FUNCTION__);
        return sHostDisplay.ptr();
    }

    GlLibrary* getGlLibrary() {
        D("AndroidEngine::%s\n", __FUNCTION__);
        return &mGlLib;
    }

    virtual EglOS::Surface* createWindowSurface(PixelFormat* pf,
                                                EGLNativeWindowType wnd) {
        D("AndroidEngine::%s\n", __FUNCTION__);
        return sHostDisplay->createWindowSurface(pf, wnd);
    }

private:
    AndroidGlLibrary mGlLib;
};

emugl::LazyInstance<AndroidEngine> sHostEngine = LAZY_INSTANCE_INIT;

}  // namespace

// static
EglOS::Engine* EglOS::Engine::getHostInstance() {
    return sHostEngine.ptr();
}
