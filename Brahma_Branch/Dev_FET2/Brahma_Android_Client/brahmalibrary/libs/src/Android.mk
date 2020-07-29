LOCAL_PATH:= $(call my-dir)
TOP_SRC_ROOT := $(LOCAL_PATH)

include $(CLEAR_VARS)

include src/libunwind/Android.mk
include src/gperftools/Android.mk
include src/globals.mk
include src/third_party/astc-codec/Android.mk
include src/third_party/angle/Android.mk
include src/android-emu/Android.mk
include src/android-emugl/Android.mk
include src/render-server/Android.mk
