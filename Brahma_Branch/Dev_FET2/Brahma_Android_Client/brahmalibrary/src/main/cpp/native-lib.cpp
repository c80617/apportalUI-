#include <jni.h>
#include <string>

// for native window JNI
#include <android/native_window_jni.h>

#include "NativeSubWindow.h"

#include "EGL/eglplatform.h"
#include "debug.h"
#include "RenderServer.h"
#include "renderer.h"

static Renderer *renderer = 0;
static ANativeWindow *theNativeWindow;

extern "C" JNIEXPORT jstring JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_createEMUGLEngine(JNIEnv *env, jclass clazz,
                                                                    jint display_width,
                                                                    jint display_height) {
    if (JNI_FALSE == initialize_render_server(display_width, display_height))
        return JNI_FALSE;

    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_shutdownEMUGLEngine(JNIEnv *env, jclass clazz) {
    // make sure we don't leak native windows
    if (theNativeWindow != NULL) {
        ANativeWindow_release(theNativeWindow);
        theNativeWindow = NULL;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_nativeOnStart(JNIEnv *env, jclass clazz) {
    //renderer = new Renderer();
}

extern "C"
JNIEXPORT void JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_nativeOnResume(JNIEnv *env, jclass clazz) {
    //renderer->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_nativeOnPause(JNIEnv *env, jclass clazz) {
    //renderer->stop();
}

extern "C"
JNIEXPORT void JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_nativeOnStop(JNIEnv *env, jclass clazz) {
    //delete renderer;
    //renderer = 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_brahma_vmi_brahmalibrary_wcitui_MainActivity_nativeSetSurface(JNIEnv *env, jclass clazz,
                                                                   jobject surface) {
    if (surface != 0) {
        // obtain a native window from a Java surface
        theNativeWindow = ANativeWindow_fromSurface(env, surface);

        // Preventing the object from being deleted until the reference is removed.
        ANativeWindow_acquire(theNativeWindow);

        // Configure EMUGL core to use this Android ANativeWindow
        //setSubWindow(theNativeWindow);

        int width = ANativeWindow_getWidth(theNativeWindow);
        int height = ANativeWindow_getHeight(theNativeWindow);
        start_render_server(0, theNativeWindow, width, height);
        //start_render_server(0, theNativeWindow, 720, 1280);

        //renderer->setWindow(theNativeWindow);
    } else {
        LOG_INFO("RENDER_SERVER", "MainActivity_setSurface: Releasing window...");
        ANativeWindow_release(theNativeWindow);
        theNativeWindow = 0;
    }
}
