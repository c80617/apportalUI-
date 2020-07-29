#
# Copyright (C) 2015-2017 ICL/ITRI
# All rights reserved.
#
# NOTICE:  All information contained herein is, and remains
# the property of ICL/ITRI and its suppliers, if any.
# The intellectual and technical concepts contained
# herein are proprietary to ICL/ITRI and its suppliers and
# may be covered by Taiwan and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from ICL/ITRI.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := angle-translator

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_SRC_FILES := lib/libtranslator.a.new.a
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := lib64/libtranslator.a.new.a
endif

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS :=
LOCAL_CXXFLAGS :=
LOCAL_STATIC_LIBRARIES :=
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  :=

#include $(PREBUILT_SHARED_LIBRARY)
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := angle-common

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_SRC_FILES := lib/libangle_common.a.new.a
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := lib64/libangle_common.a.new.a
endif

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS :=
LOCAL_CXXFLAGS :=
LOCAL_STATIC_LIBRARIES :=
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  :=

#include $(PREBUILT_SHARED_LIBRARY)
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := angle-preprocessor

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_SRC_FILES := lib/libpreprocessor.a.new.a
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_SRC_FILES := lib64/libpreprocessor.a.new.a
endif

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS :=
LOCAL_CXXFLAGS :=
LOCAL_STATIC_LIBRARIES :=
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS  :=

#include $(PREBUILT_SHARED_LIBRARY)
include $(PREBUILT_STATIC_LIBRARY)
