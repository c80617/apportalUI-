//
// Copyright(c) 2014 The ANGLE Project Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
//

// global_state.h : Defines functions for querying the thread-local GL and EGL state.

#ifndef LIBGLESV2_GLOBALSTATE_H_
#define LIBGLESV2_GLOBALSTATE_H_

#include <mutex>
#include <EGL/egl.h>

namespace gl
{
class Context;

Context *GetGlobalContext();
Context *GetValidGlobalContext();

// Duplicate of emulator's Lock and AutoLock
// specialized to Windows

class ScopedLock;
#ifdef ANGLE_PLATFORM_WINDOWS
class Lock {
public:
	Lock() {
		InitializeCriticalSection(&mLock);
	}
	~Lock() {
		DeleteCriticalSection(&mLock);
	}
	void lock() {
		EnterCriticalSection(&mLock);
	}
	void unlock() {
		LeaveCriticalSection(&mLock);
	}
private:
	CRITICAL_SECTION mLock;
};
#else
typedef ::std::mutex Lock;
#endif

class ScopedLock {
public:
	ScopedLock(Lock& lock) : mLock(lock) {
		mLock.lock();
	}
	~ScopedLock() {
		mLock.unlock();
	}
private:
	Lock& mLock;
};

Lock& getGlobalRendererLock();
}

namespace egl
{
class Error;
class Display;
class Surface;

void SetGlobalError(const Error &error);
EGLint GetGlobalError();

void SetGlobalAPI(EGLenum API);
EGLenum GetGlobalAPI();

void SetGlobalDisplay(Display *dpy);
Display *GetGlobalDisplay();

void SetGlobalDrawSurface(Surface *surface);
Surface *GetGlobalDrawSurface();

void SetGlobalReadSurface(Surface *surface);
Surface *GetGlobalReadSurface();

void SetGlobalContext(gl::Context *context);
gl::Context *GetGlobalContext();

void SetCurDrawSurface(Surface *surface);
Surface *GetCurDrawSurface();

void SetCurReadSurface(Surface *surface);
Surface *GetCurReadSurface();

void SetCurContext(gl::Context *context);
gl::Context *GetCurContext();
}

#endif // LIBGLESV2_GLOBALSTATE_H_
