LOCAL_PATH := $(call my-dir)

host_common_SRC_FILES :=   \
     CoreProfileEngine.cpp \
     GLEScmImp.cpp         \
     GLEScmUtils.cpp       \
     GLEScmContext.cpp     \
     GLEScmValidate.cpp

include $(CLEAR_VARS)

LOCAL_MODULE := GLES_CM_translator
LOCAL_SRC_FILES := $(host_common_SRC_FILES)
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/Translator/GLcommon/../include \
    $(TOP_SRC_ROOT)/android-emugl/shared
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_STATIC_LIBRARIES  := android-emu-base OpenGLESDispatch emugl_common android-emu-base GLcommon
LOCAL_SHARED_LIBRARIES  :=
LOCAL_LDFLAGS := -Wl,-Bsymbolic
#LOCAL_LDLIBS  := $(ANDROID_EMU_BASE_LDLIBS)
LOCAL_LDLIBS  :=

include $(BUILD_SHARED_LIBRARY)
