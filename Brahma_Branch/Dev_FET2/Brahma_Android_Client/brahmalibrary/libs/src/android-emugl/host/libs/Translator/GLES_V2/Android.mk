LOCAL_PATH := $(call my-dir)

host_common_SRC_FILES := \
    GLESv2Imp.cpp \
    GLESv2Context.cpp \
    GLESv2Validate.cpp \
    SamplerData.cpp \
    ShaderParser.cpp \
    ShaderValidator.cpp \
    TransformFeedbackData.cpp \
    ProgramData.cpp \
    ANGLEShaderParser.cpp

include $(CLEAR_VARS)

LOCAL_MODULE := GLES_V2_translator
LOCAL_SRC_FILES := $(host_common_SRC_FILES)
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/Translator/GLcommon/../include \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/shared/OpenglCodecCommon

LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_STATIC_LIBRARIES := \
    OpenglCodecCommon emugl_common GLcommon android-emu-base \
    emugl_common android-emu-base astc-codec \
    angle-translator angle-common angle-preprocessor
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDFLAGS := -Wl,-Bsymbolic
LOCAL_LDLIBS  :=

include $(BUILD_SHARED_LIBRARY)
