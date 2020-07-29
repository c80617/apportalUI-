/*
 * Copyright (c) 2014 The MITRE Corporation, All Rights Reserved.
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

package brahma.vmi.covid2019.apprtc;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.activities.AppRTCActivity;
import brahma.vmi.covid2019.auth.AuthData;
import brahma.vmi.covid2019.autobahn.WebSocket;
import brahma.vmi.covid2019.autobahn.WebSocketConnection;
import brahma.vmi.covid2019.autobahn.WebSocketException;
import brahma.vmi.covid2019.autobahn.WebSocketOptions;
import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.common.Constants;
import brahma.vmi.covid2019.common.DatabaseHandler;
import brahma.vmi.covid2019.common.SessionInfo;
import brahma.vmi.covid2019.common.StateMachine;
import brahma.vmi.covid2019.common.StateMachine.STATE;
import brahma.vmi.covid2019.net.EasySSLSocketFactory;
import brahma.vmi.covid2019.net.SSLConfig;
import brahma.vmi.covid2019.net.SSLContextUtil;
import brahma.vmi.covid2019.netspeed.RoleData;
import brahma.vmi.covid2019.performance.PerformanceTimer;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Request;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Response;
import brahma.vmi.covid2019.services.SessionService;
import brahma.vmi.covid2019.wcitui.devicelog.DeviceInfo;

import static android.icu.lang.UCharacter.toLowerCase;
import static brahma.vmi.covid2019.firebase.MyFirebaseMessagingService.registration_id;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity._login;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.context;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.device_id;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.jsonResponse;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalPort;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.resolution;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.responseCode;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Latitude;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Longitude;

/**
 * @developer Ian
 * <p>
 * Negotiates signaling for chatting with apprtc.appspot.com "rooms".
 * Uses the client<->server specifics of the apprtc AppEngine webapp.
 * <p>
 * Now extended to act as a Binder object between a Service and an Activity.
 * <p>
 * To use: create an instance of this object (registering a message handler) and
 * call connectToRoom().  Once that's done call sendMessage() and wait for the
 * registered handler to be called with received messages.
 */
public class AppRTCClient extends Binder implements Constants {
    private static final String TAG = "AppRTCClient";
    public static boolean useSSL;
    public static SSLConfig sslConfig;
    public static SSLContextUtil ssl;
    public static String type = "";//vmi or appstreaming
    public static String deviceID = "";
    public static String token;
    public static String performanceToken;
    public static String brahmaUsername;
    public static String role;
    public static String vm_platform;
    public static String vm_version = "7.0.0";
    public static String vm_ip;
    public static String user_id;
    public static JSONObject appListObject;

    //Overseer return value
    public static String has_notification;
    public static boolean auto_connect = true;
    DeviceInfo deviceInfo = new DeviceInfo();
    Boolean reconn = false;
    String device = "";//devices name
    String os = "";//device's os version
    String geolocation = "";//user's location
    String network = "";//user's network type
    String returnResult = "";
    // service and activity objects
    private StateMachine machine;
    private SessionService service = null;
    private AppRTCActivity activity = null;
    // common variables
    private ConnectionInfo connectionInfo;
    private SessionInfo sessionInfo;
    private DatabaseHandler dbHandler;
    private boolean init = false; // switched to 'true' when activity first binds
    private boolean proxying = false; // switched to 'true' upon state machine change
    // performance instrumentation
    private PerformanceTimer performance;
    private Socket socket;
    private SocketHandlerThread socketHandlerThread;
    private WebSocketConnection webSocket;
    WebSocket.WebSocketConnectionObserver observer = new WebSocket.WebSocketConnectionObserver() {
        private boolean hasVMREADY;

        @Override
        public void onOpen() {
            Log.i(TAG, "WebSocket connected.");
            machine.setState(STATE.CONNECTED, R.string.appRTC_toast_socketConnector_success, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // AUTH -> CONNECTED
        }

        @Override
        public void onClose(WebSocketCloseNotification code, String reason) {
            Log.e(TAG, "machine.getState(): " + machine.getState());
            if (proxying || machine.getState() == STATE.AUTH || machine.getState() == STATE.CONNECTED) {
                activity.reconnect();
                changeToErrorState("WebSocket disconnected.");
                Log.e(TAG, "WebSocket disconnected: " + code.toString() + ", " + reason);
            } else // we called disconnect(), this was intentional; log this as an Info message
                Log.i(TAG, "WebSocket disconnected.");
        }

        @Override
        public void onTextMessage(String payload) {
        }

        @Override
        public void onRawTextMessage(byte[] payload) {
        }

        @Override
        public void onBinaryMessage(byte[] payload) {
            try {
                Response data = Response.parseFrom(payload);
                Log.d(TAG, "Received incoming message object of type " + data.getType().name());
                onResponse(data);
            } catch (InvalidProtocolBufferException e) {
                Log.e(TAG, "Unable to parse protobuf:", e);
                changeToErrorState("Unable to parse protobuf.");
            }
        }

        private void onResponse(Response data) {
            if (data.getType() == Response.ResponseType.ERROR) {
                Log.e(TAG, "Received ERROR message");
                int error = hasVMREADY ? R.string.appRTC_toast_connection_finish : R.string.appRTC_toast_brahmaReadyWait_fail;
                String errorString = hasVMREADY ? "Error, connection has been closed" : "Server failed to initialize VM.";
                machine.setState(STATE.ERROR, error, errorString);
                //need to stop progress dialog.
            } else if (!hasVMREADY && data.getType() == Response.ResponseType.VMREADY) // we are in the CONNECTED state, waiting for VMREADY
                onResponseCONNECTED(data);
            else // we are in the RUNNING state
                onResponseRUNNING(data);
        }

        // STEP 3: CONNECTED -> RUNNING, Receive VMREADY message
        private void onResponseCONNECTED(Response data) {
            hasVMREADY = true;
            machine.setState(STATE.RUNNING, R.string.appRTC_toast_brahmaReadyWait_success, activity.getResources().getString(R.string.appRTC_toast_brahmaReadyWait_success)); // CONNECTED -> RUNNING
            proxying = true;
            service.onOpen();
            if (isBound())
                activity.onOpen();
            performance.start();
        }

        // STEP 4: RUNNING
        private void onResponseRUNNING(final Response data) {
            boolean consumed = service.onMessage(data);

            if (!consumed && isBound()) {
                activity.stoptimertask();
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "AppRTCClient data:" + data.getType());
                        activity.onMessage(data);
                    }
                });
                //start service
            }
        }
    };

    // STEP 0: NEW -> STARTED
    public AppRTCClient(SessionService service, StateMachine machine, ConnectionInfo connectionInfo) {
        this.service = service;
        this.machine = machine;
        machine.addObserver(service);
        this.connectionInfo = connectionInfo;

        this.dbHandler = new DatabaseHandler(service);
        this.performance = new PerformanceTimer(service, this, connectionInfo.getConnectionID());

        machine.setState(STATE.STARTED, 0, null);
    }

    // called from activity
    public void connectToRoom(AppRTCActivity activity) {
        Log.d("LifeCycle", "AppRTCClient connectToRoom");
        this.activity = activity;
        machine.addObserver(activity);

        // we don't initialize the SocketConnector until the activity first binds; mitigates concurrency issues
        if (!init) {
            init = true;

            int error = 0;
            // determine whether we should use SSL from the EncryptionType integer
            useSSL = connectionInfo.getEncryptionType() == Constants.ENCRYPTION_SSLTLS;

            if (useSSL) {
                sslConfig = new SSLConfig(connectionInfo, activity);
//                ssl = new SSLContextUtil(activity);
//                error = sslConfig.configure();
                error = 0;
            }
            if (error == 0)
                login();
            else
                machine.setState(STATE.ERROR, error, activity.getResources().getString(error));
        }
        // if the state is already running, we are reconnecting
        else if (machine.getState() == STATE.RUNNING) {
            activity.onOpen();
        }

        try {
            ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo tInfo = gConnMgr.getActiveNetworkInfo();
            Log.d("LifeCycle", "AppRTCClient connectToRoom");
            if (tInfo != null) {
                network = toLowerCase(tInfo.getTypeName());
            } else {
                network = "wifi";
            }
            device = Build.BRAND + " " + Build.MODEL;
            os = "Android " + Build.VERSION.RELEASE;
            // 取得螢幕解析度
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            //resolution = dm.widthPixels +"*"+dm.heightPixels;
            geolocation = context.getResources().getConfiguration().locale.getCountry();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR:" + e.getStackTrace());
        }
    }

    // called from activity
    public void disconnectFromRoom() {
        machine.removeObserver(activity);
        this.activity = null;
    }

    public boolean isBound() {
        return this.activity != null;
    }

    public PerformanceTimer getPerformance() {
        return performance;
    }

    public AppRTCSignalingParameters getSignalingParams() {
        return sessionInfo.getSignalingParams();
    }

    // called from AppRTCActivity
    public void changeToErrorState(String s) {
        Log.e(TAG, "changeToErrorState");
        machine.setState(STATE.ERROR, R.string.appRTC_toast_connection_finish, s);
    }

    public void disconnect() {
        proxying = false;
        // we're disconnecting, update the database record with the current timestamp
        dbHandler.close();
        performance.cancel(); // stop taking performance measurements

        // shut down the WebSocket if it's open
        if (webSocket != null && webSocket.isConnected()) {
            Log.d(TAG, "webSocket disconnect");
            webSocket.disconnect();
//            webSocket.testClose();
        }

        if (socketHandlerThread != null)
            socketHandlerThread.quitSafely();
    }

    public synchronized void sendMessage(Request msg) {

        if (proxying) {
            //webSocket.sendBinaryMessage(msg.toByteArray());
            // VM is expecting a message delimiter (varint prefix) so write a delimited message instead
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                msg.writeDelimitedTo(stream);
                webSocket.sendBinaryMessage(stream.toByteArray());
            } catch (IOException e) {
                Log.e(TAG, "Error writing delimited byte output:", e);
            }
        }
    }

    public void login() {
        // attempt to get any existing auth data JSONObject that's in memory (e.g. made of user input such as password)
        JSONObject jsonObject = AuthData.getJSON(connectionInfo);
        if (jsonObject != null) {
            // execute async HTTP request to the REST auth API
            (new BRAHMAAuthenticator()).execute(jsonObject);
            (new registerDeviceTask()).execute(connectionInfo);
        } else {
            sessionInfo = dbHandler.getSessionInfo(connectionInfo);
            if (sessionInfo != null) {
                // we've already authenticated, we can connect directly to the proxy
                machine.setState(STATE.AUTH, R.string.appRTC_toast_brahmaAuthenticator_bypassed, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_bypassed)); // STARTED -> AUTH
                connect();
            } else {
                Log.e(TAG, "login failed: no auth data or session info found");
                machine.setState(STATE.ERROR, R.string.appRTC_toast_login_failed, activity.getResources().getString(R.string.appRTC_toast_login_failed));
            }
        }
    }

    // STEP 2: AUTH -> CONNECTED, Connect to the Brahma proxy service
    public void connect() {
        Log.d(TAG, "AppRTCClent connect");
        new SocketConnector().execute();
    }

    // STEP 1: STARTED -> AUTH, Authenticate with the Brahma login REST service
    private class BRAHMAAuthenticator extends AsyncTask<JSONObject, Void, Integer> {
        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            Log.d("LifeCycle", "BRAHMAAuthenticator doInBackground");
            int returnVal = R.string.appRTC_toast_socketConnector_fail; // generic error message
            returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_fail);

            AuthData.setPd("");

            int rPort = connectionInfo.getPort();

            String proto, rHost, api, uri;
            proto = "https";
            rHost = connectionInfo.getHost();
            if (AppRTCClient.type.equals("vmi")) {
                api = "login";
            } else {
                api = "clients/getNode";
            }

            uri = String.format("%s://%s:%d/%s", proto, rHost, rPort, api);
            Log.d(TAG, "uri: " + uri);

            // set up HttpParams
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            EasySSLSocketFactory socketFactory = new EasySSLSocketFactory();

            // set up ConnectionManager
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme(proto, socketFactory, rPort));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            // create HttpClient
            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
            HttpPost post = new HttpPost(uri);
            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
            post.setHeader("brahma-lang", Locale.getDefault().getLanguage());
            JSONObject jsonRequest = new JSONObject();

            if (AppRTCClient.type.equals("vmi")) {
                jsonRequest = jsonObjects[0];
                try {
                    jsonRequest.put("device", device);
                    jsonRequest.put("os", os);
                    jsonRequest.put("longitude", String.valueOf(Longitude));
                    jsonRequest.put("latitude", String.valueOf(Latitude));
                    jsonRequest.put("resolution", resolution);
                    jsonRequest.put("network", network);
                    jsonRequest.put("media", "standard");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                post.addHeader("brahma-authtoken", performanceToken);
                post.addHeader("brahma-lang", Locale.getDefault().getLanguage());
                try {
                    jsonRequest.put("user_id", user_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "jsonRequest:" + jsonRequest.toString());

            try {
                String msg = "";
                String status_code = "";
                if (_login) {//不需要再login
                    msg = jsonResponse.getString("msg");
                    status_code = jsonResponse.getString("status_code");
                    Log.d(TAG, "get json message:" + msg);
                    _login = false;
                } else {
                    StringEntity entity = new StringEntity(jsonRequest.toString());
                    post.setEntity(entity);
                    HttpResponse response = httpclient.execute(post);
                    responseCode = response.getStatusLine().getStatusCode();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    Log.d(TAG, "responseCode:" + responseCode);
                    Log.d(TAG, "response message:" + out.toString());
                    jsonResponse = new JSONObject(out.toString());
                    msg = jsonResponse.getString("msg");
                    status_code = jsonResponse.getString("status_code");
                    Log.d(TAG, "get json message:" + msg);
                }

                long expires = 0;
                if (responseCode == 200) {
                    if (status_code.equals("20000")) {
                        //judge type
                        if (jsonResponse.has("type")) {
                            type = jsonResponse.getString("type");
                        }
                        if (jsonResponse.has("user_id")) {
                            user_id = jsonResponse.getString("user_id");
                        }
                        if (jsonResponse.has("token")) {
                            performanceToken = jsonResponse.getString("token");
                        }

                        if (jsonResponse.getJSONObject("sessionInfo").has("token")) {
                            token = jsonResponse.getJSONObject("sessionInfo").getString("token");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("brahmaUsername")) {
                            deviceInfo.setUser(jsonResponse.getJSONObject("sessionInfo").getString("brahmaUsername"));
                            brahmaUsername = jsonResponse.getJSONObject("sessionInfo").getString("brahmaUsername");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("role")) {
                            role = jsonResponse.getJSONObject("sessionInfo").getString("role");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("vm_platform")) {
                            vm_platform = jsonResponse.getJSONObject("sessionInfo").getString("vm_platform");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("vm_version")) {
                            vm_version = jsonResponse.getJSONObject("sessionInfo").getString("vm_version");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("vm_ip")) {
                            vm_ip = jsonResponse.getJSONObject("sessionInfo").getString("vm_ip");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("has_notification")) {
                            has_notification = jsonResponse.getJSONObject("sessionInfo").getString("has_notification");
                        }
                        if (jsonResponse.getJSONObject("sessionInfo").has("maxLength")) {
                            expires = new Date().getTime() + (1000 * jsonResponse.getJSONObject("sessionInfo").getInt("maxLength"));
                        }

//                        *****20191008 modify for Debug*****
//                        String host = jsonResponse.getJSONObject("server").getString("host");
//                        String port = jsonResponse.getJSONObject("server").getString("port");
                        String host = normalIP;
                        String port = normalPort;

                        JSONObject webrtc = jsonResponse.getJSONObject("webrtc");
                        sessionInfo = new SessionInfo(token, expires, host, port, webrtc);
                        RoleData roleData = new RoleData();
                        roleData.setRole(role);
                        if (sessionInfo.getSignalingParams() != null) {
                            returnVal = 0;
                            returnResult = null;
                        }
//                        sendDeviceRegist(userEmail);
                    } else {
                        Log.d(TAG, "no app" + msg);
                        //no app
                        returnVal = R.string.appRTC_toast_socketConnector_404;
                        returnResult = jsonResponse.getString("msg");
                    }
                } else if (responseCode == 400) {
                    returnVal = R.string.appRTC_toast_brahmaAuthenticator_passwordChangeFail;
                    returnResult = jsonResponse.getString("msg");
                } else if (responseCode == 401) {
                    returnVal = R.string.appRTC_toast_brahmaAuthenticator_passwordChangeFail;
                    returnResult = jsonResponse.getString("msg");
                } else if (responseCode == 403) {
                    returnVal = R.string.brahmaActivity_toast_noAccessToken;
                    returnResult = jsonResponse.getString("msg");
                } else if (responseCode == 404) {
                    returnVal = R.string.appRTC_toast_socketConnector_404;
                    returnResult = jsonResponse.getString("msg");
                } else if (responseCode == 409) {
                    returnResult = jsonResponse.getString("msg");
                } else {
                    returnResult = jsonResponse.getString("msg");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON response:", e);
                returnVal = R.string.failed_to_parse_json_response;
                returnResult = activity.getResources().getString(R.string.failed_to_parse_json_response);
            } catch (SSLHandshakeException e) {
                String msg = e.getMessage();
                if (msg.contains("java.security.cert.CertPathValidatorException")) {
                    Log.e(TAG, "Untrusted server certificate!");
                    returnVal = R.string.appRTC_toast_socketConnector_failUntrustedServer;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failUntrustedServer);
                } else {
                    Log.e(TAG, "Error during SSL handshake: " + e.getMessage());
                    returnVal = R.string.appRTC_toast_socketConnector_failSSLHandshake;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failSSLHandshake);
                }
            } catch (SSLException e) {
                if ("Connection closed by peer".equals(e.getMessage())) {
                    // connection failed, we tried to connect using SSL but REST API's SSL is turned off
                    Log.e(TAG, "Client encryption is on but server encryption is off:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failSSL);
                } else {
                    Log.e(TAG, "SSL error:", e);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "HTTP request failed:", e);
                returnVal = R.string.appRTC_toast_socketConnector_check;
                returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_check);
            } catch (IOException e) {
                Log.e(TAG, "HTTP request failed:", e);
                returnVal = R.string.appRTC_toast_socketConnector_check;
                returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_check);
            }
            return returnVal;
        }
        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                dbHandler.updateSessionInfo(connectionInfo, sessionInfo);
                machine.setState(STATE.AUTH, R.string.appRTC_toast_brahmaAuthenticator_success, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                connect();
            } else {
                // authentication failed, handle appropriately
                machine.setState(STATE.ERROR, result, returnResult); // STARTED -> ERROR
            }
        }
    }

    private class registerDeviceTask extends AsyncTask<ConnectionInfo, Void, String> {

        @Override
        protected String doInBackground(ConnectionInfo... connectionInfos) {
            Log.d("ddddd","registerDeviceTask doInBackground");
            String uriAPI = "https://" + connectionInfo.getHost() + ":" + connectionInfo.getPort() + "/clients/deviceToken";
            Log.d(TAG, "uri API:" + uriAPI);
            registration_id = FirebaseInstanceId.getInstance().getToken();
            EasySSLSocketFactory easySSLSocketFactory = new EasySSLSocketFactory();
            JSONObject http_data = new JSONObject();
            try {
                http_data.put("user_id", Integer.valueOf(user_id));
                http_data.put("fcm_token", registration_id);
                http_data.put("device_token", device_id);
                http_data.put("system", "android");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("FCM", "http_data:" + http_data.toString());
            try {
                // set up HttpParams
                HttpParams params2 = new BasicHttpParams();
                HttpProtocolParams.setVersion(params2, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params2, HTTP.UTF_8);

                // set up ConnectionManager
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", easySSLSocketFactory, Integer.valueOf(normalPort)));
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params2, registry);
                DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params2);
                HttpPost post = new HttpPost(uriAPI);
                post.setHeader(HTTP.CONTENT_TYPE, "application/json");
                post.setHeader("brahma-lang", Locale.getDefault().getLanguage());
                post.addHeader("brahma-authtoken", performanceToken);
                StringEntity entity = null;
                int responseCode = 0;
                HttpResponse response = null;
                try {
                    entity = new StringEntity(http_data.toString());
                    post.setEntity(entity);
                    response = httpclient.execute(post);
                    responseCode = response.getStatusLine().getStatusCode();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "device_regist http responseCode:" + responseCode);
                String strResult = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "device_regist http strResult:" + strResult);
                if (responseCode == 200 || responseCode == 500) {
                    strResult = EntityUtils.toString(response.getEntity());
                    Log.d(TAG, "device_regist http strResult:" + strResult);
                    return strResult;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private class SocketConnector extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            Log.d("LifeCycle", "SocketConnector doInBackground");
            int returnVal = R.string.appRTC_toast_socketConnector_fail; // resID for return message
            returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_fail);
            try {
                // create the socket for the WebSocketConnection to use
                // we do this here because the Looping and Handling that takes place in the WebSocket code causes
                // the app to freeze when any other processes are launched (such as KeyChain or MemorizingTrustManager)
                javax.net.SocketFactory factory;

                if (useSSL) {
                    EasySSLSocketFactory eSSL = new EasySSLSocketFactory();
                    factory = eSSL.getSSLContext().getSocketFactory();//取得sslSocket
                } else {
                    factory = javax.net.SocketFactory.getDefault();//使用預設
                }
//                socket = factory.createSocket(sessionInfo.getHost(), Integer.parseInt(sessionInfo.getPort()));
                socket = factory.createSocket(normalIP, Integer.parseInt(normalPort));

                if (useSSL) {//如果使用ssl,另外進行sslSocket的設定
                    Log.d(TAG, "setting SSLSocket");
                    SSLSocket sslSocket = (SSLSocket) socket;
                    sslSocket.setEnabledProtocols(ENABLED_PROTOCOLS);
                    sslSocket.setEnabledCipherSuites(ENABLED_CIPHERS);
                    sslSocket.startHandshake(); // starts the handshake to verify the cert before continuing
                }
                returnVal = 0;
                returnResult = null;
            } catch (SSLHandshakeException e) {
                String msg = e.getMessage();
                if (msg.contains("java.security.cert.CertPathValidatorException")) {
                    // the server's certificate isn't in our trust store
                    Log.e(TAG, "Untrusted server certificate!");
                    returnVal = R.string.appRTC_toast_socketConnector_failUntrustedServer;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failUntrustedServer);
                } else {
                    Log.e(TAG, "Error during SSL handshake: " + e.getMessage());
                    returnVal = R.string.appRTC_toast_socketConnector_failSSLHandshake;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failSSLHandshake);
                }
            } catch (SSLException e) {
                if ("Connection closed by peer".equals(e.getMessage())) {
                    // connection failed, we tried to connect using SSL but REST API's SSL is turned off
                    Log.e(TAG, "Client encryption is on but server encryption is off:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failSSL);
                } else {
                    Log.e(TAG, "SSL error:", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                e.printStackTrace();
            }
            return returnVal;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != 0) {
                machine.setState(STATE.ERROR, result, returnResult); // STARTED -> ERROR
            } else {
                // we have to run the WebSocket connection in a HandlerThread to ensure that Looper is prepared
                // properly and that the MemorizingTrustManager doesn't execute on the main UI thread
                //socketHandlerThread = new SocketHandlerThread("apportal-websocket-" + new Date().getTime());
                socketHandlerThread = new SocketHandlerThread("apportal-websocket-" + new Date().getTime());
                socketHandlerThread.start();
            }
        }
    }

    //用websocket來建立webrtc之間的連線管道，用來交換一些必要的資訊
    private class SocketHandlerThread extends HandlerThread {

        public SocketHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            // set up the WebSocket URI for the brahma-server
            String proto = useSSL ? "wss" : "ws";
            URI uri = URI.create(String.format("%s://%s:%s", proto, normalIP, normalPort));
            Log.d(TAG, "Socket connecting to " + uri.toString());
            Log.d(TAG, "Sec-WebSocket-Protocol " + sessionInfo.getToken());

            // set up the WebSocket options for the brahma-server
            WebSocketOptions options = new WebSocketOptions();
            options.setMaxFramePayloadSize(8 * 128 * 1024); // increase max frame size to handle high-res icons
            HashMap<String, String> headers = new HashMap<String, String>();
            // HACK: JavaScript WebSocket API doesn't allow for custom headers, so we repurpose this header instead
            // We set it here instead of the constructor because this doesn't append a comma suffix
            headers.put("Sec-WebSocket-Protocol", sessionInfo.getToken());
            options.setHeaders(headers);

            // we have the socket and the SSL handshake has completed
            // now establish a WebSocketConnection
            try {
                webSocket = new WebSocketConnection();
                webSocket.connect(socket, uri, null, observer, options);
            } catch (WebSocketException e) {
                Log.e(TAG, "Failed to connect to Brahma proxy:", e);
                machine.setState(STATE.ERROR, R.string.appRTC_toast_socketConnector_fail, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_fail));
                //通知LOG訊息
                //ls.logsend(deviceInfo.getmUser(), deviceInfo.getmModel(), deviceInfo.getmOSRelease(),"Failed to connect to Brahma proxy: " + e, connectionInfo);
            }
        }
    }

    /**
     * Struct holding the signaling parameters of an AppRTC room.
     */
    class SignalingParameters {
        public final List<PeerConnection.IceServer> iceServers;
        public final boolean initiator;
        public final String clientId;
        public final String wssUrl;
        public final String wssPostUrl;
        public final SessionDescription offerSdp;
        public final List<IceCandidate> iceCandidates;

        public SignalingParameters(List<PeerConnection.IceServer> iceServers, boolean initiator,
                                   String clientId, String wssUrl, String wssPostUrl, SessionDescription offerSdp,
                                   List<IceCandidate> iceCandidates) {
            this.iceServers = iceServers;
            this.initiator = initiator;
            this.clientId = clientId;
            this.wssUrl = wssUrl;
            this.wssPostUrl = wssPostUrl;
            this.offerSdp = offerSdp;
            this.iceCandidates = iceCandidates;
        }
    }

}
