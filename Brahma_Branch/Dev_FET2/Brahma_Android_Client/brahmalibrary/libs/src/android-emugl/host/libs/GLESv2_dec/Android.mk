LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := GLESv2_dec
LOCAL_SRC_FILES := GLESv2Decoder.cpp \
        intermediates-dir/gles2_dec.cpp \
        intermediates-dir/gles2_server_context.cpp
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/shared/OpenglCodecCommon \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv2_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv2_dec/intermediates-dir \
    $(TOP_SRC_ROOT)/android-emugl/host/include/OpenGLESDispatch \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libGLSnapshot
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_STATIC_LIBRARIES := 
LOCAL_LDLIBS  :=

include $(BUILD_STATIC_LIBRARY)
