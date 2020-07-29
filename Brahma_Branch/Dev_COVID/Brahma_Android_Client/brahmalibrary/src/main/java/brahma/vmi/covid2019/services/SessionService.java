/*
 Copyright (c) 2013 The MITRE Corporation, All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this work except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package brahma.vmi.covid2019.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.apprtc.AppRTCClient;
import brahma.vmi.covid2019.apprtc.MessageHandler;
import brahma.vmi.covid2019.client.IntentHandler;
import brahma.vmi.covid2019.client.LocationHandler;
import brahma.vmi.covid2019.client.NotificationHandler;
import brahma.vmi.covid2019.client.SensorHandler;
import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.common.Constants;
import brahma.vmi.covid2019.common.DatabaseHandler;
import brahma.vmi.covid2019.common.FileHandler;
import brahma.vmi.covid2019.common.StateMachine;
import brahma.vmi.covid2019.common.StateMachine.STATE;
import brahma.vmi.covid2019.common.StateObserver;
import brahma.vmi.covid2019.common.Utility;
import brahma.vmi.covid2019.performance.PerformanceAdapter;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.AuthResponse.AuthResponseType;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Response;


/**
 * @file SessionService
 * @brief 用於執行於背景所必要之service
 * @author YiWen Li
 * @date 2019/07/12
 **/

/**
 * @developer Ian
 * An activity should use this service as follows:
 * 1. If the state is not NEW and the connectionID is different, stop the service
 * 2. Start the service (so it doesn't stop on unbind)
 * 3. Bind to the service
 */
public class SessionService extends Service implements StateObserver, MessageHandler, SensorEventListener, Constants {
    private static final String TAG = SessionService.class.getName();
    private static final int NOTIFICATION_ID = 0;

    // only one service is started at a time, acts as a singleton for static getters
    private static SessionService service;
    Handler mHanlder2;
    // local variables
    private AppRTCClient binder; // Binder given to clients
    private StateMachine machine;
    private PerformanceAdapter performanceAdapter;
    private NotificationManager notificationManager;
    private Handler handler;
    private DatabaseHandler databaseHandler;
    private ConnectionInfo connectionInfo;
    private boolean keepNotification;
    // client components
    private LocationHandler locationHandler;
    private SensorHandler sensorHandler;
    private List<String> forwardedFiles; // files which have been forwarded
    private Set<String> waitingFiles;  // files waiting for sync back

    // public getters for state and connectionID (used by activities)
    public static STATE getState() {
        STATE value = STATE.NEW;
        if (service != null && service.machine != null)
            value = service.machine.getState();
        return value;
    }

    public static int getConnectionID() {
        int value = 0;
        if (service != null && service.connectionInfo != null)
            value = service.connectionInfo.getConnectionID();
        return value;
    }

    public static boolean isRunningForConn(int connectionID) {
        return getConnectionID() == connectionID && getState() != STATE.NEW;
    }

    public static void sendMessageStatic(BRAHMAProtocol.Request request) {
        if (service != null && service.connectionInfo != null)
            service.sendMessage(request);
    }

    public static void recordFilesStatic(String fileName) {
        if (service != null && service.connectionInfo != null)
            service.recordFiles(fileName);
    }

    public static void sendFileSyncBackRequestStatic() {
        if (service != null && service.connectionInfo != null) {
            service.sendFileSyncBackRequest();
        }
    }

    public static boolean isWaitingListEmptyStatic() {
        if (service != null && service.connectionInfo != null) {
            return service.isWaitingListEmpty();
        }
        return true;
    }

    public static void removeFromWaitingListStatic(String fileName) {

        if (service != null && service.connectionInfo != null) {
            service.removeFromWaitingList(fileName);
        }
    }

    public void recordFiles(String fileName) {
        forwardedFiles.add(fileName);
    }

    public void sendFileSyncBackRequest() {

        waitingFiles.clear();
        for (String fileName : forwardedFiles) {
            final BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder();
            msg.setType(BRAHMAProtocol.Request.RequestType.FILE_REQUEST);
            BRAHMAProtocol.FileRequest.Builder fileRequest = BRAHMAProtocol.FileRequest.newBuilder();
            fileRequest.setFilename(fileName);
            msg.setFileRequest(fileRequest);
            sendMessage(msg.build());
            waitingFiles.add(fileName);
        }
        forwardedFiles.clear();
    }

    public boolean isWaitingListEmpty() {
        return waitingFiles.isEmpty();
    }

    public void removeFromWaitingList(String fileName) {
        waitingFiles.remove(fileName);
    }

    @Override
    public void onCreate() {
        Log.d("LifeCycle", "SessionService onCreate!!!");
        service = this;
        machine = new StateMachine();
        performanceAdapter = new PerformanceAdapter();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        handler = new Handler();
        forwardedFiles = new ArrayList<String>();
        waitingFiles = new HashSet<String>();

        //start to send ping
//        long endDate = System.currentTimeMillis(); // immediately get end date
//        mHanlder2 = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (data.hasPingResponse())
//                    performanceAdapter.setPing(data.getPingResponse().getStartDate(), endDate, databaseHandler);
//                super.handleMessage(msg);
//            }
//        };
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                mHanlder2.sendEmptyMessage(1);//通知UI更新
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule(timerTask,0,5000);//周期为1秒

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LifeCycle", "SessionService onStartCommand!!!");
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopSelf();
        } else if (getState() == STATE.NEW) {
            // change state and get connectionID from intent
            machine.setState(STATE.STARTED, 0, null);
            int connectionID = intent.getIntExtra("connectionID", 0);

            // begin connecting to the server
            startup(connectionID);
        }

        return START_NOT_STICKY; // run until explicitly stopped.
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, String.format("onBind (state: %s)", getState()));
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d("LifeCycle", "SessionService onDestroy!!!");
        // before we destroy this service, shut down its components
        shutdown();
        super.onDestroy();
    }

    private void startup(int connectionID) {
        Log.i(TAG, "Starting background service. connectionID = " + connectionID);

        // connect to the database
        databaseHandler = new DatabaseHandler(this);

        // get connection information from database
        connectionInfo = databaseHandler.getConnectionInfo(connectionID);
        Log.i(TAG, "connectionInfo = " + connectionInfo.getConnectionID() + ", Description = " + connectionInfo.getDescription());
        // create binder object
        binder = new AppRTCClient(this, machine, connectionInfo);

        // attach the performance adapter to the binder's performance data objects
        performanceAdapter.setPerformanceData(binder.getPerformance());

        // create a location handler object
        locationHandler = new LocationHandler(this);

        // create a sensor handler object
        sensorHandler = new SensorHandler(this, performanceAdapter);

        // show notification
        //showNotification(true);
    }

    private void shutdown() {
        Log.i(TAG, "Shutting down background service.");
        // reset singleton
        service = null;

        // hide notification
        hideNotification();

        // Unconnected
        //sendUnconnectedRequest();

        // clean up location updates
        if (locationHandler != null)
            locationHandler.cleanupLocationUpdates();

        // clean up sensor updates
        if (sensorHandler != null)
            sensorHandler.cleanupSensors();

        // disconnect from the database
        if (databaseHandler != null)
            databaseHandler.close();

        // try to disconnect the client object
        performanceAdapter.clearPerformanceData();
        if (binder != null) {
            binder.disconnect();
            binder = null;
        }
    }

    private void sendUnconnectedRequest() {
        Log.d(TAG, "sendUnconnectedRequest()");
        BRAHMAProtocol.Unconnected.Builder ucBuilder = BRAHMAProtocol.Unconnected.newBuilder();
        ucBuilder.setUnconnected("Disconnected!!!");

        BRAHMAProtocol.Request.Builder rBuilder = BRAHMAProtocol.Request.newBuilder();
        rBuilder.setType(BRAHMAProtocol.Request.RequestType.UNCONNECTED);
        rBuilder.setUnconnectedRequest(ucBuilder);
        sendMessage(rBuilder.build());
    }

    private void hideNotification() {
        // hide the notification if we aren't supposed to keep it past the service life
        if (!keepNotification)
            notificationManager.cancel(NOTIFICATION_ID);
    }

    public void onStateChange(STATE oldState, STATE newState, int resID, String result) {
        if (newState == STATE.ERROR)
            stopSelf();
    }

    // Google AppEngine message handler method
    @Override
    public void onOpen() {
        Log.d("LifeCycle", "SessionService onOpen!!!");
        boolean locationResult = locationHandler.initLocationUpdates();
        if (!locationResult)
            doToast(R.string.callactivity_locationHandler_initFailed);
        sensorHandler.initSensors(); // start forwarding sensor data
    }

    // Google AppEngine message handler method
    // Handler for receiving Brahma protocol messages and dispatching them appropriately
    // Returns true if the message is consumed, false if it is not
    @Override
    public boolean onMessage(Response data) {
        boolean consumed = true;
        switch (data.getType()) {
            case AUTH:
                AuthResponseType type = data.getAuthResponse().getType();
                if (type == AuthResponseType.SESSION_MAX_TIMEOUT) {

                    // if we are using the background service preference, change the notification icon to indicate that the connection has been halted
                    boolean useBackground = Utility.getPrefBool(this, R.string.preferenceKey_connection_useBackground, R.string.preferenceValue_connection_useBackground);
                    if (useBackground) {
                        keepNotification = true;
                        //showNotification(false);
                    }

                    // the activity isn't running...
                    if (!binder.isBound()) {
                        // clear timed out session information from memory
                        databaseHandler.clearSessionInfo(connectionInfo);

                        // create a toast
                        doToast(R.string.brahmaActivity_toast_sessionMaxTimeout);
                    }
                }
            case SCREENINFO:
                Log.d(TAG, "SessionService SCREENINFO");
            case WEBRTC:
                consumed = false; // pass this message on to the activity message handler
                break;
            case VKeyboardInfo:
                consumed = false; // pass this message on to the activity message handler
                break;
            case LOCATION:
                locationHandler.handleLocationResponse(data);
                break;
            // This is an ACK to the video STOP request.
            case INTENT:
                // handler is needed, we might create a toast from a background thread
                final Response finalData = data;
                Log.d(TAG, "INTENT:" + finalData.getIntent().getAction());
                handler.post(new Runnable() {
                    public void run() {
                        IntentHandler.inspect(finalData, SessionService.this);
                    }
                });
                break;
            case NOTIFICATION:
                NotificationHandler.inspect(data, SessionService.this, getConnectionID());
                break;
            case PING:
                long endDate = System.currentTimeMillis(); // immediately get end date
                if (data.hasPingResponse())
                    performanceAdapter.setPing(data.getPingResponse().getStartDate(), endDate, databaseHandler);
                break;
            case APPS:
                consumed = false; // pass this message on to the activity message handler
                break;
            case FILE:
                FileHandler.inspect(data, SessionService.this);
                break;
            case UNCONNECTED:
                machine.setState(STATE.AUTOCLOSE,0,null);
                //未來for中科院 斷線功能
                Log.d(TAG, "UNCONNECTED >>>>> "+data.getType());
                AppRTCClient.auto_connect = false;
                //response
                BRAHMAProtocol.Unconnected.Builder ucBuilder = BRAHMAProtocol.Unconnected.newBuilder();
                ucBuilder.setUnconnected("Disconnected!!!");
//                狀態修改為不要重新連線
                Log.d(TAG, "AppRTCClient.auto_connect >>>>> "+AppRTCClient.auto_connect);
                BRAHMAProtocol.Request.Builder rBuilder = BRAHMAProtocol.Request.newBuilder();
                rBuilder.setType(BRAHMAProtocol.Request.RequestType.UNCONNECTED);
                rBuilder.setUnconnectedRequest(ucBuilder);
                sendMessage(rBuilder.build());
                break;
            default:
                Log.e(TAG, "Unexpected protocol message of type " + data.getType().name());
        }
        return consumed;
    }

    // used by LocationHandler and SensorHandler to send messages
    public void sendMessage(BRAHMAProtocol.Request request) {
        if (binder != null)
            binder.sendMessage(request);
    }

    // Bridge the SensorEventListener callbacks to the SensorHandler
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (getState() == STATE.RUNNING)
            sensorHandler.onAccuracyChanged(sensor, accuracy);
    }

    // Bridge the SensorEventListener callbacks to the SensorHandler
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (getState() == STATE.RUNNING)
            sensorHandler.onSensorChanged(event);
    }

    private void doToast(final int resID) {
        // handler is needed to create a toast from a background thread
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(SessionService.this, resID, Toast.LENGTH_LONG).show();
            }
        });
    }
}
