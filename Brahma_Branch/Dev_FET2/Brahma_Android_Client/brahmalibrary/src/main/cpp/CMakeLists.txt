# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp

             #------------------------------------------------------------
             RenderServer.cpp OpenglEsStreamHandler.cpp
             renderer.cpp
             #------------------------------------------------------------
        )

#-------------------------------------------------------------------------
# configure import libs
set(aemu_root_DIR ${CMAKE_SOURCE_DIR}/../../../)
set(aemu_lib_DIR ${aemu_root_DIR}/libs)
set(android-emu-base_include_DIR ${aemu_lib_DIR}/src/android-emu)
set(android-emugl_include_DIR ${aemu_lib_DIR}/src/android-emugl/host/include)

# -L
add_library(EGL_translator SHARED IMPORTED)
set_target_properties(EGL_translator PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libEGL_translator.so)

add_library(GLES_CM_translator SHARED IMPORTED)
set_target_properties(GLES_CM_translator PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libGLES_CM_translator.so)

add_library(GLES12Translator SHARED IMPORTED)
set_target_properties(GLES12Translator PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libGLES12Translator.so)

add_library(GLES_V2_translator SHARED IMPORTED)
set_target_properties(GLES_V2_translator PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libGLES_V2_translator.so)

add_library(tcmalloc SHARED IMPORTED)
set_target_properties(tcmalloc PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libtcmalloc_minimal.so)

add_library(android-emu-base SHARED IMPORTED)
set_target_properties(android-emu-base PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libandroid-emu-base.so)

add_library(OpenglRender SHARED IMPORTED)
set_target_properties(OpenglRender PROPERTIES IMPORTED_LOCATION
        ${aemu_lib_DIR}/${ANDROID_ABI}/libOpenglRender.so)

# -I
target_include_directories(native-lib PRIVATE
        ${android-emu-base_include_DIR}
        ${aemu_lib_DIR}/src/android-emugl/host/libs/libOpenglRender
        ${aemu_lib_DIR}/src/android-emugl/shared
        ${aemu_lib_DIR}/src/android-emugl/shared/OpenglCodecCommon
        ${android-emugl_include_DIR})

#-------------------------------------------------------------------------

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}

                       #------------------------------------------------------------
                       android
                       EGL_translator GLES_CM_translator GLES12Translator GLES_V2_translator
                       #        tcmalloc
                       android-emu-base OpenglRender
                       #------------------------------------------------------------
        )