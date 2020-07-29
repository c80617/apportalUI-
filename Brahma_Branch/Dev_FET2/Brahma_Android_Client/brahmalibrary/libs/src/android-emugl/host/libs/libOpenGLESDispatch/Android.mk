LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := OpenGLESDispatch
LOCAL_SRC_FILES := EGLDispatch.cpp \
                   GLESv2Dispatch.cpp \
                   GLESv1Dispatch.cpp \
                   OpenGLDispatchLoader.cpp

LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/shared/OpenglCodecCommon \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv2_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv1_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/Translator/include \
    $(TOP_SRC_ROOT)/android-emugl/shared
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_STATIC_LIBRARIES  :=
LOCAL_SHARED_LIBRARIES  :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  := -landroid

include $(BUILD_STATIC_LIBRARY)
