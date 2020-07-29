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

#include "OpenglRender/render_api.h"

int initialize_render_server(int displayWidth, int displayHeight);
int start_render_server(FBNativeWindowType rootWindowId,
                        EGLNativeWindowType subWindow,
                        int winWidth, int winHeight);
int adjust_render_server(int winWidth, int winHeight);