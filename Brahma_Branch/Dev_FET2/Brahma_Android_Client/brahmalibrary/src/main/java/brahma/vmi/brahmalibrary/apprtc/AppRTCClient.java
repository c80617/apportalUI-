package brahma.vmi.brahmalibrary.apprtc;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.protobuf.InvalidProtocolBufferException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.activities.AppRTCActivity;
import brahma.vmi.brahmalibrary.auth.AuthData;
import brahma.vmi.brahmalibrary.autobahn.WebSocket;
import brahma.vmi.brahmalibrary.autobahn.WebSocketConnection;
import brahma.vmi.brahmalibrary.autobahn.WebSocketException;
import brahma.vmi.brahmalibrary.autobahn.WebSocketOptions;
import brahma.vmi.brahmalibrary.common.ConnectionInfo;
import brahma.vmi.brahmalibrary.common.Constants;
import brahma.vmi.brahmalibrary.common.DatabaseHandler;
import brahma.vmi.brahmalibrary.common.SessionInfo;
import brahma.vmi.brahmalibrary.common.StateMachine;
import brahma.vmi.brahmalibrary.common.StateMachine.STATE;
import brahma.vmi.brahmalibrary.database.ItemBio;
import brahma.vmi.brahmalibrary.database.MyDBHelper;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Request;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Response;
import brahma.vmi.brahmalibrary.services.SessionService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static brahma.vmi.brahmalibrary.firebase.MyFirebaseMessagingService.registration_id;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity._login;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.context;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.device_id;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.gridcontext;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.jsonResponse;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalPort;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.responseCode;

//import brahma.vmi.brahmalibrary.performance.PerformanceTimer;

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
    public static String type = "";//vmi or appstreaming
    public static String deviceID = "";
    public static String token;
    public static String performanceToken;
    public static String user_id = "";
    public static JSONArray appListArray;
    public static boolean auto_connect = true;
    private final ItemBio itemBio;
    private String returnResult = "";
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;
    private StateMachine machine;
    private SessionService service;
    private AppRTCActivity activity = null;
    private ConnectionInfo connectionInfo;
    private SessionInfo sessionInfo;
    private DatabaseHandler dbHandler;
    private boolean init = false; // switched to 'true' when activity first binds
    private boolean proxying = false; // switched to 'true' upon state machine change
    //    private PerformanceTimer performance;
    private Socket socket;
    private SocketHandlerThread socketHandlerThread;
    private WebSocketConnection webSocket;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
//                    stopProgressDialog();
                    break;
                case 2:
                    if (gridcontext != null) {
                        Log.d(TAG, "handleMessage >>>>> " + msg.arg1);
                        Toast.makeText(gridcontext, msg.arg1, Toast.LENGTH_LONG).show();
                    }

                    break;
                case 3:
//                    stopProgressDialog();
                    if (gridcontext != null) {
                        Log.d(TAG, "handleMessage >>>>> " + msg.obj.toString());
                        Toast.makeText(gridcontext, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    }
                    break;
                case 4:
                    Log.d(TAG, "handleMessage >>>>> " + msg.arg1);
                    if (gridcontext != null) {
                        Toast.makeText(gridcontext, msg.arg1, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 8:
                    if (context != null) {
                        Log.d(TAG, "handleMessage >>>>> " + msg.arg1);
                        Toast.makeText(context, msg.arg1, Toast.LENGTH_LONG).show();
                    }
                    break;

                default:
                    break;
            }
        }
    };
    private WebSocket.WebSocketConnectionObserver observer = new WebSocket.WebSocketConnectionObserver() {
        private boolean hasVMREADY;

        @Override
        public void onOpen() {
            if (activity != null)
                activity.setProgressDegree(20, activity.getResources().getString(R.string.progressBar_20));
            Log.i(TAG, "WebSocket connected.");
            machine.setState(STATE.CONNECTED, R.string.appRTC_toast_socketConnector_success, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // AUTH -> CONNECTED
        }

        @Override
        public void onClose(WebSocketCloseNotification code, String reason) {
            Log.e(TAG, "machine.getState(): " + machine.getState());
            if (proxying || machine.getState() == STATE.AUTH || machine.getState() == STATE.CONNECTED) {
                // do not reconnect
                //activity.reconnect();
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
            Log.i(TAG, "WebSocket onBinaryMessage.");
            try {
                Response data = Response.parseFrom(payload);
                Log.d(TAG, "Received incoming message object of type " + data.getType().name());
                onResponse(data);
            } catch (InvalidProtocolBufferException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(TAG, "Unable to parse protobuf:", e);
                changeToErrorState("Unable to parse protobuf.");
            }
        }

        private void onResponse(Response data) {
            Log.i(TAG, "WebSocket onResponse.");

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
            Log.i(TAG, "WebSocket onResponseCONNECTED.");
            if (activity != null)
                activity.setProgressDegree(60, activity.getResources().getString(R.string.progressBar_60));
            hasVMREADY = true;
            machine.setState(STATE.RUNNING, R.string.appRTC_toast_brahmaReadyWait_success, activity.getResources().getString(R.string.appRTC_toast_brahmaReadyWait_success)); // CONNECTED -> RUNNING
            proxying = true;
            service.onOpen();
            if (isBound())
                activity.onOpen();
//            performance.start();
        }

        // STEP 4: RUNNING
        private void onResponseRUNNING(final Response data) {
            Log.i(TAG, "WebSocket onResponseRUNNING.");
//            if (activity != null)
//                activity.setProgressDegree(100,activity.getResources().getString(R.string.progressBar_100));
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
    private int RETRUEN_NO_APP = 93939;
    private JSONObject jsonObject;

    // STEP 0: NEW -> STARTED
    public AppRTCClient(SessionService service, StateMachine machine, ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "AppRTCClient");
        this.service = service;
        this.machine = machine;
        machine.addObserver(service);
        this.connectionInfo = connectionInfo;

        this.dbHandler = new DatabaseHandler(service);
//        this.performance = new PerformanceTimer(service, this, connectionInfo.getConnectionID());

        machine.setState(STATE.STARTED, 0, null);
        initSSLSocketFactory();
        MyDBHelper dbHelper = new MyDBHelper(service);
        itemBio = new ItemBio(dbHelper.getDatabase(service));
    }

    private void initSSLSocketFactory() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
        KeyStore localTrustStore = null;
        try {
            localTrustStore = KeyStore.getInstance("BKS");
            assert application != null;
            InputStream in = application.getResources().openRawResource(R.raw.mykeystore);
            try {
                localTrustStore.load(in, Constants.TRUSTSTORE.toCharArray());
                in.close();
            } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(localTrustStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    // called from activity
    public void connectToRoom(AppRTCActivity activity) {
        Log.d("LifeCycle", "AppRTCClient connectToRoom");
        this.activity = activity;
        machine.addObserver(activity);
        if (!init) {
            init = true;
            login();
        } else if (machine.getState() == STATE.RUNNING) {
            activity.onOpen();
        }
    }

    // called from activity
    public void disconnectFromRoom() {
        Log.d("LifeCycle", "appRTCClient disconnectFromRoom");
        machine.removeObserver(activity);
        this.activity = null;
    }

    public boolean isBound() {
        return this.activity != null;
    }

//    public PerformanceTimer getPerformance() {
//        return performance;
//    }

    public AppRTCSignalingParameters getSignalingParams() {
        return sessionInfo.getSignalingParams();
    }

    // called from AppRTCActivity
    public void changeToErrorState(String s) {
        Log.e(TAG, "changeToErrorState");
        machine.setState(STATE.ERROR, R.string.appRTC_toast_connection_finish, s);
    }

    public void disconnect() {
        Log.d("LifeCycle", "appRTCClient disconnect");
        proxying = false;
        // we're disconnecting, update the database record with the current timestamp
        dbHandler.close();
//        performance.cancel(); // stop taking performance measurements

        // shut down the WebSocket if it's open
        if (webSocket != null && webSocket.isConnected()) {
            Log.d(TAG, "webSocket disconnect");
            webSocket.disconnect();
        }

        if (socketHandlerThread != null)
            socketHandlerThread.quitSafely();
//        if(itemBio != null)
//            itemBio.close();

        Message msg2 = new Message();
        msg2.what = 8;
        msg2.arg1 = R.string.connection_end;
        mHandler.sendMessage(msg2);

    }

    public synchronized void sendMessage(Request msg) {
        if (proxying) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                msg.writeDelimitedTo(stream);
                webSocket.sendBinaryMessage(stream.toByteArray());
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(TAG, "Error writing delimited byte output:", e);
            }
        }
    }

    public void login() {
        (new BRAHMAAuthenticator()).execute();
    }

    // STEP 2: AUTH -> CONNECTED, Connect to the Brahma proxy service
    public void connect() {
        Log.d(TAG, "AppRTCClient connect");
        activity.setProgressDegree(0, activity.getResources().getString(R.string.progressBar_0));
        new SocketConnector().execute();
    }

    public void sendInterrupt() {
        Log.d(TAG, "AppRTCClient sendInterrupt");
        (new interruptTask()).execute();
    }

    /**
     * Struct holding the signaling parameters of an AppRTC room.
     */
    static class SignalingParameters {
        public final List<PeerConnection.IceServer> iceServers;
        final boolean initiator;
        final String clientId;
        final String wssUrl;
        final String wssPostUrl;
        final SessionDescription offerSdp;
        final List<IceCandidate> iceCandidates;

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

    // STEP 1: STARTED -> AUTH, Authenticate with the Brahma login REST service
    private class BRAHMAAuthenticator extends AsyncTask<JSONObject, Void, Integer> {
        String msg = "";
        String status_code = "";
        int returnVal;

        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {

//            if (activity != null)
//                activity.setProgressDegree(20,"準備連線");

            Log.d("LifeCycle", "BRAHMAAuthenticator doInBackground");
            returnVal = R.string.appRTC_toast_socketConnector_fail; // generic error message
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
            Log.d(TAG, "[BRAHMAAuthenticator] uri: " + uri);

            JSONObject http_data = new JSONObject();
            try {
                http_data.put("user_id", user_id);
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
            Log.d(TAG, "[BRAHMAAuthenticator] http_data:" + http_data.toString());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return s.equals(sslSession.getPeerHost());
                        }
                    })
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , http_data.toString());
            try {
                if (_login) {//不需要再login
                    msg = jsonResponse.getString("msg");
                    status_code = jsonResponse.getString("status_code");
                    Log.d(TAG, "get json message:" + msg);
                    _login = false;
                    returnVal = parseResponse();
                    Log.d(TAG, "[BRAHMAAuthenticator]  returnVal:" + returnVal);
                    if (returnVal == 0) {
                        dbHandler.updateSessionInfo(connectionInfo, sessionInfo);
                        machine.setState(STATE.AUTH, R.string.appRTC_toast_brahmaAuthenticator_success, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                        connect();
                    } else {
                        machine.setState(STATE.ERROR, returnVal, returnResult); // STARTED -> ERROR
                    }
                } else {
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(uri)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("brahma-lang", Locale.getDefault().getLanguage())
                            .addHeader("brahma-authtoken", performanceToken)
                            .post(requestBody)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "[BRAHMAAuthenticator]  error:" + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, okhttp3.Response response) throws IOException {
                            assert response.body() != null;
                            String jsonData = response.body().string();
                            responseCode = response.code();
                            Log.d(TAG, "[BRAHMAAuthenticator]  response body :" + jsonData);
                            Log.d(TAG, "[BRAHMAAuthenticator]  response code :" + responseCode);

                            try {
                                jsonResponse = new JSONObject(jsonData);
                                status_code = jsonResponse.getString("status_code");
                                returnVal = parseResponse();
                                Log.d(TAG, "[BRAHMAAuthenticator]  status_code:" + status_code);
                                Log.d(TAG, "[BRAHMAAuthenticator]  returnVal:" + returnVal);
                                if (returnVal == 0) {
                                    dbHandler.updateSessionInfo(connectionInfo, sessionInfo);
                                    machine.setState(STATE.AUTH, R.string.appRTC_toast_brahmaAuthenticator_success, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                                    connect();
//                                    (new registerDeviceTask()).execute(connectionInfo);
                                } else if (returnVal == RETRUEN_NO_APP) {
                                    Log.d(TAG, "[BRAHMAAuthenticator]  is a error!!");
//                                    machine.setState(STATE.AUTH, R.string.appRTC_toast_brahmaAuthenticator_success, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
//                                    machine.setState(STATE.ERROR, returnVal, returnResult); // STARTED -> ERROR
                                    //show alert, user can choose to interrupt connection.
                                    activity.showDialog();

                                } else {
                                    // authentication failed, handle appropriately
                                    machine.setState(STATE.ERROR, returnVal, returnResult); // STARTED -> ERROR
                                    Message msg2 = new Message();
                                    msg2.what = 2;
                                    msg2.arg1 = returnVal;
                                    mHandler.sendMessage(msg2);
                                }
                            } catch (JSONException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(TAG, "Failed to parse JSON response:", e);
                returnVal = R.string.failed_to_parse_json_response;
                returnResult = activity.getResources().getString(R.string.failed_to_parse_json_response);
                Message msg2 = new Message();
                msg2.what = 2;
                msg2.arg1 = returnVal;
                mHandler.sendMessage(msg2);
            } catch (IllegalArgumentException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(TAG, "HTTP request failed:", e);
                returnVal = R.string.appRTC_toast_socketConnector_check;
                returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_check);
                Message msg2 = new Message();
                msg2.what = 2;
                msg2.arg1 = returnVal;
                mHandler.sendMessage(msg2);
            }
            return returnVal;
        }

        int parseResponse() throws JSONException {
            long expires = 0;
            int returnVal = 0;
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
                    if (jsonResponse.getJSONObject("sessionInfo").has("maxLength")) {
                        expires = new Date().getTime() + (1000 * jsonResponse.getJSONObject("sessionInfo").getInt("maxLength"));
                    }

                    String host = jsonResponse.getJSONObject("server").getString("host");
                    String port = jsonResponse.getJSONObject("server").getString("port");

                    JSONObject webrtc = jsonResponse.getJSONObject("webrtc");
                    sessionInfo = new SessionInfo(token, expires, host, port, webrtc);

                    if (sessionInfo.getSignalingParams() != null) {
                        returnResult = null;
                    }
                } else if (status_code.equals("20005")) {
                    Log.d(TAG, "no app" + msg);
                    returnVal = RETRUEN_NO_APP;
                    returnResult = jsonResponse.getString("msg");

                } else {
                    returnResult = jsonResponse.getString("msg");
                    Message msg2 = new Message();
                    msg2.what = 3;
                    msg2.obj = returnResult;
                    mHandler.sendMessage(msg2);
                    returnVal = R.string.default_error_msg;//juse a error
                }
            } else if (responseCode == 400) {
                returnVal = R.string.appRTC_toast_brahmaAuthenticator_passwordChangeFail;
                returnResult = jsonResponse.getString("msg");
                Message msg2 = new Message();
                msg2.what = 3;
                msg2.obj = returnResult;
                mHandler.sendMessage(msg2);
            } else if (responseCode == 401) {
                returnVal = R.string.appRTC_toast_brahmaAuthenticator_passwordChangeFail;
                returnResult = jsonResponse.getString("msg");
                Message msg2 = new Message();
                msg2.what = 3;
                msg2.obj = returnResult;
                mHandler.sendMessage(msg2);
            } else if (responseCode == 403) {
                returnVal = R.string.brahmaActivity_toast_noAccessToken;
                returnResult = jsonResponse.getString("msg");
                Message msg2 = new Message();
                msg2.what = 3;
                msg2.obj = returnResult;
                mHandler.sendMessage(msg2);
            } else if (responseCode == 404) {
                returnVal = R.string.appRTC_toast_socketConnector_404;
                returnResult = jsonResponse.getString("msg");
                Message msg2 = new Message();
                msg2.what = 3;
                msg2.obj = returnResult;
                mHandler.sendMessage(msg2);
            } else if (responseCode == 409) {
                returnResult = jsonResponse.getString("msg");
                Message msg2 = new Message();
                msg2.what = 3;
                msg2.obj = returnResult;
                mHandler.sendMessage(msg2);
            } else {
                returnResult = jsonResponse.getString("msg");
                Message msg2 = new Message();
                msg2.what = 3;
                msg2.obj = returnResult;
                mHandler.sendMessage(msg2);
            }
            return returnVal;
        }
    }

    private class registerDeviceTask extends AsyncTask<ConnectionInfo, Void, String> {
        @Override
        protected String doInBackground(ConnectionInfo... connectionInfos) {
            String uriAPI = "https://" + normalIP + ":" + normalPort + "/clients/deviceToken";
            Log.d(TAG, "[registerDeviceTask] uri API:" + uriAPI);
            JSONObject http_data = new JSONObject();
            try {
                http_data.put("user_id", user_id);
                http_data.put("fcm_token", registration_id);
                http_data.put("device_token", device_id);
                http_data.put("system", "android");
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
            Log.d(TAG, "[registerDeviceTask] http_data:" + http_data.toString());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return s.equals(sslSession.getPeerHost());
                        }
                    })
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , http_data.toString());

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(uriAPI)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("brahma-lang", Locale.getDefault().getLanguage())
                    .addHeader("brahma-authtoken", performanceToken)
                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "error:" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    responseCode = response.code();
                    Log.i(TAG, "[registerDeviceTask] response body :" + jsonData);
                    Log.i(TAG, "[registerDeviceTask] response code :" + responseCode);
                }
            });
            return null;
        }
    }

    private class interruptTask extends AsyncTask<ConnectionInfo, Void, String> {
        @Override
        protected String doInBackground(ConnectionInfo... connectionInfos) {
            String uriAPI = "https://" + normalIP + ":" + normalPort + "/clients/interruptConnect";
            Log.d(TAG, "[interruptTask] uri API:" + uriAPI);
            JSONObject http_data = new JSONObject();
            try {
                http_data.put("from", "remove");
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
            Log.d(TAG, "[interruptTask] http_data:" + http_data.toString());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return s.equals(sslSession.getPeerHost());
                        }
                    })
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , http_data.toString());

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(uriAPI)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("brahma-authtoken", performanceToken)
                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "error:" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    responseCode = response.code();
                    Log.i(TAG, "[interruptTask] response body :" + jsonData);
                    Log.i(TAG, "[interruptTask] response code :" + responseCode);

                    if (responseCode == 200) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        (new BRAHMAAuthenticator()).execute(jsonObject);
                    } else {


                    }
                }
            });
            return null;
        }
    }

    private class SocketConnector extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            if (activity != null)
//                activity.setProgressDegree(40,"傳送連線要求");
                Log.d("LifeCycle", "SocketConnector doInBackground");
            int returnVal = R.string.appRTC_toast_socketConnector_fail; // resID for return message
            returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_fail);
            try {
                socket = sslSocketFactory.createSocket(normalIP, Integer.parseInt(normalPort));

                Log.d(TAG, "setting SSLSocket");
                SSLSocket sslSocket = (SSLSocket) socket;
                sslSocket.setEnabledProtocols(ENABLED_PROTOCOLS);
                sslSocket.setEnabledCipherSuites(ENABLED_CIPHERS);
                sslSocket.startHandshake(); // starts the handshake to verify the cert before continuing
                returnVal = 0;
                returnResult = null;
            } catch (SSLHandshakeException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                String msg = e.getMessage();
                assert msg != null;
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
                FirebaseCrashlytics.getInstance().recordException(e);
                if ("Connection closed by peer".equals(e.getMessage())) {
                    // connection failed, we tried to connect using SSL but REST API's SSL is turned off
                    Log.e(TAG, "Client encryption is on but server encryption is off:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                    returnResult = activity.getResources().getString(R.string.appRTC_toast_socketConnector_failSSL);
                } else {
                    Log.e(TAG, "SSL error:", e);
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
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
                socketHandlerThread = new SocketHandlerThread("apportal-websocket-" + new Date().getTime());
                socketHandlerThread.start();
//                (new registerDeviceTask()).execute(connectionInfo);
            }
        }
    }

    private class SocketHandlerThread extends HandlerThread {
        SocketHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            String proto = "wss";
            URI uri = URI.create(String.format("%s://%s:%s", proto, normalIP, normalPort));
            Log.d(TAG, "Socket connecting to " + uri.toString());
            Log.d(TAG, "Sec-WebSocket-Protocol " + sessionInfo.getToken());

            // set up the WebSocket options for the brahma-server
            WebSocketOptions options = new WebSocketOptions();
            options.setMaxFramePayloadSize(8 * 128 * 1024); // increase max frame size to handle high-res icons
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Sec-WebSocket-Protocol", sessionInfo.getToken());
            options.setHeaders(headers);

            try {
                webSocket = new WebSocketConnection();
                webSocket.connect(socket, uri, null, observer, options);
            } catch (WebSocketException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(TAG, "Failed to connect to Brahma proxy:", e);
                machine.setState(STATE.ERROR, R.string.appRTC_toast_socketConnector_fail, activity.getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_fail));
            }
        }
    }
}
