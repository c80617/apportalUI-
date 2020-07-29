/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

#pragma once

#if (defined(ANDROID) || defined(__ANDROID__))
#    include <stdio.h>
#    include <stdarg.h>
#    include <android/log.h>

#    define INFO(...)   __android_log_print(ANDROID_LOG_INFO, "EMUGL", __VA_ARGS__)
#    define WARN(...)   __android_log_print(ANDROID_LOG_WARN, "EMUGL", __VA_ARGS__)
#    define ERROR(...)  __android_log_print(ANDROID_LOG_ERROR, "EMUGL", __VA_ARGS__)
#else
#    include <stdio.h>
#    define INFO(...)    fprintf(stderr, __VA_ARGS__)
#    define WARN(...)    fprintf(stderr, __VA_ARGS__)
#    define ERROR(...)   fprintf(stderr, __VA_ARGS__)
#endif

// Usage: Define EMUGL_DEBUG_LEVEL before including this header to
//        select the behaviour of the D() and DD() macros.

#if defined(EMUGL_DEBUG_LEVEL) && EMUGL_DEBUG_LEVEL > 0

#if (defined(ANDROID) || defined(__ANDROID__))

#define DEBUG(...)    __android_log_print(ANDROID_LOG_DEBUG, "EMUGL", __VA_ARGS__)
#define DEBUG_F(...)                                                            \
    do    {                                                                     \
        char buffer[1024];                                                      \
        int i = sprintf(buffer, "(%s:%d:%s) ", __FILE__, __LINE__, __func__);   \
        int j = sprintf(&buffer[i], ##__VA_ARGS__);                             \
        buffer[i + j] = '\0';                                                   \
        __android_log_print(ANDROID_LOG_DEBUG, "EMUGL", "%s", buffer);          \
    } while(0);
#define D(...)    DEBUG_F(__VA_ARGS__);    

#else

#define DEBUG(...)  (printf(__VA_ARGS__), printf("\n"), fflush(stdout))
#define DEBUG_F(...)  (printf("%s:%d:%s: ", __FILE__, __LINE__, __func__), \
                       printf(__VA_ARGS__), printf("\n"), fflush(stdout))
#define D(...)    DEBUG_F(__VA_ARGS__);

#endif // ANDROID

#else
#define DEBUG(...)    (void)0
#define DEBUG_F(...)  (void)0
#define D(...)        (void)0
#endif // EMUGL_DEBUG_LEVEL > 0

#if defined(EMUGL_DEBUG_LEVEL) && EMUGL_DEBUG_LEVEL > 1
#define DD(...) D(__VA_ARGS__)
#else
#define DD(...) (void)0
#endif // EMUGL_DEBUG_LEVEL > 1
