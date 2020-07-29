#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
    main.cpp \
    OpenglEsStreamHandler.cpp \
    RendererDisplay.cpp \
    Vsync.cpp

LOCAL_MODULE := render-server
LOCAL_C_INCLUDES := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/android-emugl/shared/OpenglCodecCommon \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libOpenglRender \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libOpenglRender/standalone_common/angle-util
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_CXXFLAGS := -Wno-return-type-c-linkage

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_C_INCLUDES += $(TOP_SRC_ROOT)/gperftools/out_32/include/gperftools
    LOCAL_LDLIBS := -L$(TOP_SRC_ROOT)/gperftools/out_32/lib -ltcmalloc
#    LOCAL_STATIC_LIBRARIES := unwind32
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_C_INCLUDES += $(TOP_SRC_ROOT)/gperftools/out_64/include/gperftools
    LOCAL_LDLIBS := -L$(TOP_SRC_ROOT)/gperftools/out_64/lib -ltcmalloc
#    LOCAL_STATIC_LIBRARIES := unwind
endif

LOCAL_SHARED_LIBRARIES  := \
    android-emu-base \
    OpenglRender \
    EGL_translator \
    GLES_CM_translator \
    GLES12Translator \
    GLES_V2_translator

LOCAL_LDFLAGS :=

include $(BUILD_EXECUTABLE)
