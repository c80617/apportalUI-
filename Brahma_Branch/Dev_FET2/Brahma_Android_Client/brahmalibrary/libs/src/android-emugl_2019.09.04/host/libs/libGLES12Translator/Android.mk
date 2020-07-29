LOCAL_PATH := $(call my-dir)

GLES_TR_DIR=gles
COMMON_DIR=common

GLES_TR_CPPS=${GLES_TR_DIR}/api_entries.cpp \
             ${GLES_TR_DIR}/buffer_data.cpp \
             ${GLES_TR_DIR}/debug.cpp \
             ${GLES_TR_DIR}/egl_image.cpp \
             ${GLES_TR_DIR}/framebuffer_data.cpp \
             ${GLES_TR_DIR}/gles1_shader_generator.cpp \
             ${GLES_TR_DIR}/gles_context.cpp \
             ${GLES_TR_DIR}/gles_validate.cpp \
             ${GLES_TR_DIR}/matrix.cpp \
             ${GLES_TR_DIR}/paletted_texture_util.cpp \
             ${GLES_TR_DIR}/pass_through.cpp \
             ${GLES_TR_DIR}/program_data.cpp \
             ${GLES_TR_DIR}/program_variant.cpp \
             ${GLES_TR_DIR}/renderbuffer_data.cpp \
             ${GLES_TR_DIR}/shader_data.cpp \
             ${GLES_TR_DIR}/shader_variant.cpp \
             ${GLES_TR_DIR}/share_group.cpp \
             ${GLES_TR_DIR}/state.cpp \
             ${GLES_TR_DIR}/texture_codecs.cpp \
             ${GLES_TR_DIR}/texture_data.cpp \
             ${GLES_TR_DIR}/uniform_value.cpp \
             ${GLES_TR_DIR}/vector.cpp \
             ${GLES_TR_DIR}/translator_interface.cpp \
             ${COMMON_DIR}/log.cpp \
             ${COMMON_DIR}/etc1.cpp \
             ${COMMON_DIR}/RefBase.cpp

include $(CLEAR_VARS)

LOCAL_MODULE := GLES12Translator
LOCAL_SRC_FILES := $(GLES_TR_CPPS)
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/android-emugl/host/include/OpenglRender \
    $(TOP_SRC_ROOT)/android-emugl/shared/OpenglCodecCommon \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv1_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libGLES12Translator
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_STATIC_LIBRARIES  :=
LOCAL_SHARED_LIBRARIES  := android-emu-base
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  :=

include $(BUILD_SHARED_LIBRARY)
