#ifndef LIBGLESV2_EMULATOR_LOCK
#define LIBGLESV2_EMULATOR_LOCK

#if defined(ANGLE_TRACE_ENABLED)
#define EVENT(message, ...)\
    gl::ScopedLock lock(gl::getGlobalRendererLock());\
    gl::ScopedPerfEventHelper scopedPerfEventHelper ## __LINE__("%s" message "\n", __FUNCTION__, __VA_ARGS__);\
    gl::Context *tc = gl::GetGlobalContext();\
    egl::Surface *trs = egl::GetGlobalReadSurface(), *tds = egl::GetGlobalDrawSurface();\
    if (tc != EGL_NO_CONTEXT && trs != EGL_NO_SURFACE && tds != EGL_NO_SURFACE)\
        if (tc != egl::GetCurContext() || trs != egl::GetCurReadSurface() || tds != egl::GetCurDrawSurface())\
            egl::MakeCurrent_nolock(egl::GetGlobalDisplay(), tds, trs, tc)
#else
#define EVENT(message, ...)\
    gl::ScopedLock lock(gl::getGlobalRendererLock());\
    gl::Context *tc = gl::GetGlobalContext();\
    egl::Surface *trs = egl::GetGlobalReadSurface(), *tds = egl::GetGlobalDrawSurface();\
    if (tc != EGL_NO_CONTEXT && trs != EGL_NO_SURFACE && tds != EGL_NO_SURFACE)\
        if (tc != egl::GetCurContext() || trs != egl::GetCurReadSurface() || tds != egl::GetCurDrawSurface())\
            egl::MakeCurrent_nolock(egl::GetGlobalDisplay(), tds, trs, tc)
#endif

#if defined(ANGLE_TRACE_ENABLED)
#define EVENT_LOCK_ONLY(message, ...)\
    gl::ScopedLock lock(gl::getGlobalRendererLock());\
    gl::ScopedPerfEventHelper scopedPerfEventHelper ## __LINE__("%s" message "\n", __FUNCTION__, __VA_ARGS__)
#else
#define EVENT_LOCK_ONLY(mesage, ...)\
    gl::ScopedLock lock(gl::getGlobalRendererLock())
#endif

#endif
