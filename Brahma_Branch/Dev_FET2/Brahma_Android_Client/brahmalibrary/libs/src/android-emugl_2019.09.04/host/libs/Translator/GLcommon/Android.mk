LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

host_common_SRC_FILES :=     \
    etc.cpp \
    FramebufferData.cpp \
    GLBackgroundLoader.cpp \
    GLDispatch.cpp \
    GLutils.cpp \
    GLEScontext.cpp \
    GLESvalidate.cpp \
    GLESpointer.cpp \
    GLESbuffer.cpp \
    NamedObject.cpp \
    ObjectData.cpp \
    ObjectNameSpace.cpp \
    PaletteTexture.cpp \
    RangeManip.cpp \
    SaveableTexture.cpp \
    ScopedGLState.cpp \
    ShareGroup.cpp \
    TextureData.cpp \
    TextureUtils.cpp

LOCAL_MODULE := GLcommon
LOCAL_SRC_FILES := $(host_common_SRC_FILES)
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/Translator/include \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/third_party/astc-codec/include
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_CXXFLAGS := -Wno-inconsistent-missing-override
LOCAL_STATIC_LIBRARIES  := emugl_common astc-codec
LOCAL_LDFLAGS  :=
LOCAL_LDLIBS  := -llog
LOCAL_EXPORT_LDLIBS := -llog

include $(BUILD_STATIC_LIBRARY)
