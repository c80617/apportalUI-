LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := footprint
LOCAL_SRC_FILES := src/decoder/footprint.cc

LOCAL_C_INCLUDES  := include

LOCAL_CFLAGS :=
LOCAL_STATIC_LIBRARIES  :=
LOCAL_SHARED_LIBRARIES  :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS :=

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := astc_utils 
LOCAL_SRC_FILES := \
    src/decoder/astc_file.cc \
    src/decoder/endpoint_codec.cc \
    src/decoder/integer_sequence_codec.cc \
    src/decoder/intermediate_astc_block.cc \
    src/decoder/logical_astc_block.cc \
    src/decoder/partition.cc \
    src/decoder/physical_astc_block.cc \
    src/decoder/quantization.cc \
    src/decoder/weight_infill.cc

LOCAL_C_INCLUDES  := include
LOCAL_STATIC_LIBRARIES  := footprint

include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := astc-codec
LOCAL_SRC_FILES := src/decoder/codec.cc
LOCAL_C_INCLUDES  := include
LOCAL_STATIC_LIBRARIES  := astc_utils

include $(BUILD_STATIC_LIBRARY)
