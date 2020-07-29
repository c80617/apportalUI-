# This is the top-level global definitions  

#BUILD_TARGET_OS := linux
BUILD_TARGET_OS := ANDROID
EMUGL_DEBUG := true

EMULATOR_USE_ANGLE := false

ANDROID_EMU_BASE_INCLUDES := $(LOCAL_PATH)/android-emu

# List of static libraries that anything that depends on the base libraries
# should use.
ANDROID_EMU_BASE_STATIC_LIBRARIES := \
    android-emu-base
