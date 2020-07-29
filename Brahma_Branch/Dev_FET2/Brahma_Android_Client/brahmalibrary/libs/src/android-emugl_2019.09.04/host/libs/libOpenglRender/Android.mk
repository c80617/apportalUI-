LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := OSWindow

LOCAL_SRC_FILES += \
    standalone_common/angle-util/OSWindow.cpp \

ifeq ($(BUILD_TARGET_OS),ANDROID)
    LOCAL_SRC_FILES += standalone_common/angle-util/android/AndroidWindow.cpp
endif

LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libOpenglRender/standalone_common/angle-util

LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_CXXFLAGS := -Wno-format
LOCAL_STATIC_LIBRARIES  :=
LOCAL_SHARED_LIBRARIES  :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  :=

include $(BUILD_STATIC_LIBRARY)

############## OpenglRender_vulkan ############################

include $(CLEAR_VARS)
LOCAL_MODULE := OpenglRender_vulkan

LOCAL_SRC_FILES += \
    vulkan/VkAndroidNativeBuffer.cpp \
    vulkan/VkCommonOperations.cpp \
    vulkan/VkDecoder.cpp \
    vulkan/VkDecoderGlobalState.cpp \
    vulkan/VkDecoderSnapshot.cpp \
    vulkan/VkReconstruction.cpp \
    vulkan/VulkanDispatch.cpp \
    vulkan/VulkanHandleMapping.cpp \
    vulkan/VulkanStream.cpp

LOCAL_C_INCLUDES  := \
    ${EMUGL_PATH}/host/libs/libOpenglRender/vulkan/cereal \
    ${EMUGL_PATH}/host/libs/libOpenglRender/vulkan \
    ${TOP_SRC_ROOT}/android-emu \
    ${EMUGL_PATH}/shared \
    ${EMUGL_PATH}/shared/OpenglCodecCommon \
    ${EMUGL_PATH}/host/libs/Translator/include \
    ${EMUGL_PATH}/host/include \
    ${EMUGL_PATH}/host/include/OpenglRender

LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_CXXFLAGS := -Wno-return-type-c-linkage -Wno-format -Wno-null-conversion \
    -Wno-unused-value -Wno-shift-count-overflow -Wno-constant-conversion]
LOCAL_STATIC_LIBRARIES := \
    emugl_common OpenglRender_vulkan_cereal GLcommon
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  :=

include $(BUILD_STATIC_LIBRARY)

############## OpenglRender ###################################

include $(CLEAR_VARS)

host_OS_SRCS :=
host_common_LDLIBS :=

host_common_SRC_FILES := \
    $(host_OS_SRCS) \
    ChannelStream.cpp \
    ColorBuffer.cpp \
    FbConfig.cpp \
    FenceSync.cpp \
    FrameBuffer.cpp \
    GLESVersionDetector.cpp \
    PostWorker.cpp \
    ReadbackWorker.cpp \
    ReadBuffer.cpp \
    RenderChannelImpl.cpp \
    RenderContext.cpp \
    RenderControl.cpp \
    RendererImpl.cpp \
    RenderLibImpl.cpp \
    RenderThread.cpp \
    RenderThreadInfo.cpp \
    render_api.cpp \
    RenderWindow.cpp \
    SyncThread.cpp \
    TextureDraw.cpp \
    TextureResize.cpp \
    WindowSurface.cpp \
    YUVConverter.cpp \
    \
    NativeSubWindow_android.cpp

LOCAL_MODULE := OpenglRender
LOCAL_SRC_FILES := $(host_common_SRC_FILES)
LOCAL_C_INCLUDES  := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/shared \
    $(TOP_SRC_ROOT)/android-emugl/host/include/OpenglRender \
    $(TOP_SRC_ROOT)/android-emugl/shared/OpenglCodecCommon \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv1_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv1_dec/intermediates-dir \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv2_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/GLESv2_dec/intermediates-dir \
    $(TOP_SRC_ROOT)/android-emugl/host/include/OpenGLESDispatch \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libGLSnapshot \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/renderControl_dec \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/renderControl_dec/intermediates-dir \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libOpenglRender \
    $(TOP_SRC_ROOT)/android-emugl/host/include/vulkan \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/Translator/include \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libOpenGLESDispatch \
    $(TOP_SRC_ROOT)/android-emugl/host/libs/libOpenglRender/vulkan

LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_CXXFLAGS := -Wno-return-type-c-linkage -Wno-format -Wno-null-conversion
#LOCAL_WHOLE_STATIC_LIBRARIES := \
#    OpenglRender_vulkan OpenglRender_vulkan_cereal
LOCAL_WHOLE_STATIC_LIBRARIES := \
    renderControl_dec GLESv2_dec GLESv1_dec \
    OpenglCodecCommon emugl_common \
    OpenGLESDispatch GLSnapshot OSWindow
LOCAL_SHARED_LIBRARIES := android-emu-base
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  := -landroid

include $(BUILD_SHARED_LIBRARY)
