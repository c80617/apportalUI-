/*
 * Copyright (c) 2013 The MITRE Corporation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this work except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brahma.vmi.covid2019.client;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import brahma.vmi.covid2019.activities.AppRTCActivity;
import brahma.vmi.covid2019.apprtc.CallActivity;
import brahma.vmi.covid2019.common.Constants;
import brahma.vmi.covid2019.performance.PerformanceAdapter;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Request.RequestType;

import static brahma.vmi.covid2019.apprtc.AppRTCClient.vm_platform;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.vm_version;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.resolution_ratio;
/**
 * @file TouchHandler
 * @brief 用於處理使用者之觸控事件並且回傳(x, y)至vm中
 * @author YiWen Li
 * @date 2019/07/12
 **/

/**
 * @author Dave Keppler, Joe Portner
 * Captures touch input events to be sent to a remote Brahma instance.
 */
public class TouchHandler implements Constants {

    private static final String TAG = TouchHandler.class.getName();

    private AppRTCActivity activity;
    private CallActivity callActivity;
    private PerformanceAdapter spi;
    private Point displaySize;
    private float xScaleFactor, yScaleFactor = 0;
    private boolean gotScreenInfo = false;

    public TouchHandler(CallActivity activity, Point displaySize, PerformanceAdapter spi) {
        Log.d(TAG, "TouchHandler");
        this.activity = activity;
        this.callActivity = activity;
        this.displaySize = displaySize;
        this.spi = spi;
    }

    public void sendScreenInfoMessage() {
        Log.d(TAG, "sendScreenInfoMessage");
        BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder();
        msg.setType(RequestType.SCREENINFO);

        activity.sendMessage(msg.build());
    }

    public boolean handleScreenInfoResponse(BRAHMAProtocol.Response msg) {
        Log.d(TAG, "handleScreenInfoResponse");
        if (!msg.hasScreenInfo())
            return false;

        final int x = displaySize.x;
        final int y = displaySize.y;
//        final int x = msg.getScreenInfo().getX();
//        final int y = msg.getScreenInfo().getY();
        Log.d(TAG, "Got the ServerInfo: xsize=" + x + " ; ysize=" + y);
        Log.d(TAG, "Got the ServerInfo: displaySize x=" + displaySize.x + " ; displaySize y=" + displaySize.y);

        if (displaySize.x >= displaySize.y) {
            Log.d(TAG, "x比較長");
            this.xScaleFactor = (float) x / (float) displaySize.y;
            this.yScaleFactor = (float) y / ((float) displaySize.x);
        } else {
            Log.d(TAG, "y比較長");
            this.xScaleFactor = (float) x / (float) displaySize.x;
            this.yScaleFactor = (float) y / ((float) displaySize.y);
        }

        Log.i(TAG, "Scale factor: " + xScaleFactor + " ; " + yScaleFactor);
        gotScreenInfo = true;
        return true;
    }

    public boolean onTouchEvent(final MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        if (!activity.isConnected() || !gotScreenInfo) return false;
        long time = System.currentTimeMillis();
        Log.d(TAG, "TOUCH at: " + time);
        Log.d(TAG, "TOUCH rotation:" + this.callActivity.vmRotation);
        int vmRotation = this.callActivity.vmRotation;
        // increment the touch update count for performance measurement
        spi.incrementTouchUpdates();

        // Create Protobuf message builders
        BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder();
        BRAHMAProtocol.TouchEvent.Builder eventmsg = BRAHMAProtocol.TouchEvent.newBuilder();
        BRAHMAProtocol.TouchEvent.PointerCoords.Builder p = BRAHMAProtocol.TouchEvent.PointerCoords.newBuilder();
        BRAHMAProtocol.TouchEvent.HistoricalEvent.Builder h = BRAHMAProtocol.TouchEvent.HistoricalEvent.newBuilder();
        int eventAction = event.getAction();
//        if(eventAction != 2)
        Log.d(TAG, "eventAction : " + eventAction);
        // Set general touch event information
        eventmsg.setAction(eventAction);
        eventmsg.setDownTime(event.getDownTime());
        eventmsg.setEventTime(event.getEventTime());
        eventmsg.setEdgeFlags(event.getEdgeFlags());

        Log.d(TAG, "TOUCH at: " + time);

        // Loop and set pointer/coordinate information
        final int pointerCount = event.getPointerCount();

        final int vmX = this.callActivity.vmX;
        final int vmY = this.callActivity.vmY;

        if (vm_platform != null) {
            if (true) {
                for (int i = 0; i < pointerCount; i++) {

                    float adjX = -1;
                    float adjY = -1;

                    switch (vmRotation) {
                        case 0:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            Log.i(TAG, "original: adjX: " + adjX + " ; adjY:" + adjY);
                            Log.i(TAG, "Scale factor: " + xScaleFactor + " ; " + yScaleFactor);
                            adjX = adjX * this.xScaleFactor * (float) resolution_ratio;
                            adjY = adjY * this.yScaleFactor * (float) resolution_ratio;
                            Log.i(TAG, "pointerCount: adjX: " + adjX + " ; adjY:" + adjY);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjX);
                            p.setY(adjY);
                            eventmsg.addItems(p.build());
                            break;
                        case 1:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            Log.i(TAG, "original: adjX: " + adjX + " ; adjY:" + adjY);
                            Log.i(TAG, "Scale factor: " + xScaleFactor + " ; " + yScaleFactor);
                            //float newY = adjY - vmX;
                            float newY = vmX - (adjY * this.xScaleFactor);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(newY * (float) resolution_ratio);
                            p.setY((adjX * yScaleFactor) * (float) resolution_ratio);
                            eventmsg.addItems(p.build());
                            break;
                        case 2:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            adjX = adjX * this.xScaleFactor;
                            adjY = adjY * this.yScaleFactor;
                            Log.i(TAG, "pointerCount: adjX: " + adjX + " ; adjY:" + adjY);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjX * (float) resolution_ratio);
                            p.setY(adjY * (float) resolution_ratio);
                            eventmsg.addItems(p.build());
                            break;
                        case 3:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            float newX = vmY - (adjX * yScaleFactor);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjY * xScaleFactor * (float) resolution_ratio);
                            p.setY(newX * (float) resolution_ratio);
                            eventmsg.addItems(p.build());
                            Log.i(TAG, "new adjX: " + p.getX() + " ; new adjY:" + p.getY());
                            break;
                        default:
                            adjX = adjX * this.xScaleFactor;
                            adjY = adjY * this.yScaleFactor;
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjX * (float) resolution_ratio);
                            p.setY(adjY * (float) resolution_ratio);
                            eventmsg.addItems(p.build());
                            Log.i(TAG, "new adjX: " + p.getX() + " ; new adjY:" + p.getY());
                            break;
                    }
                    Log.i(TAG, "adjX eventX: " + p.getX() + " ; eventY:" + p.getY());
                }

                // Loop and set historical pointer/coordinate information
                final int historicalCount = event.getHistorySize();
                for (int i = 0; i < historicalCount; i++) {
                    h.clear();

                    for (int j = 0; j < pointerCount; j++) {
                        float adjX = 0;//1
                        float adjY = 0;
                        Log.i(TAG, "historicalCount: adjX: " + adjX + " ; adjY:" + adjY);

                        switch (vmRotation) {
                            case 1:
                                adjX = event.getHistoricalX(j, i);
                                adjY = event.getHistoricalY(j, i);
                                float newY = vmX - (adjY * this.xScaleFactor);
                                p.clear();
                                p.setId(event.getPointerId(j));
                                p.setX(newY * (float) resolution_ratio);
                                p.setY(adjX * this.yScaleFactor * (float) resolution_ratio);
                                h.addCoords(p.build());
                                break;
                            case 2:
                                break;
                            case 3:
                                adjX = event.getHistoricalX(j, i);
                                adjY = event.getHistoricalY(j, i);
                                float newX = vmY - (adjX * this.yScaleFactor);
                                p.clear();
                                p.setId(event.getPointerId(j));
                                p.setX(adjY * this.xScaleFactor * (float) resolution_ratio);
                                p.setY(newX * (float) resolution_ratio);

                                h.addCoords(p.build());

                                break;
                            case 0:
                            default:
                                adjX = event.getHistoricalX(j, i);
                                adjY = event.getHistoricalY(j, i);
                                p.clear();
                                p.setId(event.getPointerId(j));
                                p.setX(adjX * this.xScaleFactor * (float) resolution_ratio);
                                p.setY(adjY * this.yScaleFactor * (float) resolution_ratio);

                                h.addCoords(p.build());
                                break;
                        }
                    }
                    h.setEventTime(event.getHistoricalEventTime(i));
                    eventmsg.addHistorical(h.build());
                }

            } else {
                Log.d(TAG, "使用原本arm的觸控處理");
                for (int i = 0; i < pointerCount; i++) {

                    float adjX = -1;
                    float adjY = -1;

                    switch (vmRotation) {
                        case 0:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            Log.i(TAG, "original: adjX: " + adjX + " ; adjY:" + adjY);
                            Log.i(TAG, "Scale factor: " + xScaleFactor + " ; " + yScaleFactor);
                            adjX = adjX * this.xScaleFactor;
                            adjY = adjY * this.yScaleFactor;
                            Log.i(TAG, "pointerCount: adjX: " + adjX + " ; adjY:" + adjY);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjX);
                            p.setY(adjY);
                            eventmsg.addItems(p.build());
                            break;
                        case 1:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            Log.i(TAG, "original: adjX: " + adjX + " ; adjY:" + adjY);
                            Log.i(TAG, "Scale factor: " + xScaleFactor + " ; " + yScaleFactor);
                            //float newY = adjY - vmX;
                            float newY = vmX - (adjY * this.xScaleFactor);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(newY);
                            p.setY(adjX * yScaleFactor);
                            eventmsg.addItems(p.build());
                            break;
                        case 2:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            adjX = adjX * this.xScaleFactor;
                            adjY = adjY * this.yScaleFactor;
                            Log.i(TAG, "pointerCount: adjX: " + adjX + " ; adjY:" + adjY);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjX);
                            p.setY(adjY);
                            eventmsg.addItems(p.build());
                            break;
                        case 3:
                            adjX = event.getX(i);
                            adjY = event.getY(i);
                            float newX = vmY - (adjX * yScaleFactor);
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjY * xScaleFactor);
                            p.setY(newX);
                            eventmsg.addItems(p.build());
                            Log.i(TAG, "new adjX: " + p.getX() + " ; new adjY:" + p.getY());
                            break;
                        default:
                            adjX = adjX * this.xScaleFactor;
                            adjY = adjY * this.yScaleFactor;
                            p.clear();
                            p.setId(event.getPointerId(i));
                            p.setX(adjX);
                            p.setY(adjY);
                            eventmsg.addItems(p.build());
                            Log.i(TAG, "new adjX: " + p.getX() + " ; new adjY:" + p.getY());
                            break;
                    }
                    Log.i(TAG, "adjX eventX: " + p.getX() + " ; eventY:" + p.getY());
                }

                // Loop and set historical pointer/coordinate information
                final int historicalCount = event.getHistorySize();
                for (int i = 0; i < historicalCount; i++) {
                    h.clear();

                    for (int j = 0; j < pointerCount; j++) {
                        float adjX = 0;//1
                        float adjY = 0;
                        Log.i(TAG, "historicalCount: adjX: " + adjX + " ; adjY:" + adjY);

                        switch (vmRotation) {

                            case 1:
                                adjX = event.getHistoricalX(j, i);
                                adjY = event.getHistoricalY(j, i);
                                float newY = vmX - (adjY * this.xScaleFactor);
                                p.clear();
                                p.setId(event.getPointerId(j));
                                p.setX(newY);
                                p.setY(adjX * this.yScaleFactor);
                                h.addCoords(p.build());
                                break;
                            case 2:
                                break;
                            case 3:
                                adjX = event.getHistoricalX(j, i);
                                adjY = event.getHistoricalY(j, i);
                                float newX = vmY - (adjX * this.yScaleFactor);
                                p.clear();
                                p.setId(event.getPointerId(j));
                                p.setX(adjY * this.xScaleFactor);
                                p.setY(newX);

                                h.addCoords(p.build());

                                break;
                            case 0:
                            default:
                                adjX = event.getHistoricalX(j, i);
                                adjY = event.getHistoricalY(j, i);
                                p.clear();
                                p.setId(event.getPointerId(j));
                                p.setX(adjX * this.xScaleFactor);
                                p.setY(adjY * this.yScaleFactor);

                                h.addCoords(p.build());
                                break;
                        }
                    }
                    h.setEventTime(event.getHistoricalEventTime(i));
                    eventmsg.addHistorical(h.build());
                }


            }
        }

        // Add Request wrapper around touch event
        msg.setType(RequestType.TOUCHEVENT);
        msg.addTouch(eventmsg);

        Log.d("getEventAction", msg.build().toString());

        // Send touch event to VM
        activity.sendMessage(msg.build());

        return true;
    }
}


