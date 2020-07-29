package brahma.vmi.brahmalibrary.wcitui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.IDispatcher;
import com.microsoft.aad.adal.Logger;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.aad.adal.Telemetry;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.apprtc.AppRTCClient;
import brahma.vmi.brahmalibrary.apprtc.CallActivity;
import brahma.vmi.brahmalibrary.auth.AuthData;
import brahma.vmi.brahmalibrary.common.ConnectionInfo;
import brahma.vmi.brahmalibrary.common.Constants;
import brahma.vmi.brahmalibrary.common.DatabaseHandler;
import brahma.vmi.brahmalibrary.common.StateMachine;
import brahma.vmi.brahmalibrary.database.ItemBio;
import brahma.vmi.brahmalibrary.database.MyDBHelper;
import brahma.vmi.brahmalibrary.keyboard.KeyboardHeightObserver;
import brahma.vmi.brahmalibrary.keyboard.KeyboardHeightProvider;
import brahma.vmi.brahmalibrary.services.SessionService;
import brahma.vmi.brahmalibrary.wcitui.devicelog.DeviceInfo;
import brahma.vmi.brahmalibrary.widgets.APKVersionCodeUtils;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.icu.lang.UCharacter.toLowerCase;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static brahma.vmi.brahmalibrary.activities.AppRTCActivity.OpenAppMode;
import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.appListArray;
import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.performanceToken;
import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.user_id;
import static brahma.vmi.brahmalibrary.apprtc.CallActivity.KBheight;
import static brahma.vmi.brahmalibrary.firebase.MyFirebaseMessagingService.registration_id;
import static brahma.vmi.brahmalibrary.wcitui.SettingActivity.tempIP;
import static brahma.vmi.brahmalibrary.wcitui.SettingActivity.tempPort;
import static brahma.vmi.brahmalibrary.wcitui.WelcomeActivity.Latitude;
import static brahma.vmi.brahmalibrary.wcitui.WelcomeActivity.Longitude;
import static brahma.vmi.brahmalibrary.wcitui.WelcomeActivity.isSelectFile;

/**
 * @author YiWen Li
 * @file BrahmaMainActivity
 * @brief 使用者主要進行操作行為之activity
 * @date 2019/07/12
 **/


public class BrahmaMainActivity extends AppCompatActivity implements Constants, KeyboardHeightObserver {

    public final static int RESULT_NEEDAUTH = 103; // need to authenticate
    public final static int RESULT_NEEDPASSWORDCHANGE = 104; // need to authenticate
    private static final String adTAG = "azureAD";
    /* Azure AD Constants */
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    /* The clientID of your application is a unique identifier which can be obtained from the app registration portal */
    private static final String CLIENT_ID = "71b14e7e-63f1-4007-bd92-4ba622bab21f";
    /* Resource URI of the endpoint which will be accessed */
    private static final String RESOURCE_ID = "https://graph.windows.net";
    //    private static final String RESOURCE_ID = "https://hapseng.com/HSService";
    /* The Redirect URI of the application (Optional) */
    private static final String REDIRECT_URI = "Brahma://APPortalFET";
    /* Microsoft Graph Constants */
    private final static String MSGRAPH_URL = "https://graph.windows.net/hapseng.onmicrosoft.com/users?api-version=1.6";
    /* Constant to send message to the mAcquireTokenHandler to do acquire token with Prompt.Auto*/
    private static final int MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO = 1;
    /* Constant to send message to the mAcquireTokenHandler to do acquire token with Prompt.Always */
    private static final int MSG_INTERACTIVE_SIGN_IN_PROMPT_ALWAYS = 2;
    private static final int REQUEST_STARTVIDEO = 102;        // opens AppRTCVideoActivity
    private static final int REQUEST_SCQNNERQRCODE = 49374;
    private static final int REQUEST_UPLOADQRCODE = 49778;
    private static final int REQUEST_PROGREEDIALOG = 104;
    private static final int IPERF_ERROR = 1;
    private static final int IPERF_SCCESS = 2;
    private static final String IPERF_PATH = "/data/data/brahma.vmi.apportal/iperf";
    private static final String iperfServiceCmd = IPERF_PATH + " -s &";
    // Flag to turn event aggregation on/off
    private static final boolean sTelemetryAggregationIsRequired = true;
    private static final String KEY_NAME = "apportal_biometric_key";
    public static Context context = null;
    public static Context gridcontext = null;
    public static boolean https = true;
    public static String resolution = "";
    public static int widthPixels = 0;
    public static int heightPixels = 0;
    public static int DPI = 0;
    public static String userEmail = "";
    public static String techDayType = "";
    public static String qrcodePath = "";
    public static String qrcodeID = "";
    public static String qrcodeAPK = "";
    public static String qrcodeFileName = "";
    public static String normalIP = "";
    public static String normalPort = "";
    public static String normalUsername = "";
    public static String normalPassword = "";
    public static String packageName;
    public static String device_type = "";
    //Record login information
    public static JSONObject jsonResponse = null;
    public static int responseCode = 0;
    public static HttpResponse response = null;
    public static boolean _login = false;
    public static DatabaseHandler dbHandler;
    public static String login_type = "normal";
    public static String device_id = "";
    public static double resolution_ratio = 0.5;//default 0.5
    public static int heightPixels_org;
    public static int widthPixels_org;
    public static String myAppVerison = "";
    public static int SafeInsetTop = 0;
    public static int SafeInsetBottom = 0;
    public static String adToken = "";
    /* Boolean variable to ensure invocation of interactive sign-in only once in case of multiple  acquireTokenSilent call failures */
    private static AtomicBoolean sIntSignInInvoked = new AtomicBoolean();
    private static String TAG = "BrahmaMainActivity";

    /* Telemetry dispatcher registration */
    static {
        Telemetry.getInstance().registerDispatcher(new IDispatcher() {
            @Override
            public void dispatchEvent(Map<String, String> events) {
                // Events from ADAL will be sent to this callback
                for (Map.Entry<String, String> entry : events.entrySet()) {
                    Log.d(TAG, entry.getKey() + ": " + entry.getValue());
                }
            }
        }, sTelemetryAggregationIsRequired);
    }

    protected boolean busy = false; // is set to 'true' immediately after starting a connection, set to 'false' when resuming
    protected IntentFilter intentFilter;
    //    protected BrahmaMainActivity.MyBroadcastReceiver myBroadcastReceiver;
    protected IntentFilter intentFilter2;
    protected BrahmaMainActivity.MyDialogReceiver myDialogReceiver;
    Button ad_login;
    Button signOutButton;
    int times = 0;
    String myState;
    SharedPreferences sharedPreferences;
    AuthData pd = new AuthData();
    //    LoginStream lg = new LoginStream();
//    LoginStream.Clerk clerk = new LoginStream.Clerk();
    DeviceInfo deviceInfo = new DeviceInfo();
    Toast logToast;
    ConnectivityManager mConnectivityManager;
    NetworkInfo mNetworkInfo;
    ProgressDialog mDialog;
//    File file;
    ItemBio itemBio;
    InputMethodManager imm;
    TrustManager[] trustManagers = null;
    String language = "en";
    ImageButton setting;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;
    /* Azure AD Variables */
    private AuthenticationContext mAuthContext;
    private AuthenticationResult mAuthResult;
    /* Handler to do an interactive sign in and acquire token */
    private Handler mAcquireTokenHandler;
    private int sendRequestCode = REQUEST_STARTVIDEO; // used in the "afterStartAppRTC" method to determine what activity gets started
    private StateMachine machine;
    private View messageView;
    private View reconnectView;
    private AlertDialog reset_dialog;
    private AlertDialog message_dialog;
    private AlertDialog reconnect_dialog;
    private TextView appVersion, tv_touchid;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;
    private KeyboardHeightProvider keyboardHeightProvider;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    stopProgressDialog();
                    break;
                case 2:
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, msg.arg1, Toast.LENGTH_LONG);
                    logToast.show();
                    break;
                case 3:
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG);
                    logToast.show();
                    break;
                case 4:
                    logToast = Toast.makeText(BrahmaMainActivity.this, msg.arg1, Toast.LENGTH_LONG);
                    logToast.show();
                    break;
                case 5:
                    startProgressDialog(getResources().getString(R.string.appRTC_toast_connection_start));
                default:
                    break;
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case IPERF_ERROR:
                    Log.d(TAG, "Fail");
                    break;
                case IPERF_SCCESS:
                    Log.d(TAG, "Success");
                    break;
            }
        }
    };
    //    private BiometricPromptManager mManager;
    private TextView reconnect_tv;
    private FirebaseCrashlytics crashlytics;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private ImageButton image_touchid;
    private long result;
//    private EditText Username, deviceid;

    public static boolean isOpenLocationPermission() {
        int permission1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    public Activity getActivity() {
        return this;
    }

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brahma);
        Log.d("LifeCycle", "BrahmaMainActivity onCreate!!!");
        initSSLSocketFactory();

//        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        crashlytics = FirebaseCrashlytics.getInstance();
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);

        if (sharedPreferences.getString("resolution_ratio", "").equals("")) {
            Log.d(TAG, "default resolution_ratio");
            resolution_ratio = 0.5;
        } else {
            Log.d(TAG, "get resolution_ratio");
            resolution_ratio = Double.parseDouble(sharedPreferences.getString("resolution_ratio", ""));
        }
        Log.d(TAG, "oncreate resolution_ratio:" + resolution_ratio);

        // 動態調整解析度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        widthPixels = (int) ((dm.widthPixels) * resolution_ratio);
        heightPixels = (int) ((dm.heightPixels - SafeInsetTop - SafeInsetBottom) * resolution_ratio);
        widthPixels_org = dm.widthPixels;
        heightPixels_org = dm.heightPixels;
        resolution = widthPixels + "x" + heightPixels;
        DPI = (int) (getResources().getDisplayMetrics().densityDpi * resolution_ratio);

        Log.d(TAG, "resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "螢幕解析度(修正前):" + widthPixels_org + " x " + heightPixels_org);
        Log.d(TAG, "螢幕解析度(修正後):" + widthPixels + " x " + heightPixels);
        Log.d(TAG, "DPI: " + DPI);

        device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        device_id = "fbbe08e045ac3241";
//        Log.d(TAG, "getAndroidId: " + device_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        double wInches = dm.widthPixels / (double) dm.densityDpi;
        double hInches = dm.heightPixels / (double) dm.densityDpi;
        double screenDiagonal = Math.sqrt(Math.pow(wInches, 2) + Math.pow(hInches, 2));
        if (screenDiagonal >= 7.0) {
            device_type = "tablet";
        } else {
            device_type = "smartphone";
        }

        //App Version
        appVersion = (TextView) findViewById(R.id.appVersion);
        myAppVerison = APKVersionCodeUtils.getVerName(this);
        appVersion.append(APKVersionCodeUtils.getVerName(this));
        Log.d(TAG, "myAppVerison:" + myAppVerison);

//        intentFilter = new IntentFilter();
//        intentFilter.addAction("STATE_RECONNECT");
//        myBroadcastReceiver = new MyBroadcastReceiver();
//        registerReceiver(myBroadcastReceiver, intentFilter);

        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("STOP_PROGRESS");
        myDialogReceiver = new MyDialogReceiver();
        registerReceiver(myDialogReceiver, intentFilter2);

        image_touchid = (ImageButton) findViewById(R.id.image_touchid);
        tv_touchid = (TextView) findViewById(R.id.tv_touchid);

//        Username = (EditText) findViewById(R.id.Username);
//        deviceid = (EditText) findViewById(R.id.deviceid);

        View view = View.inflate(getApplicationContext(), R.layout.dialog_layout, null);

        mAuthContext = new AuthenticationContext(getApplicationContext(), AUTHORITY, false);
        setting = (ImageButton) findViewById(R.id.setting);
        /* Instantiate handler which can invoke interactive sign-in to get the Resource
         * sIntSignInInvoked ensures interactive sign-in is invoked one at a time */

        mAcquireTokenHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "mAcquireTokenHandler");
                if (sIntSignInInvoked.compareAndSet(false, true)) {
                    if (msg.what == MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO) {
                        //use cache or not
                        mAuthContext.acquireToken(getActivity(), RESOURCE_ID, CLIENT_ID, REDIRECT_URI, PromptBehavior.Always, getAuthInteractiveCallback());
                    } else if (msg.what == MSG_INTERACTIVE_SIGN_IN_PROMPT_ALWAYS) {
                        mAuthContext.acquireToken(getActivity(), RESOURCE_ID, CLIENT_ID, REDIRECT_URI, PromptBehavior.Always, getAuthInteractiveCallback());
                    }
                }
            }
        };

        /* ADAL Logging callback setup */
        Logger.getInstance().setExternalLogger(new Logger.ILogger() {
            @Override
            public void Log(String tag, String message, String additionalMessage, Logger.LogLevel level, ADALError errorCode) {
                Log.d(TAG, message + " " + additionalMessage);
            }
        });


        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.setView(view);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        machine = new StateMachine();

        dbHandler = new DatabaseHandler(this);
        deviceInfo.deviceInit(this);

        // if we received an intent indicating which connection to open, act upon it
        Intent intent = getIntent();
        if (intent.hasExtra("connectionID")) {
            int id = intent.getIntExtra("connectionID", 0);
            ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(id);
            if (connectionInfo != null) {
                if (connectionInfo.getDescription().equals("userMode")) {
                    //password need set!!
                    authPrompt(connectionInfo);
                }
            } else {
                Log.e(TAG, "connectionInfo is null");
            }
        }

        context = this;
        messageView = LayoutInflater.from(BrahmaMainActivity.this).inflate(R.layout.dialog_message, null);
        reconnectView = LayoutInflater.from(BrahmaMainActivity.this).inflate(R.layout.dialog_reconnect, null);

        MaterialAlertDialogBuilder builder2 = new MaterialAlertDialogBuilder(BrahmaMainActivity.this);
        builder2.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder2.setTitle("Reset password features would be open in the futures.");
        builder2.setView(messageView);
        message_dialog = builder2.create();

        reconnect_tv = (TextView) reconnectView.findViewById(R.id.message_tv);

        MaterialAlertDialogBuilder builder3 = new MaterialAlertDialogBuilder(BrahmaMainActivity.this);
        builder3.setView(reconnectView);
        reconnect_dialog = builder3.create();

//        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Apportal_output.txt");
//        if (file.exists()) {
//            boolean result2 = file.delete();
//            Log.d(TAG, "MediaRecorderHandler delete: " + result2);
//        }
//        try {
//            boolean result3 = file.createNewFile();
//            Log.d(TAG, "MediaRecorderHandler createNewFile: " + result3);
//        } catch (IOException e) {
//            Log.i(TAG, "create file failed");
//            throw new IllegalStateException("create file failed" + file.toString());
//        }

        if (isOpenPermission()) {
            isSelectFile = false;
        } else {
            isSelectFile = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        15151);
            } else {
                ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CAMERA},
                        15151);

            }
        }

        for (int i = 1; i < 3; i++) {
            if (dbHandler.getConnectionInfo(i) == null) {
                ConnectionInfo connectionInfo = new ConnectionInfo(0, Constants.CONN_DESC[i], Constants.USER[i], Constants.CONN_HOST, Constants.DEFAULT_PORT, 1, 1, "", 0, "");
                dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
            }
        }

        MyDBHelper dbHelper = new MyDBHelper(this);
        itemBio = new ItemBio(dbHelper.getDatabase(this));

        if (itemBio.getCount() == 0) {
            itemBio.sample();
        }
        if (itemBio.getCount_login() == 0) {
            itemBio.sample2();
        }

        Log.d("FCM", "BrahmaLogin Activity >>>>> intent action:" + getIntent().getExtras());
        if (intent.getAction() != null) {
            if (intent.getAction().equals("NOTIFICATION_ACTION")) {
                packageName = itemBio.get_login(1).getPackageName();

                normalUsername = itemBio.get_login(1).getUsername();
                normalPassword = itemBio.get_login(1).getPassword();
                normalIP = itemBio.get_login(1).getIp();
                normalPort = itemBio.get_login(1).getPort();
                AuthData.setPd(intent.getStringExtra("password"));

                if (packageName.length() == 0) {
                    packageName = null;
                }

                Log.d(TAG, "normalUsername : " + normalUsername);
                Log.d(TAG, "normalPassword : " + normalPassword);
                Log.d(TAG, "normalIP : " + normalIP);
                Log.d(TAG, "normalPort : " + normalPort);

                if (!normalUsername.isEmpty()
                        && !normalPassword.isEmpty()
                        && !normalIP.isEmpty()
                        && !normalPort.isEmpty()) {
                    mAuthContext.acquireToken(getActivity(), RESOURCE_ID, CLIENT_ID, REDIRECT_URI, PromptBehavior.Auto, getAuthInteractiveCallback());
                    ad_login.performClick();

                }
            } else if (intent.getAction().equals("URL_LOGIN_ACTION")) {
                packageName = itemBio.get_login(1).getPackageName();

                normalUsername = itemBio.get_login(1).getUsername();
                normalPassword = itemBio.get_login(1).getPassword();
                normalIP = itemBio.get_login(1).getIp();
                normalPort = itemBio.get_login(1).getPort();
                AuthData.setPd(intent.getStringExtra("password"));

                if (packageName.length() == 0) {
                    packageName = null;
                }
                if (!normalUsername.isEmpty()
                        && !normalPassword.isEmpty()
                        && !normalIP.isEmpty()
                        && !normalPort.isEmpty()) {
                    mAuthContext.acquireToken(getActivity(), RESOURCE_ID, CLIENT_ID, REDIRECT_URI, PromptBehavior.Auto, getAuthInteractiveCallback());
                    ad_login.performClick();
                }

            }
        } else {
            packageName = null;
            normalUsername = null;
            normalPassword = null;
            normalIP = null;
            normalPort = null;
        }

        keyboardHeightProvider = new KeyboardHeightProvider(this);
        language = Locale.getDefault().getLanguage();
        View view2 = findViewById(R.id.rl);
        view2.post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });

        //remove ShortcutBadger
        ShortcutBadger.removeCount(context);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            getNotchParams();
//        }

        boolean isAllScreen = hasNotchInScreenOfAndroidP(view2);
        Log.d(TAG, "isAllScreen :" + isAllScreen);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        registration_id = task.getResult().getToken();
                        Log.d(TAG, "registration_id : " + registration_id);
                    }
                });
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(BrahmaMainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                //帶入上次記憶的帳號密碼後直接登入
                Message msg = new Message();
                msg.what = 5;
                mHandler.sendMessage(msg);
                normalUsername = itemBio.get(1).getbioUsername();
                normalPassword = itemBio.get(1).getbioPassword();
                normalIP = itemBio.get(1).getbioIP();
                normalPort = itemBio.get(1).getbioPort();
                loginVMI();

                Toast.makeText(getApplicationContext(), R.string.biometric_dialog_state_succeeded, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), R.string.biometric_dialog_state_failed,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_dialog_login))
                .setSubtitle(getString(R.string.biometric_dialog_subtitle))
                .setNegativeButtonText(getString(R.string.biometric_dialog_use_password))
                .build();

//        try {
//            generateSecretKey(new KeyGenParameterSpec.Builder(
//                    KEY_NAME,
//                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                    .setUserAuthenticationRequired(true)
//                    .setInvalidatedByBiometricEnrollment(true)
//                    .build());
//        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }

        //show face id button
        if (itemBio.get(1).getUsingBio().equals("true")) {
            image_touchid.setVisibility(View.VISIBLE);
            tv_touchid.setVisibility(View.VISIBLE);
        } else {
            image_touchid.setVisibility(View.INVISIBLE);
            tv_touchid.setVisibility(View.INVISIBLE);
        }
    }

    private void initSSLSocketFactory() {
        KeyStore localTrustStore = null;
        try {
            localTrustStore = KeyStore.getInstance("BKS");
            InputStream in = getResources().openRawResource(R.raw.mykeystore);
            try {
                localTrustStore.load(in, Constants.TRUSTSTORE.toCharArray());
                in.close();
            } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
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
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private boolean isOpenPermission() {

        int permission1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permission4 = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int permission5 = 0;
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED && permission4 == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isOpenPermission_necessary() {
        int permission3 = ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        int permission4 = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        int permission5 = ActivityCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int permission1 = 0;
        int permission2 = 0;
        int permission6 = 0;
        int permission7 = 0;
        int permission8 = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permission8 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        return permission3 == PackageManager.PERMISSION_GRANTED && permission4 == PackageManager.PERMISSION_GRANTED && permission5 == PackageManager.PERMISSION_GRANTED && permission8 == PackageManager.PERMISSION_GRANTED;
    }


    private void callBiometric() {
        if (itemBio.get(1).getUsingBio().equals("true")) {
            //check biometric is available
            BiometricManager biometricManager = BiometricManager.from(this);
            switch (biometricManager.canAuthenticate()) {
                case BiometricManager.BIOMETRIC_SUCCESS:

                    biometricPrompt.authenticate(promptInfo);
                    Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    Toast.makeText(this, R.string.BIOMETRIC_ERROR_NO_HARDWARE, Toast.LENGTH_LONG).show();
                    Log.e("MY_APP_TAG", "No biometric features available on this device.");
                    break;
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    Toast.makeText(this, R.string.BIOMETRIC_ERROR_HW_UNAVAILABLE, Toast.LENGTH_LONG).show();
                    Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    Toast.makeText(this, R.string.BIOMETRIC_ERROR_NONE_ENROLLED, Toast.LENGTH_LONG).show();
                    Log.e("MY_APP_TAG", "The user hasn't associated any biometric credentials with their account.");
                    break;
            }
        } else {
            Log.d("isUsingBio", "no bio data");
            Toast.makeText(BrahmaMainActivity.this, R.string.no_bio_record, Toast.LENGTH_LONG).show();
        }
    }

    public boolean hasNotchInScreenOfAndroidP(View context) {
        final boolean[] ret = {false};
        final View view = context;
        if (Build.VERSION.SDK_INT >= 28) {
            if (context != null) {
                context.post(new Runnable() {
                    @Override
                    public void run() {
                        WindowInsets windowInsets = view.getRootWindowInsets();
                        if (windowInsets == null) {
                            ret[0] = false;
                        } else {
                            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                            if (displayCutout == null) {
                                ret[0] = false;
                            } else {
                                List<Rect> rects = displayCutout.getBoundingRects();
                                SafeInsetTop = displayCutout.getSafeInsetTop();
                                SafeInsetBottom = displayCutout.getSafeInsetBottom();
                                ret[0] = rects.size() != 0;
                            }
                        }
                    }
                });
            }
        }
        return ret[0];
    }

//    @TargetApi(28)
//    public void getNotchParams() {
//        final View decorView = getWindow().getDecorView();
//        decorView.post(new Runnable() {
//            @Override
//            public void run() {
//                WindowInsets windowInsets = decorView.getRootWindowInsets();
//                if (windowInsets != null) {
//                    DisplayCutout displayCutout = decorView.getRootWindowInsets().getDisplayCutout();
//                    if (displayCutout != null) {
//                        Log.e("TAG", "SafeInsetLeft:" + displayCutout.getSafeInsetLeft());
//                        Log.e("TAG", "SafeInsetRight:" + displayCutout.getSafeInsetRight());
//                        Log.e("TAG", "SafeInsetTop:" + displayCutout.getSafeInsetTop());
//                        Log.e("TAG", "SafeInsetBottom:" + displayCutout.getSafeInsetBottom());
//
//                        SafeInsetTop = displayCutout.getSafeInsetTop();
//                        SafeInsetBottom = displayCutout.getSafeInsetBottom();
//
//                        List<Rect> rects = displayCutout.getBoundingRects();
//                        if (rects.size() == 0) {
//                            Log.e("TAG", "不是瀏海屏");
//                        } else {
//                            Log.e("TAG", "瀏海屏数量:" + rects.size());
//                            for (Rect rect : rects) {
//                                Log.e("TAG", "瀏海屏區域：" + rect);
//                            }
//                        }
//                    }
//                }
//            }
//        });
//    }

    private void onCallGraphClicked() {
        Log.d(TAG, "onCallGraphClicked");
        mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO);
    }

    private AuthenticationCallback<AuthenticationResult> getAuthSilentCallback() {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {

                if (authenticationResult == null || TextUtils.isEmpty(authenticationResult.getAccessToken())
                        || authenticationResult.getStatus() != AuthenticationResult.AuthenticationStatus.Succeeded) {
                    Log.d(TAG, "Silent acquire token Authentication Result is invalid, retrying with interactive");
                    /* retry with interactive */
                    mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO);
                    return;
                }
                /* Successfully got a token, call graph now */
                Log.d(TAG, "Successfully authenticated");

                /* Store the mAuthResult */
                mAuthResult = authenticationResult;

                Log.d(TAG, "Successfully Silent authenticated");
                Log.d(TAG, "ID Silent Token: " + authenticationResult.getIdToken());
                Log.d(TAG, "Access Silent Token: " + authenticationResult.getAccessToken());//important info!!!
                Log.d(TAG, "Email: " + authenticationResult.getUserInfo().getDisplayableId());//important info!!!

                userEmail = authenticationResult.getUserInfo().getDisplayableId();
                adToken = authenticationResult.getAccessToken();
                normalUsername = userEmail;
                normalPassword = "";
                loginVMI();
            }

            @Override
            public void onError(Exception exception) {
                /* Failed to acquireToken */
                Log.e(TAG, "Authentication failed: " + exception.toString());
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                if (exception instanceof AuthenticationException) {
                    AuthenticationException authException = ((AuthenticationException) exception);
                    ADALError error = authException.getCode();
                    logHttpErrors(authException);
                    /*  Tokens expired or no session, retry with interactive */
                    if (error == ADALError.AUTH_REFRESH_FAILED_PROMPT_NOT_ALLOWED) {
                        mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO);
                    } else if (error == ADALError.NO_NETWORK_CONNECTION_POWER_OPTIMIZATION) {
                        /* Device is in Doze mode or App is in stand by mode.
                           Wake up the app or show an appropriate prompt for the user to take action
                           More information on this : https://github.com/AzureAD/azure-activedirectory-library-for-android/wiki/Handle-Doze-and-App-Standby */
                        Log.e(TAG, "Device is in doze mode or the app is in standby mode");
                    }
                    return;
                }
                /* Attempt an interactive on any other exception */
                mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO);
            }
        };
    }

    private void logHttpErrors(AuthenticationException authException) {
        int httpResponseCode = authException.getServiceStatusCode();
        Log.d(TAG, "HTTP Response code: " + authException.getServiceStatusCode());
        if (httpResponseCode < 200 || httpResponseCode > 300) {
            // logging http response headers in case of a http error.
            HashMap<String, List<String>> headers = authException.getHttpResponseHeaders();
            if (headers != null) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    sb.append(entry.getKey());
                    sb.append(":");
                    sb.append(entry.getValue().toString());
                    sb.append("; ");
                }
                Log.e(TAG, "HTTP Response headers: " + sb.toString());
            }
        }
    }

    /* Callback used for interactive request.  If succeeds we use the access
     * token to call the Microsoft Graph. Does not check cache
     */
    private AuthenticationCallback<AuthenticationResult> getAuthInteractiveCallback() {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                if (authenticationResult == null || TextUtils.isEmpty(authenticationResult.getAccessToken())
                        || authenticationResult.getStatus() != AuthenticationResult.AuthenticationStatus.Succeeded) {
                    Log.e(TAG, "Authentication Result is invalid");
                    return;
                }
                /* Successfully got a token, call graph now */

                Log.d(TAG, "Successfully authenticated");
                Log.d(TAG, "ID Token: " + authenticationResult.getIdToken());
                Log.d(TAG, "Access Token: " + authenticationResult.getAccessToken());//important info!!!

                mAuthResult = authenticationResult;

                /* Store User id to SharedPreferences to use it to acquire token silently later */
//                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                preferences.edit().putString(USER_ID, authenticationResult.getUserInfo().getUserId()).apply();

                /* call graph */
                //callGraphAPI();

//                /* update the UI to post call graph state */
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateSuccessUI();
//                    }
//                });
                /* set the sIntSignInInvoked boolean back to false  */
                mAuthContext.acquireTokenSilentAsync(RESOURCE_ID, CLIENT_ID, authenticationResult.getUserInfo().getUserId(), getAuthSilentCallback());
                sIntSignInInvoked.set(false);

            }

            @Override
            public void onError(Exception exception) {
                /* Failed to acquireToken */
                Log.e(TAG, "Authentication failed: " + exception.toString());
                Message msg = new Message();
                msg.what = 1;
                mHandler.sendMessage(msg);
                if (exception instanceof AuthenticationException) {
                    ADALError error = ((AuthenticationException) exception).getCode();
                    if (error == ADALError.AUTH_FAILED_CANCELLED) {
                        Log.e(TAG, "The user cancelled the authorization request");
                    } else if (error == ADALError.AUTH_FAILED_NO_TOKEN) {
                        // In this case ADAL has found a token in cache but failed to retrieve it.
                        // Retry interactive with Prompt.Always to ensure we do an interactive sign in
                        mAcquireTokenHandler.sendEmptyMessage(MSG_INTERACTIVE_SIGN_IN_PROMPT_ALWAYS);
                    } else if (error == ADALError.NO_NETWORK_CONNECTION_POWER_OPTIMIZATION) {
                        /* Device is in Doze mode or App is in stand by mode.
                           Wake up the app or show an appropriate prompt for the user to take action
                           More information on this : https://github.com/AzureAD/azure-activedirectory-library-for-android/wiki/Handle-Doze-and-App-Standby */
                        Log.e(TAG, "Device is in doze mode or the app is in standby mode");
                    }
                }
                /* set the sIntSignInInvoked boolean back to false  */
                sIntSignInInvoked.set(false);
            }
        };
    }

    /*** 點擊button進行指紋辨識動作 */
    public void onClick_touchID(View v) {
        callBiometric();
    }

    public void onClick_tempLogin(View v) {
        if (sharedPreferences.getString("normal_IP", "") == "") {
            normalIP = tempIP;
        } else {
            normalIP = sharedPreferences.getString("normal_IP", tempIP);
        }

        if (sharedPreferences.getString("normal_Port", "") == "") {
            normalPort = tempPort;
        } else {
            normalPort = sharedPreferences.getString("normal_Port", tempPort);
        }
        Log.d(TAG, "normalIP >>>>> " + normalIP);
        Log.d(TAG, "normalPort >>>>> " + normalPort);
        loginVMI();
    }

    public void onClick_adlogin(View v) {
        //Check Permission
        boolean isPermissionReady;
        if (isOpenPermission_necessary()) {
            isPermissionReady = true;
            isSelectFile = false;
        } else {
            isPermissionReady = false;
            isSelectFile = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        15151);
            } else {
                ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        15151);

            }
        }

        if (!isPermissionReady) {
            Toast.makeText(this, getResources().getString(R.string.please_open_permission), Toast.LENGTH_LONG).show();
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            myIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(myIntent, 101);
        } else {
            //1.檢查設備是否合法
            startProgressDialog(getResources().getString(R.string.appRTC_toast_connection_start));
            if (sharedPreferences.getString("normal_IP", "") == "") {
                normalIP = tempIP;
            } else {
                normalIP = sharedPreferences.getString("normal_IP", tempIP);
            }

            if (sharedPreferences.getString("normal_Port", "") == "") {
                normalPort = tempPort;
            } else {
                normalPort = sharedPreferences.getString("normal_Port", tempPort);
            }
            Log.d(TAG, "normalIP >>>>> " + normalIP);
            Log.d(TAG, "normalPort >>>>> " + normalPort);

            //test
            normalPassword = "Aa123456";
            normalUsername = "yiwenli@itri.org.tw";
            loginVMI();
//            new checkDeviceTask().execute(device_id);
        }
    }

    /*** 點擊brahma icon進入QR code Scanner模式 */
    public void onClick_setting(View v) {
        Intent intent = new Intent();
        intent.setClass(BrahmaMainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("LifeCycle", "BrahmaMainActivity onDestroy!!!");
        hideSoftKeyboard();
        dbHandler.close();
//        itemBio.close();
//        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(myDialogReceiver);
        keyboardHeightProvider.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d("LifeCycle", "BrahmaMainActivity onBackPressed!!!");
        super.onBackPressed();
    }

    /*** 一般登入方式 */
    public void loginVMI() {
        Log.d(TAG, "normalUsername >>>>> " + normalUsername);
        Log.d(TAG, "normalIP >>>>> " + normalIP);
        Log.d(TAG, "normalPort >>>>> " + normalPort);
        Log.d(TAG, "normalPassword >>>>> " + normalPassword);

        if (!Settings.canDrawOverlays(this)) {
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            myIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(myIntent, 101);
        } else {
            AuthData.setPd(normalPassword);
            AuthData.setAD(false);
            AppRTCClient.type = "vmi";
            userEmail = normalUsername;
            long result;
            ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);

            if (connectionInfo != null) {
                connectionInfo = new ConnectionInfo(2, "userMode", normalUsername, normalIP, Integer.parseInt(normalPort), 1, 1, "", 0, "vmi");
                result = dbHandler.updateConnectionInfo(connectionInfo);
                Log.d(TAG, "Update ConnectionInfo");
            } else {
                connectionInfo = new ConnectionInfo(2, "userMode", normalUsername, normalIP, Integer.parseInt(normalPort), 1, 1, "", 0, "vmi");
                result = dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
                Log.d(TAG, "Add new ConnectionInfo");
            }

            // exit and resume previous activity, report results in the intent
            if (result > -1) {
                // we have updated this ConnectionInfo; if session info is stored for this ConnectionInfo, remove it
                dbHandler.clearSessionInfo(connectionInfo);
            }
            deviceInfo.setUser(normalUsername);
            if (connectionInfo != null) {
                try {
                    // if we allow use of desktop mode, start the connection; otherwise take us to this connection's app list
                    this.sendRequestCode = REQUEST_STARTVIDEO;
                    machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                    getUserInfo(connectionInfo);
//                    stopProgressDialog();
                } catch (Exception e) {
                    Log.e(TAG, "Error:" + e.getMessage());
                }
            }
        }
    }


    private void getUserInfo(ConnectionInfo connectionInfo) {
        new LoginTask().execute(connectionInfo);
    }

    /*** 一般AppStreaming登入 */
    public void loginApp() {
        AuthData.setPd(normalPassword);
        AuthData.setAD(false);//判定連線是否使用AD
        AppRTCClient.type = "APP";//need to check
        techDayType = "";

        long result;
        ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(1);
        if (connectionInfo != null) {
            connectionInfo = new ConnectionInfo(1, "appMode", normalUsername, normalIP, Integer.parseInt(normalPort), 1, 1, "", 0, "appstreaming");
            result = dbHandler.updateConnectionInfo(connectionInfo);
            Log.d(TAG, "Update ConnectionInfo");
        } else {
            connectionInfo = new ConnectionInfo(1, "appMode", normalUsername, normalIP, Integer.parseInt(normalPort), 1, 1, "", 0, "appstreaming");
            // insert or update the ConnectionInfo in the database
            result = dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
            Log.d(TAG, "Add new ConnectionInfo");
        }

        // exit and resume previous activity, report results in the intent
        if (result > -1) {
            // we have updated this ConnectionInfo; if session info is stored for this ConnectionInfo, remove it
            dbHandler.clearSessionInfo(connectionInfo);
        }

        //通知登入訊息
        deviceInfo.setUser(normalUsername);

//        Intent intent = new Intent(BrahmaMainActivity.this, SlidingTab.class);
        Intent intent = new Intent(BrahmaMainActivity.this, GridActivity.class);
        intent.putExtra("cName", normalUsername);
        intent.putExtra("connectionID", connectionInfo.getConnectionID());
        // start the activity and expect a result intent when it is finished
        machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
        startActivity(intent);

    }

    // activity returns
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("LifeCycle", "BrahmaMainActivity onActivityResult!!!" + requestCode);
        //aes 128
        String password = "qwertyuiopasdfgh";
        String iv = "1234567890abcdef";
        EncryptionUtils eu = new EncryptionUtils(password, iv);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && requestCode == REQUEST_SCQNNERQRCODE) {
            if (result.getContents() == null) {
                Log.d(TAG, "Scanner failed!!!");
            } else {
                Log.d(TAG, "result.getContents():" + result.getContents());
                try {
                    try {
                        String decrypt = new String(eu.decrypt(new String(result.getContents().getBytes())));
                        parseQRcodePath(decrypt);
                    } catch (IllegalBlockSizeException exception) {
                        Log.d(TAG, "IllegalBlockSizeException:" + exception.getMessage());
                        //判斷是128解密錯誤,換用base64解密看看
                        String strBase64 = new String(Base64.decode(result.getContents().getBytes(), Base64.DEFAULT));
                        Log.d(TAG, "Base64 decode >>>" + strBase64);
                        try {
                            parseQRcodePath(strBase64);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Log.d(TAG, "qr :" + result.getContents());
                    Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_UPLOADQRCODE) {
            //藉由requestCode判斷是否為開啟相機或開啟相簿而呼叫的，且data不為null
            if (data != null) {
                //取得照片路徑uri
                Uri uri = data.getData();
                ContentResolver cr = this.getContentResolver();
                InputStream input = null;
                try {
                    //讀取照片，型態為Bitmap
                    assert uri != null;
                    input = cr.openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (input != null) {
                        input.close();
                    }
                    BarcodeDetector detector =
                            new BarcodeDetector.Builder(getApplicationContext())
                                    .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                                    .build();
                    if (!detector.isOperational()) {
                        Log.i(TAG, "Could not set up the detector!");
                        return;
                    }
                    try {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<Barcode> barcodes = detector.detect(frame);
                        Barcode thisCode = barcodes.valueAt(0);
                        Log.i(TAG, "scannerQR value:" + thisCode.rawValue);
                        try {
                            String decrypt = new String(eu.decrypt(new String(thisCode.rawValue)));
                            parseQRcodePath(decrypt);
                        } catch (IllegalBlockSizeException exception) {
                            Log.d(TAG, "IllegalBlockSizeException:" + exception.getMessage());
                            //判斷是128解密錯誤,換用base64解密看看
                            String strBase64 = new String(Base64.decode(thisCode.rawValue.getBytes(), Base64.DEFAULT));
                            Log.i(TAG, "Base64 decode >>>" + strBase64);
                            try {
                                parseQRcodePath(strBase64);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.d(TAG, "scannerQR error:" + e.getMessage());
                        Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e) {
                    Log.d(TAG, "FileNotFoundException:" + e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == 1001) {
            //ad auth
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }
        busy = false;
        isSelectFile = false;

        if (requestCode == REQUEST_PROGREEDIALOG) {
            Log.d("LifeCycle", "REQUEST_PROGREEDIALOG:" + REQUEST_PROGREEDIALOG);
            stopProgressDialog();
        } else // fall back to superclass method
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final Activity activity = this;

        if (grantResults.length > 0
                && requestCode == 15153
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //取得經緯度
            try {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                assert lm != null;
                Location locationGPS = lm.getLastKnownLocation(GPS_PROVIDER); //使用GPS定位座標
                Location locationNetwork = lm.getLastKnownLocation(NETWORK_PROVIDER); //使用Network定位座標

                if (locationGPS != null) {
                    //try catch NullPointerException
                    try {
                        assert locationNetwork != null;
                        Longitude = locationNetwork.getLongitude(); //取得經度
                        Latitude = locationNetwork.getLatitude(); //取得緯度
                        Log.d(TAG, "Longitude:" + Longitude + " ,Latitude:" + Latitude);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else if (locationNetwork != null) {
                    //try catch NullPointerException
                    try {
                        Longitude = locationNetwork.getLongitude(); //取得經度
                        Latitude = locationNetwork.getLatitude(); //取得緯度
                        Log.d(TAG, "Longitude:" + Longitude + " ,Latitude:" + Latitude);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "location is NULL!");
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "location permission denied");
        }

        //camera permission
        if (grantResults.length > 0 && requestCode == 15158) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "camera permission granted");
                isSelectFile = true;
                //scan qrcode
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("Scan");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(false);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();

            } else {
                Log.d(TAG, "camera permission denied");
            }
            isSelectFile = false;
        }

        //camera permission
        if (grantResults.length > 0 && requestCode == 15159) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "camera permission granted");
                isSelectFile = true;
            } else {
                Log.d(TAG, "camera permission denied");
            }
            isSelectFile = false;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void parseQRcodePath(String strBase64) {
        try {
            JSONObject jsonObj = new JSONObject(strBase64);
            if (!jsonObj.isNull("path") && !jsonObj.isNull("id")) {
                qrcodePath = jsonObj.getString("path");
                qrcodeID = jsonObj.getString("id");

                String[] path_string1 = qrcodePath.split("://");
                String[] path_string2 = path_string1[1].split(":");

                normalIP = path_string2[0];
                normalPort = path_string2[1];

                //update connectioninfo
                ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(1);
                if (connectionInfo != null) {
                    connectionInfo = new ConnectionInfo(1, "appMode", qrcodeID, normalIP, Integer.parseInt(normalPort), 1, 1, "", 0, "appstreaming");
                    result = dbHandler.updateConnectionInfo(connectionInfo);
                    Log.d(TAG, "Update ConnectionInfo result "+result);
                } else {
                    connectionInfo = new ConnectionInfo(1, "appMode", qrcodeID, normalIP, Integer.parseInt(normalPort), 1, 1, "", 0, "appstreaming");
                    result = dbHandler.insertConnectionInfo(connectionInfo);
                    Log.d(TAG, "Add new ConnectionInfo result"+result);
                }
                new QRcodeTask().execute(jsonObj);
            } else {
                Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();
        }

    }

    public void onResume() {
        super.onResume();
        OpenAppMode = false;
        Log.d("LifeCycle", "BrahmaMainActivity onResume!!!");
        // 動態調整解析度
        if (sharedPreferences.getString("resolution_ratio", "0.5") != "")
            resolution_ratio = Double.parseDouble(sharedPreferences.getString("resolution_ratio", "0.5"));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        widthPixels = (int) ((dm.widthPixels) * resolution_ratio);
        heightPixels = (int) ((dm.heightPixels - SafeInsetTop - SafeInsetBottom) * resolution_ratio);
        DPI = (int) (getResources().getDisplayMetrics().densityDpi * resolution_ratio);

        Log.d(TAG, "onResume resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "display:" + dm.widthPixels + " x " + dm.heightPixels);
        Log.d(TAG, "螢幕解析度(修正後):" + widthPixels + " x " + heightPixels);
        Log.d(TAG, "DPI(修正後): " + DPI);


        keyboardHeightProvider.setKeyboardHeightObserver(this);

//        MyDBHelper dbHelper = new MyDBHelper(this);
//        ItemBio itemBio = new ItemBio(dbHelper.getDatabase(this));
        //show face id button
        if (itemBio.get(1).getUsingBio().equals("true")) {
            image_touchid.setVisibility(View.VISIBLE);
            tv_touchid.setVisibility(View.VISIBLE);
        } else {
            image_touchid.setVisibility(View.INVISIBLE);
            tv_touchid.setVisibility(View.INVISIBLE);
        }
//        itemBio.close();
    }

    public void onPause() {
        super.onPause();
        Log.d("LifeCycle", "BrahmaMainActivity onPause!!!");
        mDialog.dismiss();
        hideSoftKeyboard();

        // 動態調整解析度
        if (sharedPreferences.getString("resolution_ratio", "0.5") != "")
            resolution_ratio = Double.parseDouble(sharedPreferences.getString("resolution_ratio", "0.5"));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        widthPixels = (int) ((dm.widthPixels) * resolution_ratio);
        heightPixels = (int) ((dm.heightPixels - SafeInsetTop - SafeInsetBottom) * resolution_ratio);
        DPI = (int) (getResources().getDisplayMetrics().densityDpi * resolution_ratio);

        Log.d(TAG, "onResume resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "螢幕解析度(修正後):" + widthPixels + " x " + heightPixels);
        Log.d(TAG, "DPI(修正後): " + DPI);
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    private void hideSoftKeyboard() {
        Log.d(TAG, "hideSoftKeyboard ");
        Log.d(TAG, "getCurrentFocus() >>>>> " + getCurrentFocus());
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //點擊空白區域隱藏鍵盤
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert mInputMethodManager != null;
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    private void authPrompt(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "BrahmaActivity authPrompt");
        if (busy)
            return;
        busy = true;
        startAppRTCWithAuth(connectionInfo);
    }

    // prepares a JSONObject using the auth dialog input, then starts the AppRTC connection
    private void startAppRTCWithAuth(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "BrahmaActivity startAppRTCWithAuth");
        String arg = String.format("{user_name: '%s'}", connectionInfo.getUsername());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(arg);
        } catch (JSONException e) {
            Log.e(TAG, "startAppRTCWithAuth failed:", e);
            return;
        }

        // store the user credentials to be used by the AppRTCClient
        AuthData.setAuthJSON(connectionInfo, jsonObject);
        // start the connection
        startAppRTC(connectionInfo);
    }

    // Start the AppRTC service and allow child to start correct AppRTC activity
    protected void startAppRTC(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "BrahmaActivity startAppRTC");
        // if the session service is running for a different connection, stop it
        boolean stopService = SessionService.getConnectionID() != connectionInfo.getConnectionID() && SessionService.getState() != StateMachine.STATE.NEW;
        if (stopService)
            stopService(new Intent(this, SessionService.class));

        // make sure the session service is running for this connection
        if (stopService || SessionService.getState() == StateMachine.STATE.NEW) {
            startService(new Intent(this, SessionService.class).putExtra("connectionID", connectionInfo.getConnectionID()));
//                startService(new Intent(this, NetMService.class).putExtra("connectionID", connectionInfo.getConnectionID()));
        }
        // after we make sure the service is started, we can start the AppRTC actions for this activity
        afterStartAppRTC(connectionInfo);
    }

    private void afterStartAppRTC(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "BrahmaMainActivity afterStartAppRTC");
        Intent intent = new Intent();
        if (sendRequestCode == REQUEST_STARTVIDEO) {
            Log.d(TAG, "packageName >>>>> " + packageName);
            intent.putExtra("pkgName", packageName);
            packageName = null;
            intent.setClass(BrahmaMainActivity.this, CallActivity.class);
        }
        intent.putExtra("connectionID", connectionInfo.getConnectionID());
        startActivityForResult(intent, sendRequestCode);
    }

    private void startProgressDialog(String msg) {
        Log.d("uououo", "startProgressDialog");
        progressDialog = new ProgressDialog(BrahmaMainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        progressDialog.show();
    }

    private void stopProgressDialog() {
        Log.d("uououo", "stopProgressDialog");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        String orientationLabel = orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape";
        KBheight = (int) (height * resolution_ratio);
        if (KBheight < 0)
            KBheight = 0;

        Log.i(TAG, "onKeyboardHeightChanged in pixels: " + height + " " + orientationLabel);
        Log.i(TAG, "onKeyboardHeightChanged KBheight: " + KBheight);
    }


    class LoginTask extends AsyncTask<ConnectionInfo, String, JSONObject> {
        // <傳入參數, 處理中更新介面參數, 處理後傳出參數>
        String network = "";
        String device = "";
        String os = "";
        String uriAPI = "";
        JSONObject http_data;
        int returnVal;
        ConnectionInfo myConnectionInfo;
        String msg = "";

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        protected void onPreExecute() {
            uriAPI = "https://" + normalIP + ":" + normalPort + "/login";
            Log.d(TAG, "[LoginTask] uri:" + uriAPI);
            http_data = new JSONObject();
            try {
                ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                assert gConnMgr != null;
                NetworkInfo tInfo = gConnMgr.getActiveNetworkInfo();
                if (tInfo != null) {
                    network = toLowerCase(tInfo.getTypeName());
                } else {
                    network = "wifi";
                }
                device = Build.BRAND + " " + Build.MODEL;
                os = "Android " + Build.VERSION.RELEASE;
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
//            //temp
//            if (!Username.getText().toString().matches("")) {
//                normalUsername = Username.getText().toString();
//            }
//            if (!deviceid.getText().toString().matches("")) {
//                device_id = deviceid.getText().toString();
//            }
            String email = normalUsername.substring(0, normalUsername.indexOf("@"));

            try {
                http_data.put("user_name", email);
                http_data.put("device_token", device_id);

                http_data.put("user_name", "60631");
                http_data.put("device_token", "1");

                http_data.put("user_name", "yiwenli@itri.org.tw");
                http_data.put("password", "Aa123456");

                http_data.put("device", device);
                http_data.put("os", os);
                http_data.put("longitude", String.valueOf(Longitude));
                http_data.put("latitude", String.valueOf(Latitude));
                http_data.put("resolution", resolution);
                http_data.put("network", network);
                http_data.put("media", "standard");
                http_data.put("f_version", myAppVerison);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "[LoginTask] http_data:" + http_data.toString());
        }

        @Override
        protected JSONObject doInBackground(ConnectionInfo... connectionInfo) {
            myConnectionInfo = connectionInfo[0];
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

            Request request = new Request.Builder()
                    .url(uriAPI)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("brahma-lang", language)
                    .post(requestBody)
                    .build();

            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "error:" + e.getMessage());
                    returnVal = R.string.https_no_response;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    responseCode = response.code();
                    Log.i(TAG, "[LoginTask] response body :" + jsonData);
                    Log.i(TAG, "[LoginTask] response code :" + responseCode);

                    try {
                        jsonResponse = new JSONObject(jsonData);
                        msg = jsonResponse.getString("msg");
                        String status_code = jsonResponse.getString("status_code");

                        Log.d(TAG, "[LoginTask] status_code:" + status_code);
                        Log.d(TAG, "[LoginTask] msg:" + msg);

                        if (responseCode == 200) {
                            if (status_code.equals("20000")) {
                                //judge type
                                String type = jsonResponse.getString("type");
                                if (type.equals("vmi")) {
                                    _login = true;
                                    authPrompt(myConnectionInfo);
                                    OpenAppMode = false;
                                } else {
//                                    appListObject = jsonResponse.getJSONObject("data");
                                    appListArray = jsonResponse.getJSONArray("applist");
                                    user_id = jsonResponse.getString("user_id");
                                    performanceToken = jsonResponse.getString("token");
                                    loginApp();
                                    Log.d(TAG, "[LoginTask] appListArray:" + appListArray.toString());
                                }
                            } else {
                                Looper.prepare();
                                stopProgressDialog();
                                logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                                logToast.show();
                                Looper.loop();
                            }
                            login_type = "normal";
                        } else {
                            Message msg2 = new Message();
                            msg2.what = 3;
                            msg2.obj = msg;
                            mHandler.sendMessage(msg2);
                        }
                        Message msg2 = new Message();
                        msg2.what = 1;
                        mHandler.sendMessage(msg2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to parse JSON response:", e);
                        Message msg2 = new Message();
                        msg2.what = 1;
                        mHandler.sendMessage(msg2);
                    }
                }
            });
            return null;
        }
    }

    class QRcodeTask extends AsyncTask<JSONObject, String, JSONObject> {
        String uriAPI = "";
        JSONObject http_data = null;
        ConnectionInfo connectionInfo = null;
        String network = "";
        String device = "";
        String os = "";
        String msg = "";
        int returnVal;

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        protected void onPreExecute() {
            uriAPI = qrcodePath + "/loginByQrcode";
            Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
            http_data = new JSONObject();

            //fake connectioninfo
            connectionInfo = dbHandler.getConnectionInfo(2);
            try {
                ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                assert gConnMgr != null;
                NetworkInfo tInfo = gConnMgr.getActiveNetworkInfo();
                if (tInfo != null) {
                    network = toLowerCase(tInfo.getTypeName());
                } else {
                    network = "wifi";
                }
                device = Build.BRAND + " " + Build.MODEL;
                os = "Android " + Build.VERSION.RELEASE;
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            try {
                http_data.put("id", qrcodeID);
                http_data.put("device", device);
                http_data.put("os", os);
                http_data.put("longitude", String.valueOf(Longitude));
                http_data.put("latitude", String.valueOf(Latitude));
                http_data.put("resolution", resolution);
                http_data.put("network", network);
                http_data.put("media", "standard");
                http_data.put("device_token", device_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "[QRcodeTask] http_data:" + http_data.toString());
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
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

            Request request = new Request.Builder()
                    .url(uriAPI)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("brahma-lang", language)
                    .post(requestBody)
                    .build();

            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "error:" + e.getMessage());
                    returnVal = R.string.https_no_response;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    responseCode = response.code();
                    Log.i(TAG, "[QRcodeTask] response body :" + jsonData);
                    Log.i(TAG, "[QRcodeTask] response code :" + responseCode);

                    try {
                        jsonResponse = new JSONObject(jsonData);
                        msg = jsonResponse.getString("msg");
                        String status_code = jsonResponse.getString("status_code");

                        Log.d(TAG, "[QRcodeTask] status_code:" + status_code);
                        Log.d(TAG, "[QRcodeTask] msg:" + msg);
                        Log.d(TAG, "[QRcodeTask] jsonResponse:" + jsonResponse);

                        if (responseCode == 200) {
                            if (status_code.equals("20000")) {
                                //judge type
                                String type = jsonResponse.getString("type");
                                if (!jsonResponse.isNull("qrcodeInfo")) {
                                    qrcodeAPK = jsonResponse.getJSONObject("qrcodeInfo").getString("apk");
                                    qrcodeFileName = jsonResponse.getJSONObject("qrcodeInfo").getString("file_name");
                                    Log.d(TAG, "[QRcodeTask] qrcodeFileName >>>>> " + qrcodeFileName);
                                    Log.d(TAG, "[QRcodeTask] qrcodeFileName.length() >>>>> " + qrcodeFileName.length());
                                    if (qrcodeFileName.length() != 0) {
                                        packageName = qrcodeAPK + ";" + qrcodeFileName;
                                    } else {
                                        packageName = qrcodeAPK;
                                    }
                                }
                                if (!jsonResponse.isNull("server")) {
                                    normalIP = jsonResponse.getJSONObject("server").getString("host");
                                    normalPort = jsonResponse.getJSONObject("server").getString("port");
                                }

                                if (type.equals("vmi")) {
                                    _login = true;
                                    authPrompt(connectionInfo);
                                } else {
                                    //                                    appListObject = jsonResponse.getJSONObject("data");
                                    appListArray = jsonResponse.getJSONArray("applist");
                                    user_id = jsonResponse.getString("user_id");
                                    performanceToken = jsonResponse.getString("token");
                                    loginApp();
                                    Log.d(TAG, "[QRcodeTask] appListObject:" + appListArray);
                                }
                            } else {
                                Message msg2 = new Message();
                                msg2.what = 3;
                                msg2.obj = msg;
                                mHandler.sendMessage(msg2);
                            }
                            login_type = "qrcode";
                        } else {
                            Message msg2 = new Message();
                            msg2.what = 3;
                            msg2.obj = msg;
                            mHandler.sendMessage(msg2);
                        }
                        Message msg2 = new Message();
                        msg2.what = 1;
                        mHandler.sendMessage(msg2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to parse JSON response:", e);
                        Message msg2 = new Message();
                        msg2.what = 1;
                        mHandler.sendMessage(msg2);
                    }
                }
            });

            return null;
        }

    }

    /**
     * BroadcastReceiver
     */
    public class MyDialogReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MyDialogReceiver action:" + intent.getAction());
            stopProgressDialog();
        }
    }

//    public class MyBroadcastReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "AppRTCClient.auto_connect >>>>> " + AppRTCClient.auto_connect);
//            Bundle bundle = intent.getExtras();
//            assert bundle != null;
//            times = bundle.getInt("reConnectTimes");
//            myState = bundle.getString("myState");
//            boolean auto_connect = bundle.getBoolean("auto_connect");
//            String loginType = bundle.getString("type");
//
//            machine.setState(StateMachine.STATE.ERROR, R.string.appRTC_toast_connection_finish, getResources().getString(R.string.appRTC_toast_connection_finish));
//            mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//
//            assert loginType != null;
//            if (StateMachine.STATE.ERROR == machine.getState() && loginType.equals("vmi") && auto_connect) {
//                if (times <= 5 && myState.equals("ERROR")) {
//                    Log.d(TAG, "doing reconnect");
//
//                    reconnect_tv.setText("Reconnect to Server ,please wait...\nReconnect times:" + times + "/5");
//                    reconnect_dialog.show();
//                    //如果未連線的話，mNetworkInfo會等於null
//                    ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);
//                    if(connectionInfo != null){
//                    authPrompt(connectionInfo);
//                    _login = true;
//
//                    }
//                } else {
//                    reconnect_dialog.dismiss();
//                    if (myState.equals("CONNECTED") || myState.equals("STARTED")) {
//                    } else
//                        Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG).show();
//                }
//            } else {
//                reconnect_dialog.dismiss();
//                times = 0;
//            }
//            AppRTCClient.auto_connect = true;
//        }
//    }


    class checkDeviceTask extends AsyncTask<String, String, String> {
        String uriAPI = "";
        JSONObject http_data = null;
        int returnVal;
        String msg = "";

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        protected void onPreExecute() {
            uriAPI = "https://" + normalIP + ":" + normalPort + "/custom/verDevice";
            Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
            http_data = new JSONObject();
            try {
//                http_data.put("device_token", device_id);
                http_data.put("device_token", "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "http_data:" + http_data.toString());
        }

        @Override
        protected String doInBackground(String... strings) {

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

            Request request = new Request.Builder()
                    .url(uriAPI)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("brahma-cusclitoken", "eyJhbGciOiJIUzI1NiJ9.N25EMUF2R3Nua3I0UkoxUEtDeXVNVmxIWjlHcA.XWZMc-MQhnPNVqkvWQE6ErVAUKzZXGt0oOrKWDpWxoE")
                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "error:" + e.getMessage());
                    returnVal = R.string.https_no_response;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    assert response.body() != null;
                    String jsonData = response.body().string();
                    responseCode = response.code();
                    Log.i(TAG, "response body :" + jsonData);
                    Log.i(TAG, "response code :" + responseCode);

                    try {
                        jsonResponse = new JSONObject(jsonData);
                        msg = jsonResponse.getString("msg");
                        String status_code = jsonResponse.getString("status_code");

                        Log.d(TAG, "status_code:" + status_code);
                        Log.d(TAG, "msg:" + msg);

                        if (responseCode == 200) {
                            onCallGraphClicked();
                        } else {
                            Message msg2 = new Message();
                            msg2.what = 3;
                            msg2.obj = msg;
                            mHandler.sendMessage(msg2);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to parse JSON response:", e);
                        Message msg2 = new Message();
                        msg2.what = 3;
                        msg2.obj = msg;
                        mHandler.sendMessage(msg2);
                    }
//                    stopProgressDialog();
                }
            });
            return null;
        }
    }
}
