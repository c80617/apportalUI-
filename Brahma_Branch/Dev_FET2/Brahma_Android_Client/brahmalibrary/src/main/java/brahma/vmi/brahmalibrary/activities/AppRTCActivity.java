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

// Derived from AppRTCActivity from the libjingle / webrtc AppRTCDemo
// example application distributed under the following license.
/*
 * libjingle
 * Copyright 2013, Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package brahma.vmi.brahmalibrary.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONObject;
import org.webrtc.MediaConstraints;

import java.util.Timer;
import java.util.TimerTask;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.apprtc.AppRTCClient;
import brahma.vmi.brahmalibrary.apprtc.AppRTCHelper;
import brahma.vmi.brahmalibrary.apprtc.CallActivity;
import brahma.vmi.brahmalibrary.apprtc.MessageHandler;
import brahma.vmi.brahmalibrary.client.VirtualKeyboardHandler;
import brahma.vmi.brahmalibrary.common.ConnectionInfo;
import brahma.vmi.brahmalibrary.common.Constants;
import brahma.vmi.brahmalibrary.common.DatabaseHandler;
import brahma.vmi.brahmalibrary.common.StateMachine.STATE;
import brahma.vmi.brahmalibrary.common.StateObserver;
import brahma.vmi.brahmalibrary.common.Utility;
//import brahma.vmi.brahmalibrary.performance.PerformanceAdapter;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Request;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Response;
import brahma.vmi.brahmalibrary.services.SessionService;
import brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity;
import brahma.vmi.brahmalibrary.wcitui.GridActivity;

import static brahma.vmi.brahmalibrary.apprtc.CallActivity.RESULTCODE_CLOSECALL;


/**
 * @developer Ian
 * Base activity for establishing an AppRTC connection
 */
public class AppRTCActivity extends Activity implements StateObserver, MessageHandler, Constants {

    private static final String TAG = AppRTCActivity.class.getName();
    public static boolean OpenAppMode = false;
    private static boolean OpenCamera = false;
    private static STATE myState;
    final long downTime = SystemClock.uptimeMillis();
    final MotionEvent downEvent = MotionEvent.obtain(
            downTime, downTime, MotionEvent.ACTION_DOWN, 30, 20, 0);
    final MotionEvent upEvent = MotionEvent.obtain(
            downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 30, 20, 0);
    // Synchronize on quit[0] to avoid teardown-related crashes.
    private final Boolean[] quit = new Boolean[]{false};
    public boolean getStream = false;
    protected AppRTCClient appRtcClient;
//    protected PerformanceAdapter performanceAdapter;
    protected DatabaseHandler dbHandler;
    protected ConnectionInfo connectionInfo;
    protected boolean proxying = false; // if this is true, we have finished the handshakes and the connection is running
    //宣告Timer及TimerTask
    protected Timer timer;
    protected TimerTask timerTask;
    int times = 0;
    private boolean bound = false;
    private VirtualKeyboardHandler kbHandler;
    //    private ProgressDialog pd;
    private Toast logToast;
    private Timer timeoutTimer;
    private TimerTask timeoutTimerTask;
    private Timer progressTimer;
    private TimerTask progressTask;
    private AlertDialog progressBarDialog;
    private ProgressBar progressBar;
    private TextView tv_progress;
    private AlertDialog interrupt_dialog;
    private Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    progressBar.incrementProgressBy(10);
                    progressBar.incrementSecondaryProgressBy(20);
                    tv_progress.setText(progressBar.getProgress() + " %");
                    break;
                case 1:
                    progressBar.setProgress(msg.arg1);
                    progressBar.setSecondaryProgress(msg.arg1 + 15);
                    tv_progress.setText(progressBar.getProgress() + " %");

                    if (progressBarDialog != null)
                        progressBarDialog.setTitle(msg.obj.toString());
                    break;
                case 2:
                    interrupt_dialog.show();
                    break;
            }
        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d("LifeCycle", "AppRTCActivity onServiceConnected!!!");
            // We've bound to SessionService, cast the IBinder and get SessionService instance
            appRtcClient = (AppRTCClient) iBinder;
//            performanceAdapter.setPerformanceData(appRtcClient.getPerformance());
            bound = true;

            // after we have bound to the service, begin the connection
            appRtcClient.connectToRoom(AppRTCActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    private AlertDialog end_dialog;
    private Context context;
    private Button button;

    public static void setOpenCamera(boolean value) {
        OpenCamera = value;
    }

    public void showDialog() {
        Message msg = new Message();
        msg.what = 2;
        progressHandler.sendMessage(msg);

    }

    public boolean getFlagRemoteLandscape() {
        boolean isRemoteLandscape = true;
        isRemoteLandscape = Utility.getPrefBool(this, R.string.preferenceKey_connection_useLandscape, R.string.preferenceValue_connection_useLandscape);
        return isRemoteLandscape;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycle", "AppRTCActivity onCreate!!!");
        context = this;
        setRequestedOrientation(getDeviceDefaultOrientation());
        dbHandler = new DatabaseHandler(this);
        kbHandler = new VirtualKeyboardHandler(this);
//        performanceAdapter = new PerformanceAdapter();
//
        // Create ProgressBar Dialog
        View progressView = LayoutInflater.from(AppRTCActivity.this).inflate(R.layout.dialog_progressbar, null);
        progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
        tv_progress = (TextView) progressView.findViewById(R.id.tv_progress);
        button = (Button)progressView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(context, GridActivity.class);
                startActivity(intent);
            }
        });
        progressBar.setProgress(0);
        progressBar.setSecondaryProgress(10);


        progressTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                progressHandler.sendMessage(msg);
            }
        };

        MaterialAlertDialogBuilder progressBuilder = new MaterialAlertDialogBuilder(AppRTCActivity.this);
        progressBuilder.setTitle(getResources().getString(R.string.appRTC_toast_connection_start));
        progressBuilder.setView(progressView);

//        progressBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                Intent intent = new Intent();
//                intent.setClass(context, GridActivity.class);
//                startActivity(intent);
////                finish();
//            }
//        });
        progressBarDialog = progressBuilder.create();
//        progressBarDialog.setCancelable(false);
        progressBarDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // 关闭 Dialog
                dialog.dismiss();
                // 关闭当前 Activity
                finish();
            }
        });

        View logoutView = LayoutInflater.from(AppRTCActivity.this).inflate(R.layout.dialog_message, null);
        TextView message_tv = (TextView) logoutView.findViewById(R.id.message_tv);
        message_tv.setText(getResources().getString(R.string.interrupt_Connect2));
        MaterialAlertDialogBuilder interruptBuilder = new MaterialAlertDialogBuilder(AppRTCActivity.this);

        interruptBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //send interrupt request
                appRtcClient.sendInterrupt();
            }
        });
        interruptBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent();
                setResult(RESULTCODE_CLOSECALL, intent);
                OpenAppMode = false;
                finish();
            }
        });

        interruptBuilder.setTitle(R.string.interrupt_Connect);
        interruptBuilder.setView(logoutView);
        interrupt_dialog = interruptBuilder.create();

        View endView = LayoutInflater.from(AppRTCActivity.this).inflate(R.layout.dialog_message, null);
        TextView message_tv2 = (TextView) endView.findViewById(R.id.message_tv);
        message_tv2.setText(getResources().getString(R.string.connection_end));
//        Button endButton = (Button)endView.findViewById(R.id.button);
//        endButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(AppRTCActivity.this, BrahmaMainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
        MaterialAlertDialogBuilder endBuilder = new MaterialAlertDialogBuilder(AppRTCActivity.this);

        endBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //go to BrahmaMainActivity
                Intent intent = new Intent(AppRTCActivity.this, BrahmaMainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        endBuilder.setTitle(R.string.interrupt_Connect);
        endBuilder.setView(endView);
        end_dialog = endBuilder.create();

        // Get info passed to Intent
        final Intent intent = getIntent();
        connectionInfo = dbHandler.getConnectionInfo(intent.getIntExtra("connectionID", 0));
        String mode = intent.getStringExtra("mode");
        Log.d(TAG, "mode >>>>> " + mode);
        if (connectionInfo != null)
            connectToRoom();
        else
            logAndToast(R.string.appRTC_toast_connection_notFound);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    // returns what value we should request for screen orientation, either portrait or landscape
    private int getDeviceDefaultOrientation() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Configuration config = getResources().getConfiguration();
        assert windowManager != null;
        int rotation = windowManager.getDefaultDisplay().getRotation();

        int value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT))
            value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        return value;
    }

    // called from PCObserver
    public MediaConstraints getPCConstraints() {
        MediaConstraints value = null;
        if (appRtcClient != null)
            value = appRtcClient.getSignalingParams().pcConstraints;
        return value;
    }

    public void changeToErrorState(String s) {
        Log.d(TAG, "AppRTCActivity changeToErrorState");
        if (appRtcClient != null)
            appRtcClient.changeToErrorState(s);
    }

    protected void connectToRoom() {
        Log.d("LifeCycle", "AppRTCActivity connect to room!!!!");
//        logAndToast(R.string.appRTC_toast_connection_start);
//        setProgressDegree(0, getResources().getString(R.string.progressBar_0));
        startProgressDialog();//開始轉圈圈
        bindService(new Intent(this, SessionService.class), serviceConnection, 0);//啟動service
    }

    protected void startProgressDialog() {
        progressBarDialog.show();
        startProgressTimer();
    }

    public void stopProgressDialog() {
        if (progressBarDialog != null) {
            progressBarDialog.dismiss();
            progressBarDialog = null;
        }
        stopProgressTimer();
    }

    public void startProgressTimer() {
        Log.d(TAG, "startProgressTimer");
        progressTimer = new Timer();
//        progressTimer.schedule(progressTask, 0, 500);
    }

    public void stopProgressTimer() {
        Log.d(TAG, "stopProgressTimer");
        if (progressTimer != null) {
            progressTimer.cancel();
        }
    }

    protected boolean checkOpenCamera() {
        return OpenCamera;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Ian-proxying= " + proxying);
        Log.d("LifeCycle", "AppRTCActivity onPause");
        Log.d("LifeCycle", "OpenAppMode >>>>> " + OpenAppMode);
        if (OpenCamera) {
            Log.d("LifeCycle", "OpenCamera true");
            //do nothing
//        } else if (openVMI) {
        } else if (OpenAppMode) {
            Log.d("LifeCycle", "testValue true");
            //do not disconnect
        } else if (proxying) {
            Log.d("LifeCycle", "proxying true");
            disconnectAndExit();
        }
    }

    // Log |msg| and Toast about it.
    public void logAndToast(final int resID) {
        Log.d(TAG, getResources().getString(resID));
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (logToast != null) {
                    logToast.cancel();
                }
                logToast = Toast.makeText(AppRTCActivity.this, resID, Toast.LENGTH_LONG);
                logToast.show();
            }
        });
    }

    // called from PCObserver, SDPObserver, RotationHandler, and TouchHandler
    public void sendMessage(Request msg) {
        Log.d(TAG, "send Message:" + msg.toString());
        if (appRtcClient != null) {
            appRtcClient.sendMessage(msg);
        }
    }

    // MessageHandler interface method
    // Called when the client connection is established
    public void onOpen() {
        proxying = true;
//        logAndToast(R.string.appRTC_toast_clientHandler_start);
        setProgressDegree(80, getResources().getString(R.string.progressBar_80));
    }

    // MessageHandler interface method
    // Called when a message is sent from the server, and the SessionService doesn't consume it
    public boolean onMessage(Response data) {
        if (data.getType() == Response.ResponseType.AUTH) {
            if (data.hasAuthResponse()) {
                //setResponse(data);
                if (data.getAuthResponse().getType() == BRAHMAProtocol.AuthResponse.AuthResponseType.SESSION_MAX_TIMEOUT) {
                    needAuth(R.string.brahmaActivity_toast_sessionMaxTimeout, false);
                }
            }
        } else {
            Log.e(TAG, "Unexpected protocol message of type " + data.getType().name());
        }
        return true;
    }

    // when authentication fails, or a session maxTimeout or idleTimeout message is received, stop the
    // AppRTCActivity, close the connection, and cause the ConnectionList activity to reconnect to this
    // connectionID
    public void needAuth(int messageResID, boolean passwordChange) {
        // clear timed out session information from memory
        dbHandler.clearSessionInfo(connectionInfo);
        // send a result message to the calling activity so it will show the authentication dialog again
        Intent intent = new Intent();
        intent.putExtra("connectionID", connectionInfo.getConnectionID());
        if (messageResID > 0)
            logAndToast(messageResID);
        setResult(passwordChange ? BrahmaMainActivity.RESULT_NEEDPASSWORDCHANGE : BrahmaMainActivity.RESULT_NEEDAUTH, intent);

        disconnectAndExit();
    }


    // Disconnect from remote resources, dispose of local resources, and exit.
    public void disconnectAndExit() {
        Log.d("LifeCycle", "appRTC disconnectAndExit");
        SessionService.sendFileSyncBackRequestStatic();
        try {
            while (!SessionService.isWaitingListEmptyStatic()) {
                Thread.sleep(500);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            // do nothing
        }
        proxying = false;
        synchronized (quit[0]) {
            if (quit[0]) {
                return;
            }
            quit[0] = true;

            // allow child classes to clean up their components
            onDisconnectAndExit();

            // Unbind from the service
            if (bound) {
                if (SessionService.getState() == STATE.RUNNING) {
                    JSONObject json = new JSONObject();
                    AppRTCHelper.jsonPut(json, "type", "bye");
                    try {
                        if (CallActivity.keyboard_shown) {
                            kbHandler.sendVKeyboardReqMessage(VirtualKeyboardHandler.TYPE_KEYCODE, 4);
                        }
                        appRtcClient.sendMessage(AppRTCHelper.makeWebRTCRequest(json));
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                unbindService(serviceConnection);
                bound = false;
                if (appRtcClient != null) {
                    appRtcClient.disconnectFromRoom();
                }
//                //提前關閉websocket
//                appRtcClient.disconnect();

                appRtcClient = null;
//                performanceAdapter.clearPerformanceData();
            }

            stopProgressDialog(); // prevent resource leak if we disconnect while the progress dialog is still up

            // if the useBackground preference is unchecked, stop the session service before finishing
            boolean useBackground = Utility.getPrefBool(this, R.string.preferenceKey_connection_useBackground, R.string.preferenceValue_connection_useBackground);
            if (!useBackground)
                stopService(new Intent(this, SessionService.class));

            if (!isFinishing()) {
                finish();
            }
        }

    }

    // override in child classes
    protected void onDisconnectAndExit() {
    }

    public boolean isConnected() {
        return proxying;
    }


    public void onStateChange(STATE oldState, STATE newState, int resID, String result) {
        Log.d("LifeCycle", "onStateChange newState >>>>> " + newState);
        myState = newState;
        boolean exit = false;
        switch (newState) {
            case CONNECTED:
                Intent intent = new Intent("STATE_RECONNECT");
                intent.putExtra("myState", newState.toString());
                intent.putExtra("reConnectTimes", 6);
                intent.putExtra("type", AppRTCClient.type);
                sendBroadcast(intent);
                break;
            case AUTH:
                break;
            case RUNNING:
                break;
            case ERROR:
                // we are in an error state, check the previous state and act appropriately
                switch (oldState) {
                    case STARTED: // failed to authenticate and transition to AUTH
                    case CONNECTED: // failed to receive ready message and transition to RUNNING (can fail auth to proxy)
                        break;
                    case AUTH: // failed to connect the WebSocket and transition to CONNECTED
                        // the socket connection failed, display the failure message and return to the connection list
                        break;
                    case RUNNING: // failed after already running
                        break;
                }
                exit = true;
                break;
            case TEST:
                exit = true;
                break;
            default:
                break;
        }

        // if the state change included a message, log it and display a toast popup message
//        if (resID > 0 && !quit[0])
//            logAndToast(resID);

        if (exit)
            // finish this activity and return to the connection list
            disconnectAndExit();
    }


    public void connectionTimeout() {
        Log.d(TAG, "connectionTimeout");
        startTimeoutTimer();
    }

    private void startTimeoutTimer() {
        Log.d(TAG, "startTimeoutTimer");
        initTimeOutTimerTask();
        timeoutTimer = new Timer();
        timeoutTimer.schedule(timeoutTimerTask, 10000, 1000000);
    }

    private void initTimeOutTimerTask() {
        timeoutTimerTask = new TimerTask() {
            public void run() {
                Log.d(TAG, "initTimeOutTimerTask");
                logAndToast(R.string.appRTC_toast_login_failed);
                disconnectAndExit();
                stopTimeoutTimer();
            }
        };
    }

    public void stopTimeoutTimer() {
        Log.d(TAG, "stopTimeoutTimer");
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }
    }


    public void reconnect() {
        Log.d(TAG, "reconnect");
        startTimer();//一次startTimer就會執行5次重新連線
    }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 6000);
    }

    public void initializeTimerTask() {
        times = 0;
        Log.d("Reconnect", "Timer: initializeTimerTask");

        timerTask = new TimerTask() {
            public void run() {
                Log.d("Reconnect", "initializeTimerTask :" + times + " ,myState:" + myState);
                Log.d("Reconnect", "initializeTimerTask >>>>> " + AppRTCClient.auto_connect);
                if (times <= 5 && myState == STATE.ERROR) {
                    times++;
                    if (AppRTCClient.auto_connect) {
                        Intent intent = new Intent("STATE_RECONNECT");
                        intent.putExtra("myState", myState.toString());
                        intent.putExtra("reConnectTimes", times);
                        intent.putExtra("type", AppRTCClient.type);
                        intent.putExtra("auto_connect", AppRTCClient.auto_connect);
                        sendBroadcast(intent);
                        Log.d("Reconnect", "send broadcast");
                    } else {
                        times = 6;
                    }
                } else {
                    Log.d("Reconnect", "do nothing");
                    this.cancel();
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
            times = 0;
        }
    }

    public void setProgressDegree(int progress, String text) {
        Log.d(TAG,"setProgressDegree:"+progress);
        if(progress == 0){
            button.setEnabled(true);
        }else{
            button.setEnabled(false);
        }
        Message msg = new Message();
        msg.what = 1;
        msg.obj = text;
        msg.arg1 = progress;
        progressHandler.sendMessage(msg);
    }
}
