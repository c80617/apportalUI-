LOCAL_PATH := $(call my-dir)

host_OS_SRCS :=

ifeq ($(BUILD_TARGET_OS),ANDROID)
    host_OS_SRCS = EglOsApi_android.cpp \
                   EglOsApi_egl.cpp \
                   CoreProfileConfigs_android.cpp
endif


host_common_SRC_FILES :=      \
     $(host_OS_SRCS)          \
     ThreadInfo.cpp           \
     EglImp.cpp               \
     EglConfig.cpp            \
     EglContext.cpp           \
     EglGlobalInfo.cpp        \
     EglValidate.cpp          \
     EglSurface.cpp           \
     EglWindowSurface.cpp     \
     EglPbufferSurface.cpp    \
     EglThreadInfo.cpp        \
     EglDisplay.cpp           \
     ClientAPIExts.cpp

include $(CLEAR_VARS)

LOCAL_MODULE := EGL_translator
LOCAL_SRC_FILES := $(host_common_SRC_FILES)
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/Translator/include \
    $(TOP_SRC_ROOT)/android-emugl/shared
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1 -fopenmp -Wno-inconsistent-missing-override
LOCAL_CXXFLAGS := -Wno-inconsistent-missing-override
LOCAL_STATIC_LIBRARIES  := GLcommon emugl_common astc-codec
LOCAL_SHARED_LIBRARIES  := android-emu-base
LOCAL_LDFLAGS := -Wl,-Bsymbolic
LOCAL_LDLIBS  := -llog -landroid

include $(BUILD_SHARED_LIBRARY)
