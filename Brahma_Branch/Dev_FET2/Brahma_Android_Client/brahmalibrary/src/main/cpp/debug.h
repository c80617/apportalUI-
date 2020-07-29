/*
 * Copyright (C) 2015-2017 ICL/ITRI
 * All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ICL/ITRI and its suppliers, if any.
 * The intellectual and technical concepts contained
 * herein are proprietary to ICL/ITRI and its suppliers and
 * may be covered by Taiwan and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from ICL/ITRI.
 */

#pragma once

#include <stdio.h>
#include <errno.h>
#include <string.h>

#if (defined(ANDROID) || defined(__ANDROID__))

#include <android/log.h>

// Usage: Define DEBUG_LEVEL before including this header to
//        select the behaviour of the DEBUG() and DDEBUG() macros.
#if defined(DEBUG_LEVEL) && DEBUG_LEVEL > 0
#define DEBUG(TAG, FMT, ...) \
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "(%s:%d) " FMT "", __FILE__, __LINE__, \
                        ##__VA_ARGS__)
#else
#define DEBUG(TAG, FMT, ...)
#endif // DEBUG_LEVEL > 0

#if defined(DEBUG_LEVEL) && DEBUG_LEVEL > 1
#define DDEBUG(...) DEBUG(__VA_ARGS__)
#else
#define DDEBUG(...) (void)0
#endif // EMUGL_DEBUG_LEVEL > 1

#define clean_errno() (errno == 0 ? "None" : strerror(errno))

#define LOG_ERR(TAG, FMT, ...) \
    __android_log_print(ANDROID_LOG_ERROR, TAG, "(%s:%d: errno: %s) " FMT "", __FILE__, __LINE__, \
                        clean_errno(), ##__VA_ARGS__)

#define LOG_ERR_ARGS(TAG, FMT, ARGS)                                            \
    do    {                                                                     \
        char buffer[2048];                                                      \
        int i = sprintf(buffer, "(%s:%d:%s) ", __FILE__, __LINE__, __func__);   \
        int j = vsprintf(&buffer[i], FMT, ARGS);                                \
        buffer[i + j] = '\0';                                                   \
        __android_log_print(ANDROID_LOG_ERROR, TAG, "%s", buffer);              \
    } while(0)

#define LOG_WARN(TAG, FMT, ...) \
    __android_log_print(ANDROID_LOG_WARN, TAG, "(%s:%d: errno: %s) " FMT "", __FILE__, __LINE__, \
                        clean_errno(), ##__VA_ARGS__)

#define LOG_INFO(TAG, FMT, ...) \
    __android_log_print(ANDROID_LOG_INFO, TAG, FMT, ##__VA_ARGS__)

#define LOG_DEBUG(TAG, FMT, ...) \
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "(%s:%d) " FMT "", __FILE__, __LINE__, \
                        ##__VA_ARGS__)

#define LOG_DEBUG_S(TAG, FMT, ...) \
    __android_log_print(ANDROID_LOG_DEBUG, TAG, FMT, ##__VA_ARGS__)

#define LOG_VERBOSE(TAG, FMT, ARGS) \
    __android_log_vprint(ANDROID_LOG_VERBOSE, TAG, FMT, ARGS)

#else

#ifdef NDEBUG
#define DEBUG(TAG, FMT, ...)
#else
#define DEBUG(TAG, FMT, ...) \
    fprintf(stderr, TAG ": DEBUG %s:%d: " FMT "", __FILE__, __LINE__, ##__VA_ARGS__)
#endif

#define clean_errno() (errno == 0 ? "None" : strerror(errno))

#define LOG_ERR(TAG, FMT, ...) \
    fprintf(stderr, TAG ": [ERROR] (%s:%d: errno: %s) " FMT "", __FILE__, __LINE__, \
            clean_errno(), ##__VA_ARGS__);                                          \
    fprintf(stderr, "\n")

#define LOG_WARN(TAG, FMT, ...) \
    fprintf(stderr, TAG ": [WARN] (%s:%d: errno: %s) " FMT "", __FILE__, __LINE__, \
            clean_errno(), ##__VA_ARGS__);                                         \
    fprintf(stderr, "\n")

#define LOG_INFO(TAG, FMT, ...) \
    fprintf(stderr, TAG ": [INFO] " FMT "", ##__VA_ARGS__); fprintf(stderr, "\n")

#define LOG_DEBUG(TAG, FMT, ...) \
    fprintf(stderr, TAG ": [DEBUG] (%s:%d) " FMT "", __FILE__, __LINE__, ##__VA_ARGS__); \
    fprintf(stderr, "\n")

#define LOG_DEBUG_S(TAG, FMT, ...) \
    vfprintf(stderr, TAG ": [DEBUG] " FMT "", ##__VA_ARGS__); fprintf(stderr, "\n")

#define LOG_VERBOSE(TAG, FMT, ...) \
    vfprintf(stderr, TAG ": [DEBUG] " FMT "", ##__VA_ARGS__); fprintf(stderr, "\n")

#endif