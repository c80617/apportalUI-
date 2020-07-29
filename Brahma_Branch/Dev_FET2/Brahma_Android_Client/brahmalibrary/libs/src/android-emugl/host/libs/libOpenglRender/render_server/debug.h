// Copyright (C) 2018 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#pragma once

#define DEBUG 0

#if DEBUG
#define DDDLN(...) printf(__VA_ARGS__), printf("\n"), fflush(stdout)
#define DDD(...) printf(__VA_ARGS__), fflush(stdout)
#else
#define DDDLN(...) ((void)0)
#define DDD(...) ((void)0)
#endif
