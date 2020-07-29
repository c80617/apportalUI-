# This build script corresponds to a library containing many definitions
# common to both the guest and the host. They relate to
#
LOCAL_PATH := $(call my-dir)

### emugl_common host library ###########################################

commonSources := \
        crash_reporter.cpp \
        dma_device.cpp \
        vm_operations.cpp \
        window_operations.cpp \
        feature_control.cpp \
        logging.cpp \
        misc.cpp \
        shared_library.cpp \
        stringparsing.cpp \
        sync_device.cpp

host_commonSources := $(commonSources)

include $(CLEAR_VARS)

LOCAL_MODULE := emugl_common

LOCAL_SRC_FILES := $(host_commonSources)
LOCAL_C_INCLUDES := \
    $(TOP_SRC_ROOT)/android-emu \
    $(TOP_SRC_ROOT)/android-emugl/host/include \
    $(TOP_SRC_ROOT)/android-emugl/shared
LOCAL_CFLAGS := -D__STDC_LIMIT_MACROS=1 -DEMUGL_BUILD=1
LOCAL_STATIC_LIBRARIES := android-emu-base
LOCAL_LDLIBS  :=

include $(BUILD_STATIC_LIBRARY) 
