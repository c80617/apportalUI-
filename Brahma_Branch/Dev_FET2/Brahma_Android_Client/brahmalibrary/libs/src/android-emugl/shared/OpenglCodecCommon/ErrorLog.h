/*
* Copyright (C) 2011 The Android Open Source Project
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
#ifndef _ERROR_LOG_H_
#define _ERROR_LOG_H_

#if (defined(ANDROID) || defined(__ANDROID__))

#include <android/log.h>

#define INFO(...)   __android_log_print(ANDROID_LOG_INFO, "EMUGL", __VA_ARGS__)
#define WARN(...)   __android_log_print(ANDROID_LOG_WARN, "EMUGL", __VA_ARGS__)
#define ERR(...)    __android_log_print(ANDROID_LOG_ERROR, "EMUGL", __VA_ARGS__)   

#ifdef EMUGL_DEBUG
#include <stdio.h>
#include <stdarg.h>
#include <android/log.h>
#define DBG(...)    __android_log_print(ANDROID_LOG_DEBUG, "EMUGL", ##__VA_ARGS__)
#define DDBG(...)                                                               \
    do    {                                                                     \
        char buffer[1024];                                                      \
        int i = sprintf(buffer, "(%s:%d:%s) ", __FILE__, __LINE__, __func__);   \
        int j = sprintf(&buffer[i], ##__VA_ARGS__);                             \
        buffer[i + j] = '\0';                                                   \
        __android_log_print(ANDROID_LOG_DEBUG, "EMUGL", "%s", buffer);          \
    } while(0)
#else
#    define DBG(...)    ((void)0)
#    define DDBG(...)    ((void)0)
#endif


#else

#include <stdio.h>

#define INFO(...)    fprintf(stderr, __VA_ARGS__)
#define WARN(...)    fprintf(stderr, __VA_ARGS__)
#define ERR(...)    fprintf(stderr, __VA_ARGS__)
#ifdef EMUGL_DEBUG
#    define DBG(...)    fprintf(stderr, __VA_ARGS__)
#else
#    define DBG(...)    ((void)0)
#endif

#endif

#endif  // _ERROR_LOG_H_
