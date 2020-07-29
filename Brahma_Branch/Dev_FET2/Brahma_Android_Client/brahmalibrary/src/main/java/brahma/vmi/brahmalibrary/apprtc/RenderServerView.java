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

package brahma.vmi.brahmalibrary.apprtc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class RenderServerView extends SurfaceView {
    //public class RenderServerView extends GLSurfaceView {
    private static String TAG = "RenderServerView";

    public RenderServerView(Context context) {
        super(context);
        init(false, 0, 0);
    }

    // Both parameters are used for inflating the View
    public RenderServerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(false, 0, 0);
    }

    public RenderServerView(Context context, boolean translucent, int depth, int stencil) {
        super(context);
        init(translucent, depth, stencil);
    }

    private void init(boolean translucent, int depth, int stencil) {
        // TODO: initialize anythin here
        // Maybe prepare a SurfaceView to native EMUGL engine to disply content...
    }
}
