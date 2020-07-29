LOCAL_PATH := $(call my-dir)

common_SRC_FILES :=      \
    android/base/CpuTime.cpp \
    android/base/CpuUsage.cpp \
    android/base/Debug.cpp \
    android/base/files/Fd.cpp \
    android/base/files/MemStream.cpp \
    android/base/files/PathUtils.cpp \
    android/base/files/Stream.cpp \
    android/base/files/StreamSerializing.cpp \
    android/base/GLObjectCounter.cpp \
    android/base/LayoutResolver.cpp \
    android/base/misc/FileUtils.cpp \
    android/base/misc/StringUtils.cpp \
    android/base/StringFormat.cpp \
    android/base/StringParse.cpp \
    android/base/StringView.cpp \
    android/base/Tracing.cpp \
    android/base/Pool.cpp \
    android/base/sockets/SocketDrainer.cpp \
    android/base/sockets/SocketUtils.cpp \
    android/base/sockets/SocketWaiter.cpp \
    android/base/synchronization/MessageChannel.cpp \
    android/base/Log.cpp \
    android/base/memory/LazyInstance.cpp \
    android/base/memory/MemoryTracker.cpp \
    android/base/system/System.cpp \
    android/base/threads/Async.cpp \
    android/base/threads/FunctorThread.cpp \
    android/base/threads/ThreadStore.cpp \
    android/featurecontrol/FeatureControl.cpp \
    android/featurecontrol/ICLFeatureControlImpl.cpp \
    android/utils/bufprint.c \
    android/utils/bufprint_system.cpp \
    android/utils/debug.c \
    android/utils/debug_wrapper.cpp \
    android/utils/dirscanner.cpp \
    android/utils/eintr_wrapper.c \
    android/utils/fd.cpp \
    android/utils/file_io.cpp \
    android/utils/panic.c \
    android/utils/path.cpp \
    android/utils/path_system.cpp \
    android/utils/socket_drainer.cpp \
    android/utils/sockets.c \
    android/utils/system.c \
    android/utils/tempfile.c

# Linux specific sources.
common_SRC_FILES +=      \
    android/base/threads/Thread_pthread.cpp

include $(CLEAR_VARS)

LOCAL_MODULE := android-emu-base
LOCAL_SRC_FILES := $(common_SRC_FILES)

LOCAL_C_INCLUDES := $(TOP_SRC_ROOT)/libunwind/include
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
# tcmalloc/gperftools requires libunwind,
# BUT since we disable the use of tcmalloc, we dont need it anymore
#    LOCAL_STATIC_LIBRARIES := unwind32 
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
#
# tcmalloc/gperftools requires libunwind,
# BUT since we disable the use of tcmalloc, we dont need it anymore 
#    LOCAL_STATIC_LIBRARIES := unwind
endif

LOCAL_CFLAGS := -Wno-parentheses -Wno-invalid-constexpr
LOCAL_CXXFLAGS := -Wno-format

# tcmalloc/gperftools requires libunwind,
# BUT since we disable the use of tcmalloc, we dont need it anymore
# see android/base/memory/MemoryTracker.cpp 
#LOCAL_STATIC_LIBRARIES += tcmalloc
#LOCAL_SHARED_LIBRARIES  := tcmalloc_minimal

# We need this because the current asm generates the following link error:
# requires unsupported dynamic reloc R_ARM_REL32; recompile with -fPIC
# Bug: 16853291
# https://developer.android.com/studio/projects/gradle-external-native-builds
LOCAL_LDFLAGS := -Wl,-Bsymbolic

LOCAL_LDLIBS  :=

include $(BUILD_SHARED_LIBRARY)

