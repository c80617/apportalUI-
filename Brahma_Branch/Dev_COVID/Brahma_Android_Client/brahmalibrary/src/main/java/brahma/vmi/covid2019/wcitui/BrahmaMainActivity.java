package brahma.vmi.covid2019.wcitui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.apprtc.AppRTCClient;
import brahma.vmi.covid2019.apprtc.CallActivity;
import brahma.vmi.covid2019.auth.AuthData;
import brahma.vmi.covid2019.biometriclib.BiometricPromptManager;
import brahma.vmi.covid2019.client.CrashHandler;
import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.common.Constants;
import brahma.vmi.covid2019.common.DatabaseHandler;
import brahma.vmi.covid2019.common.StateMachine;
import brahma.vmi.covid2019.database.ItemBio;
import brahma.vmi.covid2019.iperf.CommandHelper;
import brahma.vmi.covid2019.iperf.CommandResult;
import brahma.vmi.covid2019.keyboard.KeyboardHeightObserver;
import brahma.vmi.covid2019.keyboard.KeyboardHeightProvider;
import brahma.vmi.covid2019.net.EasySSLSocketFactory;
import brahma.vmi.covid2019.services.SessionService;
import brahma.vmi.covid2019.wcitui.devicelog.DeviceInfo;
import brahma.vmi.covid2019.widgets.APKVersionCodeUtils;
import io.fabric.sdk.android.Fabric;
import me.leolin.shortcutbadger.ShortcutBadger;

import static android.icu.lang.UCharacter.toLowerCase;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.appListObject;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.performanceToken;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.user_id;
import static brahma.vmi.covid2019.apprtc.CallActivity.KBheight;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.jsonResponse;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Latitude;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Longitude;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.isSelectFile;

/**
 * @file BrahmaMainActivity
 * @brief 使用者主要進行操作行為之activity
 * <p>
 * 提供使用者一般登入、
 * QRcode登入、
 * 接收與處理Notification登入訊息、
 * 判斷使用VMI mode或是AppStreaming mode、
 * 檢查輸入資訊是否正確、
 * 可開啟照相權限、
 * 進行網路iperf檢測、
 * 接收url redirect資訊、
 * 可Reset Password、
 * @author YiWen Li
 * @date 2019/07/12
 **/

/**
 * 版本更新日期:2019/07/24
 * 1.使用新版web API.
 * 2.給遠傳使用的版本
 * 3.拔除網路檢查
 * 4.加入reset password的按鈕
 * 5.error message的回傳還要確認一下
 * 6.加入local audiotrack
 * 7.加入url redirect
 * 8.使用API 28(for google play需求)
 * 9.加入iperf
 * 10.Android 9.0之後不支援BasicHttpPram,暫時解法:
 * 11.for sunon version(no local audio).
 */

public class BrahmaMainActivity extends AppCompatActivity implements Constants, KeyboardHeightObserver {
    public final static int RESULT_NEEDAUTH = 103; // need to authenticate
    public final static int RESULT_NEEDPASSWORDCHANGE = 104; // need to authenticate
    private static final int REQUEST_STARTVIDEO = 102;        // opens AppRTCVideoActivity
    private static final int REQUEST_SCQNNERQRCODE = 49374;
    private static final int REQUEST_UPLOADQRCODE = 49778;
    private static final int REQUEST_PROGREEDIALOG = 104;
    private static final int IPERF_ERROR = 1;
    private static final int IPERF_SCCESS = 2;
    private static final String IPERF_PATH = "/data/data/brahma.vmi.apportal/iperf";
    private static final String iperfServiceCmd = IPERF_PATH + " -s &";
    public static Context context = null;
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
    /*** copy from BrahamActivity*/
    public static DatabaseHandler dbHandler;
    public static String login_type = "normal";
    public static String device_id = "";
    public static double resolution_ratio = 0.5;//default 0.5
    public static int heightPixels_org;
    public static int widthPixels_org;
    public static String myAppVerison = "";
    private static String TAG = "BrahmaMainActivity";
    private static String iperfClientCmd = IPERF_PATH + " -c 172.19.0.179 -p 5001 -t 5 -P 1";
    private static String curIperfCmd = iperfServiceCmd;
    private volatile static boolean mHasCheckAllScreen;
    private volatile static boolean mIsAllScreenDevice;
    protected boolean busy = false; // is set to 'true' immediately after starting a connection, set to 'false' when resuming
    protected IntentFilter intentFilter;
    protected BrahmaMainActivity.MyBroadcastReceiver myBroadcastReceiver;
    protected IntentFilter intentFilter2;
    protected BrahmaMainActivity.MyDialogReceiver myDialogReceiver;
    int times = 0;
    String myState;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferences2;
    AuthData pd = new AuthData();
    LoginStream lg = new LoginStream();
    LoginStream.Clerk clerk = new LoginStream.Clerk();
    DeviceInfo deviceInfo = new DeviceInfo();
    Toast logToast;
    ConnectivityManager mConnectivityManager;
    NetworkInfo mNetworkInfo;
    ProgressDialog mDialog;
    File file;
    ItemBio itemBio;
    InputMethodManager imm;
    TrustManager[] trustManagers = null;
    String language = "en";
    private int sendRequestCode = REQUEST_STARTVIDEO; // used in the "afterStartAppRTC" method to determine what activity gets started
    private StateMachine machine;
    private AutoCompleteTextView
            usernameView,
            hostView,
            portView,
            pEditDView;
    private View resetView;
    private View messageView;
    private View reconnectView;
    private AlertDialog reset_dialog;
    private AlertDialog message_dialog;
    private AlertDialog reconnect_dialog;
    private TextView appVersion, tv_touchid;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;
    private KeyboardHeightProvider keyboardHeightProvider;

    /*** 1. copy iperf to directory.*/
    private WifiManager mWm;
    private String iperfreply;
    private boolean IPERF_OK = false;
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
    private BiometricPromptManager mManager;
    private TextView reconnect_tv;

    public static boolean isOpenLocationPermission() {
        int permission1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_brahma_login);
        Log.d("LifeCycle", "BrahmaMainActivity onCreate!!!");

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(this, new Crashlytics());

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

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

        widthPixels = (int) (dm.widthPixels * resolution_ratio);
        heightPixels = (int) (dm.heightPixels * resolution_ratio);
        widthPixels_org = dm.widthPixels;
        heightPixels_org = dm.heightPixels;
        resolution = widthPixels + "x" + heightPixels;
        DPI = (int) (getResources().getDisplayMetrics().densityDpi * resolution_ratio);

        Log.d(TAG, "螢幕解析度:" + resolution);
        Log.d(TAG, "resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "螢幕解析度(修正前):" + widthPixels_org + " x " + heightPixels_org);
        Log.d(TAG, "螢幕解析度(修正後):" + widthPixels + " x " + heightPixels);
        Log.d(TAG, "DPI: " + DPI);

//        getNotchParams();

        device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "getAndroidId: " + device_id);

//        //全屏显示
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        getWindow().setAttributes(lp);

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

        intentFilter = new IntentFilter();
        intentFilter.addAction("STATE_RECONNECT");
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);

        intentFilter2 = new IntentFilter();
        intentFilter2.addAction("STOP_PROGRESS");
        myDialogReceiver = new MyDialogReceiver();
        registerReceiver(myDialogReceiver, intentFilter2);

        View view = View.inflate(getApplicationContext(), R.layout.dialog_layout, null);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.setView(view);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        machine = new StateMachine();

        dbHandler = new DatabaseHandler(this);
        deviceInfo.deviceInit(this);

        usernameView = (AutoCompleteTextView) findViewById(R.id.editName);
        hostView = (AutoCompleteTextView) findViewById(R.id.editHost);
        portView = (AutoCompleteTextView) findViewById(R.id.editPort);
        pEditDView = (AutoCompleteTextView) findViewById(R.id.editPp);

        usernameView.setText(sharedPreferences.getString("usernameView", ""));
        hostView.setText(sharedPreferences.getString("hostView", ""));
        portView.setText(sharedPreferences.getString("portView", ""));
        setHistory();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

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

        // title has to be set here instead of in Manifest, for compatibility with shortcuts
        setTitle(R.string.app_name);

        context = this;
        resetView = LayoutInflater.from(BrahmaMainActivity.this).inflate(R.layout.dialog_reset_password, null);
        messageView = LayoutInflater.from(BrahmaMainActivity.this).inflate(R.layout.dialog_message, null);
        reconnectView = LayoutInflater.from(BrahmaMainActivity.this).inflate(R.layout.dialog_reconnect, null);

        //touch id text color change
        tv_touchid = (TextView) findViewById(R.id.tv_touchid);
        SpannableStringBuilder style = new SpannableStringBuilder(tv_touchid.getText().toString());
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.touch_id)), 3, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_touchid.setText(style);

        //限制拍照
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);//Brahma

        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(BrahmaMainActivity.this);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText resetHost = (EditText) resetView.findViewById(R.id.editHost);
                EditText resetPort = (EditText) resetView.findViewById(R.id.editPort);
                EditText resetUsername = (EditText) resetView.findViewById(R.id.editName);
                sendRestPassword(resetHost.getText().toString(), resetPort.getText().toString(), resetUsername.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        builder.setTitle(R.string.reset_password);
        builder.setView(resetView);
        reset_dialog = builder.create();

        //create dialog
        AlertDialog.Builder builder2 = new AlertDialog.Builder(BrahmaMainActivity.this);
        builder2.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder2.setTitle("Reset password features would be open in the futures.");
        builder2.setView(messageView);
        message_dialog = builder2.create();

        //create dialog
        reconnect_tv = (TextView) reconnectView.findViewById(R.id.message_tv);

        AlertDialog.Builder builder3 = new AlertDialog.Builder(BrahmaMainActivity.this);
//        builder3.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                return;
//            }
//        });
//        builder3.setTitle("Reconnect");
        builder3.setView(reconnectView);
        reconnect_dialog = builder3.create();

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Apportal_output.txt");
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.i(TAG, e.getMessage());
        }

        // iperf
        File file = new File(IPERF_PATH);
        Log.i(TAG, "file.exists(): " + file.exists());
        if (!file.exists()) {
            copyiperf();
        }

        if (isOpenLocationPermission()) {
            isSelectFile = false;
        } else {
            isSelectFile = true;
            //dialog
            new AlertDialog.Builder(BrahmaMainActivity.this)
                    .setTitle(R.string.suggestion)
                    .setMessage(R.string.Location_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

            ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    15153);
        }

        if (isOpenCameraPermission()) {
            isSelectFile = false;
        } else {
            isSelectFile = true;
            ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    15159);
        }

        for (int i = 1; i < 3; i++) {
            if (dbHandler.getConnectionInfo(i) == null) {
                ConnectionInfo connectionInfo = new ConnectionInfo(0, Constants.CONN_DESC[i], Constants.USER[i], Constants.CONN_HOST, Constants.DEFAULT_PORT, 1, 1, "", 0, "");
                dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
            }
        }

//        //Notification Login
//        if (sharedPreferences2.getString("notification", "").equals("yes")) {
//            packageName = sharedPreferences2.getString("packageName", "");
//            normalUsername = sharedPreferences2.getString("normalUsername", "");
//            normalPassword = sharedPreferences2.getString("normalPassword", "");
//            normalIP = sharedPreferences2.getString("normalIP", "");
//            normalPort = sharedPreferences2.getString("normalPort", "");
//
//            loginVMI(normalUsername,
//                    normalPassword,
//                    normalIP,
//                    normalPort,
//                    packageName);
//        }

//        //URL Redirect Login
//        if (sharedPreferences2.getString("urlLogin", "").equals("yes")) {
//            packageName = sharedPreferences2.getString("packageName", "");
//            normalUsername = sharedPreferences2.getString("normalUsername", "");
//            normalPassword = sharedPreferences2.getString("normalPassword", "");
//            normalIP = sharedPreferences2.getString("normalIP", "119.31.180.2");
//            normalPort = sharedPreferences2.getString("normalPort", "3000");
//
//            loginVMI(normalUsername,
//                    normalPassword,
//                    normalIP,
//                    normalPort,
//                    packageName);
//        }
        itemBio = new ItemBio(BrahmaMainActivity.this);
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

                Log.d("ddd", "my data >>>>> " + packageName);
                Log.d("ddd", "my data >>>>> " + normalUsername);
                Log.d("ddd", "my data >>>>> " + normalPassword);
                Log.d("ddd", "my data >>>>> " + normalIP);
                Log.d("ddd", "my data >>>>> " + normalPort);

                loginVMI();
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
                loginVMI();
            }
        } else {
            packageName = null;
            normalUsername = null;
            normalPassword = null;
            normalIP = null;
            normalPort = null;
        }


//        callBiometric();

//        Intent iResult = new Intent();
//        iResult.setAction(AbstractIME.TEXT_GOT);
//        iResult.putExtra("TEXT_RESULT", "");
//        sendBroadcast(iResult); // Make an Intent to broadcast back to IME

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        isUsingOurInputMethod();
//        for (int i = 65; i <= 121; i++) {
//            int AcsiiCode = i;
//            char Asc2Char = (char) AcsiiCode;
//            Log.d("KeyEventss", "english " + i + ">>>>> " + Asc2Char);
//        }
//
//        for (int i = 122; i <= 1000; i++) {
//            int AcsiiCode = i;
//            char Asc2Char = (char) AcsiiCode;
//            Log.d("KeyEventss", "english " + i + ">>>>> " + Asc2Char);
//        }
//        for (int i = 19968; i < 20000; i++) {
//            int AcsiiCode = i;
//            char Asc2Char = (char) AcsiiCode;
//            Log.d("KeyEventss", "中文字 " + i + ">>>>> " + Asc2Char);
//        }
//        for (int i = 12549; i < 12582; i++) {
//            int AcsiiCode = i;
//            char Asc2Char = (char) AcsiiCode;
//            Log.d("KeyEventss", "注音 " + i + ">>>>> " + Asc2Char);
//        }
//        for (int i = 12353; i < 12800; i++) {
//            int AcsiiCode = i;
//            char Asc2Char = (char) AcsiiCode;
//            Log.d("KeyEventss", i + " >>>>> " + Asc2Char);
//        }

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

    }

    private boolean callBiometric() {
        if (itemBio.get(1).getUsingBio().equals("true")) {
            mManager = BiometricPromptManager.from(BrahmaMainActivity.this);
            if (mManager.isBiometricPromptEnable()) {
                mManager.authenticate(new BiometricPromptManager.OnBiometricIdentifyCallback() {
                    @Override
                    public void onUsePassword() {
                        Log.d("isUsingBio", "onUsePassword");
                    }

                    @Override
                    public void onSucceeded() {
                        //帶入上次記憶的帳號密碼後直接登入
                        normalUsername = itemBio.get(1).getbioUsername();
                        normalPassword = itemBio.get(1).getbioPassword();
                        normalIP = itemBio.get(1).getbioIP();
                        normalPort = itemBio.get(1).getbioPort();
                        loginVMI();
                    }

                    @Override
                    public void onFailed() {
                        Log.d("isUsingBio", "onFailed");
                    }

                    @Override
                    public void onError(int code, String reason) {
                        Log.d("isUsingBio", "onError");
                    }

                    @Override
                    public void onCancel() {
                        Log.d("isUsingBio", "onCancel");
                    }
                });
            }
            return true;
        } else {
            Log.d("isUsingBio", "no bio data");
            Toast.makeText(BrahmaMainActivity.this, R.string.no_bio_record, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @TargetApi(28)
    public void getNotchParams() {
        final View decorView = getWindow().getDecorView();

        decorView.post(new Runnable() {
            @Override
            public void run() {
                DisplayCutout displayCutout = decorView.getRootWindowInsets().getDisplayCutout();
                Log.e("TAG", "SafeInsetLeft:" + displayCutout.getSafeInsetLeft());
                Log.e("TAG", "SafeInsetRight:" + displayCutout.getSafeInsetRight());
                Log.e("TAG", "SafeInsetTop:" + displayCutout.getSafeInsetTop());
                Log.e("TAG", "SafeInsetBottom:" + displayCutout.getSafeInsetBottom());

                List<Rect> rects = displayCutout.getBoundingRects();
                if (rects == null || rects.size() == 0) {
                    Log.e("TAG", "不是瀏海屏");
                } else {
                    Log.e("TAG", "瀏海屏数量:" + rects.size());
                    for (Rect rect : rects) {
                        Log.e("TAG", "瀏海屏區域：" + rect);
                    }
                }
            }
        });
    }

    private void isUsingOurInputMethod() {
        boolean needChange = true;
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            Log.d("settingInput", "imi.getPackageName() >>>>> " + imi.getPackageName());
//            Log.d("settingInput", "getPackageName() >>>>> " + getPackageName());
            if (imi.getPackageName().equals("com.google.android.inputmethod.latin")) {
//            if (getPackageName().equals(imi.getPackageName())) {
                needChange = false;
                return;
            }
        }
        if (needChange)
            settingInput();
    }

    private void settingInput() {
        //dialog
        new AlertDialog.Builder(BrahmaMainActivity.this)
                .setTitle(R.string.suggestion)
                .setMessage(R.string.IME_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                        startActivityForResult(myIntent, 1099);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private boolean isOpenCameraPermission() {
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /*** 欄位輸入之記憶功能 */
    public void setHistory() {
        String[] hostArray = sharedPreferences.getString("hostHistory", "").split(",");
        String[] portArray = sharedPreferences.getString("portHistory", "").split(",");
        String[] userArray = sharedPreferences.getString("usernameHistory", "").split(",");

        Set<String> strSet1 = new HashSet<String>();
        for (String element : hostArray) {
            strSet1.add(element);
        }

        String nonDuplicateArray[] = new String[strSet1.size()];
        Object[] tempArray = strSet1.toArray();
        for (int i = 0; i < tempArray.length; i++) {
            nonDuplicateArray[i] = (String) tempArray[i];
        }
        adapter = new ArrayAdapter<>(this, R.layout.search_item, nonDuplicateArray);
        hostView.setThreshold(1);
        hostView.setAdapter(adapter);

        Set<String> strSet2 = new HashSet<String>();
        for (String element : portArray) {
            strSet2.add(element);
        }

        String nonDuplicateArray2[] = new String[strSet2.size()];
        Object[] tempArray2 = strSet2.toArray();
        for (int i = 0; i < tempArray2.length; i++) {
            nonDuplicateArray2[i] = (String) tempArray2[i];
        }
        adapter = new ArrayAdapter<>(this, R.layout.search_item, nonDuplicateArray2);
        portView.setThreshold(1);
        portView.setAdapter(adapter);

        Set<String> strSet3 = new HashSet<String>();
        for (String element : userArray) {
            strSet3.add(element);
        }

        String nonDuplicateArray3[] = new String[strSet3.size()];
        Object[] tempArray3 = strSet3.toArray();
        for (int i = 0; i < tempArray3.length; i++) {
            nonDuplicateArray3[i] = (String) tempArray3[i];
        }

        adapter = new ArrayAdapter<>(this, R.layout.search_item, nonDuplicateArray3);
        usernameView.setThreshold(1);
        usernameView.setAdapter(adapter);
    }

    /*** 點擊brahma icon進入QR code Scanner模式 */
    public void onClick_setting(View v) {
        Intent intent = new Intent();
        intent.setClass(BrahmaMainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    /*** 點擊brahma icon進入QR code Scanner模式 */
    public void onClick_back(View v) {
        final Activity activity = this;
        isSelectFile = true;

        if (isOpenCameraPermission()) {
            new AlertDialog.Builder(BrahmaMainActivity.this)
                    .setTitle(R.string.SCAN_OR_UPLOAD)
                    .setMessage(R.string.SELECT_QRCODE_METHOD)
                    .setPositiveButton(R.string.SCAN_QRCODE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                            intentIntegrator.setPrompt("Scan");
                            intentIntegrator.setCameraId(0);
                            intentIntegrator.setBeepEnabled(false);
                            intentIntegrator.setBarcodeImageEnabled(false);
                            intentIntegrator.initiateScan();
                        }
                    })
                    .setNegativeButton(R.string.UPLOAD_QRCODE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            isSelectFile = true;
                            startActivityForResult(intent, REQUEST_UPLOADQRCODE);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(BrahmaMainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    15158);
        }
    }

    /*** 點擊button進行reset password */
    public void onClick_reset(View v) {
        EditText resetHost = (EditText) resetView.findViewById(R.id.editHost);
        EditText resetPort = (EditText) resetView.findViewById(R.id.editPort);
        EditText resetUsername = (EditText) resetView.findViewById(R.id.editName);
        resetHost.setText(hostView.getText().toString());
        resetPort.setText(portView.getText().toString());
        resetUsername.setText(usernameView.getText().toString());
        reset_dialog.show();
    }

    public void sendRestPassword(String host, String port, String username) {

        new resetPasswordTask().execute(host, port, username);

//        Thread t = new Thread(new sendRestPasswordRunnable(host, port, username));
//        t.start();
    }

    /*** 點擊button進行login動作 */
    public void onClick_login(View v) {
        mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        //如果未連線的話，mNetworkInfo會等於null
//        if (mNetworkInfo != null) {
//*****20191008 modify for Debug*****
//        pEditDView.setText("Aa123456");

        normalIP = hostView.getText().toString();
        normalPort = portView.getText().toString();
        normalUsername = usernameView.getText().toString();
        normalPassword = pEditDView.getText().toString();

        //確認欄位都有輸入
        if (hostView.getText().toString().matches("") ||
                portView.getText().toString().matches("") ||
                usernameView.getText().toString().matches("") ||
                pEditDView.getText().toString().matches("")) {
            Toast.makeText(BrahmaMainActivity.this, R.string.check_input, Toast.LENGTH_LONG).show();
//            *****20191008 modify for Debug*****
//        } else if (!isSecurityInput(usernameView.getText().toString())) {//check email format
//            Toast.makeText(BrahmaMainActivity.this, R.string.is_not_secure, Toast.LENGTH_SHORT).show();
        } else if (!isSecurityPort(Integer.parseInt(normalPort))) {
            Toast.makeText(BrahmaMainActivity.this, R.string.check_input_port, Toast.LENGTH_LONG).show();
        } else if (!isSecurityHost(normalIP)) {
            Toast.makeText(BrahmaMainActivity.this, R.string.check_input_host, Toast.LENGTH_LONG).show();
        } else {
            loginVMI();
        }

//        } else {
//            Log.d(TAG, "client network 沒有連線");
//            Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_SHORT).show();
//        }

        //記憶輸入過的資料
        String hostHistory = sharedPreferences.getString("hostHistory", null);
        if (hostHistory != null)
            hostHistory += "," + hostView.getText().toString();
        else
            hostHistory = hostView.getText().toString();
        String portHistory = sharedPreferences.getString("portHistory", null);

        if (portHistory != null)
            portHistory += "," + portView.getText().toString();
        else
            portHistory = portView.getText().toString();
        String usernameHistory = sharedPreferences.getString("usernameHistory", null);

        if (usernameHistory != null)
            usernameHistory += "," + usernameView.getText().toString();
        else
            usernameHistory = usernameView.getText().toString();

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        sharedPreferences.edit().putString("usernameView", usernameView.getText().toString()).apply();
        sharedPreferences.edit().putString("password", pEditDView.getText().toString()).apply();
        sharedPreferences.edit().putString("hostView", hostView.getText().toString()).apply();
        sharedPreferences.edit().putString("portView", portView.getText().toString()).apply();
        sharedPreferences.edit().putString("hostHistory", hostHistory).apply();
        sharedPreferences.edit().putString("portHistory", portHistory).apply();
        sharedPreferences.edit().putString("usernameHistory", usernameHistory).apply();
    }

    private boolean isSecurityPort(int normalPort) {
        if (0 < normalPort && normalPort < 65536) {
            Log.d(TAG, "isSecurityPort true");
            return true;
        } else {
            Log.d(TAG, "isSecurityPort false");
            return false;
        }
    }

    /*** 點擊button進行指紋辨識動作 */
    public void onClick_touchID(View v) {
        callBiometric();
    }

    @Override
    public void onDestroy() {
        Log.d("LifeCycle", "BrahmaMainActivity onDestroy!!!");
        hideSoftKeyboard();
        dbHandler.close();
//        itemBio.close();
        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(myDialogReceiver);
        super.onDestroy();
        keyboardHeightProvider.close();
    }

    @Override
    public void onBackPressed() {
        Log.d("LifeCycle", "BrahmaMainActivity onBackPressed!!!");
        super.onBackPressed();
    }

    /*** 一般登入方式 */
    public void loginVMI() {
        Log.d(TAG, "Login vmi");
        //check overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "detect overlay result:" + Settings.canDrawOverlays(context));
            if (!Settings.canDrawOverlays(context)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                myIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(myIntent, 101);
            } else {
                pd.setPd(normalPassword);
                pd.setAD(false);
                AppRTCClient.type = "vmi";
                userEmail = normalUsername;
                long result;
                ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);

                if (connectionInfo != null) {
                    connectionInfo = new ConnectionInfo(2, "userMode", usernameView.getText().toString(), hostView.getText().toString(), Integer.parseInt(portView.getText().toString()), 1, 1, "", 0, "vmi");
                    result = dbHandler.updateConnectionInfo(connectionInfo);
                    Log.d(TAG, "Update ConnectionInfo");
                } else {
                    connectionInfo = new ConnectionInfo(2, "userMode", usernameView.getText().toString(), hostView.getText().toString(), Integer.parseInt(portView.getText().toString()), 1, 1, "", 0, "vmi");
                    result = dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
                    Log.d(TAG, "Add new ConnectionInfo");
                }

                // exit and resume previous activity, report results in the intent
                if (result > -1) {
                    // we have updated this ConnectionInfo; if session info is stored for this ConnectionInfo, remove it
                    dbHandler.clearSessionInfo(connectionInfo);
                }
                deviceInfo.setUser(usernameView.getText().toString());
                if (connectionInfo != null) {
                    try {
                        // if we allow use of desktop mode, start the connection; otherwise take us to this connection's app list
                        this.sendRequestCode = REQUEST_STARTVIDEO;
                        machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                        startProgressDialog("Connecting...");
                        getUserInfo(connectionInfo);
                    } catch (Exception e) {
                        Log.e(TAG, "Error:" + e.getMessage());
                    }
                }
            }
        } else {
            pd.setPd(normalPassword);
            pd.setAD(false);
            AppRTCClient.type = "vmi";
            userEmail = normalUsername;

            long result;
            ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);
            if (connectionInfo != null) {
                connectionInfo = new ConnectionInfo(2, "userMode", usernameView.getText().toString(), hostView.getText().toString(), Integer.parseInt(portView.getText().toString()), 1, 1, "", 0, "vmi");
                result = dbHandler.updateConnectionInfo(connectionInfo);
                Log.d(TAG, "Update ConnectionInfo");
            } else {
                connectionInfo = new ConnectionInfo(2, "userMode", usernameView.getText().toString(), hostView.getText().toString(), Integer.parseInt(portView.getText().toString()), 1, 1, "", 0, "vmi");
                result = dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
                Log.d(TAG, "Add new ConnectionInfo");
            }

            // exit and resume previous activity, report results in the intent
            if (result > -1) {
                // we have updated this ConnectionInfo; if session info is stored for this ConnectionInfo, remove it
                dbHandler.clearSessionInfo(connectionInfo);
            }

            deviceInfo.setUser(usernameView.getText().toString());

            if (connectionInfo != null) {
                try {
                    // if we allow use of desktop mode, start the connection; otherwise take us to this connection's app list
                    this.sendRequestCode = REQUEST_STARTVIDEO;
                    machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                    getUserInfo(connectionInfo);
                } catch (Exception e) {
                    Log.e(TAG, "Error:" + e.getMessage());
                }
            }
        }
    }

    /*** 使用QRcode Scanner的方式登入VMI */
    public void loginVMI(String username, String password, String host, String port, String pgeName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "detect overlay result:" + Settings.canDrawOverlays(context));
            if (!Settings.canDrawOverlays(context)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                myIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(myIntent, 101);
            } else {
                pd.setPd(password);
                pd.setAD(false);
                AppRTCClient.type = "vmi";
                userEmail = username;

                long result;
                ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);
                if (connectionInfo != null) {
                    connectionInfo = new ConnectionInfo(2, "userMode", username, host, Integer.parseInt(port), 1, 1, "", 0, "vmi");
                    result = dbHandler.updateConnectionInfo(connectionInfo);
                    Log.d(TAG, "Update ConnectionInfo");
                } else {
                    connectionInfo = new ConnectionInfo(2, "userMode", username, host, Integer.parseInt(port), 1, 1, "", 0, "vmi");
                    result = dbHandler.insertConnectionInfo(connectionInfo);
                    Log.d(TAG, "Add new ConnectionInfo");
                }

                // exit and resume previous activity, report results in the intent
                if (result > -1) {
                    // we have updated this ConnectionInfo; if session info is stored for this ConnectionInfo, remove it
                    dbHandler.clearSessionInfo(connectionInfo);
                }
                deviceInfo.setUser(username);
                if (connectionInfo != null) {
                    try {
                        // if we allow use of desktop mode, start the connection; otherwise take us to this connection's app list
                        this.sendRequestCode = REQUEST_STARTVIDEO;
                        machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                        authPrompt(connectionInfo);
                    } catch (Exception e) {
                        Log.e(TAG, "Error:" + e.getMessage());
                    }
                }
            }
        } else {
            pd.setPd(password);
            pd.setAD(false);
            AppRTCClient.type = "vmi";
            userEmail = username;

            long result;
            ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);
            if (connectionInfo != null) {
                connectionInfo = new ConnectionInfo(2, "userMode", username, host, Integer.parseInt(port), 1, 1, "", 0, "vmi");
                result = dbHandler.updateConnectionInfo(connectionInfo);
                Log.d(TAG, "Update ConnectionInfo");
            } else {
                connectionInfo = new ConnectionInfo(2, "userMode", username, host, Integer.parseInt(port), 1, 1, "", 0, "vmi");
                result = dbHandler.insertConnectionInfo(connectionInfo);//將連線資訊新增至資料庫
                Log.d(TAG, "Add new ConnectionInfo");
            }
            if (result > -1) {
                dbHandler.clearSessionInfo(connectionInfo);
            }

            deviceInfo.setUser(username);

            if (connectionInfo != null) {
                try {
                    this.sendRequestCode = REQUEST_STARTVIDEO;
                    machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                    authPrompt(connectionInfo);
                } catch (Exception e) {
                    Log.e(TAG, "Error:" + e.getMessage());
                }
            }
        }
    }

    private void getUserInfo(ConnectionInfo connectionInfo) {
//        Thread t = new Thread(new sendPostRunnable(connectionInfo));
//        t.start();
        new LoginTask().execute(connectionInfo);

        iperfClientCmd = IPERF_PATH + " -c 119.31.180.2 -p 9002";
        sercomfun(iperfClientCmd);
    }

//    private String sendPostDataToInternet(ConnectionInfo connectionInfo) {
//        String uriAPI = "https://" + normalIP + ":" + normalPort + "/login";
//        Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
//        EasySSLSocketFactory socketFactory;
//        socketFactory = new EasySSLSocketFactory();
//        JSONObject http_data = new JSONObject();
//        String network = "";
//        String device = "";
//        String os = "";
//
//        try {
//            ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo tInfo = gConnMgr.getActiveNetworkInfo();
//            if (tInfo != null) {
//                network = toLowerCase(tInfo.getTypeName());
//            } else {
//                network = "wifi";
//            }
//            device = Build.BRAND + " " + Build.MODEL;
//            os = "Android " + Build.VERSION.RELEASE;
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//            Log.e(TAG, "ERROR:" + e.getStackTrace());
//        }
//
//        try {
//            http_data.put("user_name", normalUsername);
//            http_data.put("password", normalPassword);
//            http_data.put("device", device);
//            http_data.put("os", os);
//            http_data.put("longitude", String.valueOf(Longitude));
//            http_data.put("latitude", String.valueOf(Latitude));
//            http_data.put("resolution", resolution);
//            http_data.put("network", network);
//            http_data.put("media", "standard");
//            http_data.put("device_token", device_id);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "http_data:" + http_data.toString());
//
//
//        int returnVal;
//        try {
//            HttpParams params = new BasicHttpParams();
//            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//
//            SchemeRegistry registry = new SchemeRegistry();
//            registry.register(new Scheme("https", socketFactory, 9568));
//            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
//
//            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
//            HttpPost post = new HttpPost(uriAPI);
//            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
//            post.setHeader("brahma-lang", language);
//            StringEntity entity = null;
//
//            try {
//                entity = new StringEntity(http_data.toString());
//                post.setEntity(entity);
//                response = httpclient.execute(post);
//                responseCode = response.getStatusLine().getStatusCode();
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                response.getEntity().writeTo(out);
//                out.close();
//                Log.d(TAG, "responseCode:" + responseCode);
//                Log.d(TAG, "response message:" + out.toString());
//                jsonResponse = new JSONObject(out.toString());
//                String msg = jsonResponse.getString("msg");
//                String status_code = jsonResponse.getString("status_code");
//                Log.d(TAG, "get json message:" + msg);
//
//                if (responseCode == 200) {
//                    if (status_code.equals("20000")) {
//                        //judge type
//                        String type = jsonResponse.getString("type");
//                        if (type.equals("vmi")) {
//                            _login = true;
//                            authPrompt(connectionInfo);
//                        } else {
//                            appListObject = jsonResponse.getJSONObject("data");
//                            user_id = jsonResponse.getString("user_id");
//                            performanceToken = jsonResponse.getString("token");
//                            loginApp();
//                            Log.d(TAG, "appListObject:" + appListObject);
//                        }
//                    } else {
//                        Looper.prepare();
//                        stopProgressDialog();
//                        logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
//                        logToast.show();
//                        Looper.loop();
//                    }
//                    login_type = "normal";
//                } else {
//                    Looper.prepare();
//                    stopProgressDialog();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                }
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//            } catch (JSONException e) {
//                Log.e(TAG, "Failed to parse JSON response:", e);
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//            } catch (SSLHandshakeException e) {
//                String msg = e.getMessage();
//                if (msg.contains("java.security.cert.CertPathValidatorException")) {
//                    Log.e(TAG, "Untrusted server certificate!");
//                    returnVal = R.string.appRTC_toast_socketConnector_failUntrustedServer;
//                    Looper.prepare();
//                    stopProgressDialog();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                } else {
//                    Log.e(TAG, "Error during SSL handshake: " + e.getMessage());
//                    returnVal = R.string.appRTC_toast_socketConnector_failSSLHandshake;
//                    Looper.prepare();
//                    stopProgressDialog();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                }
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//            } catch (SSLException e) {
//                if ("Connection closed by peer".equals(e.getMessage())) {
//                    // connection failed, we tried to connect using SSL but REST API's SSL is turned off
//                    Log.e(TAG, "Client encryption is on but server encryption is off:", e);
//                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
//                    Looper.prepare();
//                    stopProgressDialog();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                } else {
//                    Log.e(TAG, "SSL error:", e);
//                }
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//            } catch (NoHttpResponseException e) {
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//                if ("The target server failed to respond".equals(e.getMessage())) {
//                    // connection failed, we tried to connect without using SSL but REST API's SSL is turned on
//                    Log.e(TAG, "Client encryption is off but server encryption is on:", e);
//                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
//                    Looper.prepare();
//                    stopProgressDialog();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                } else {
//                    Log.e(TAG, "HTTP request failed, NoHttpResponseException:", e);
//                    returnVal = R.string.appRTC_toast_socketConnector_nohttprespons;
//                    Looper.prepare();
//                    stopProgressDialog();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                }
//            } catch (IllegalArgumentException e) {
//                Log.e(TAG, "HTTP request failed:", e);
//                returnVal = R.string.appRTC_toast_socketConnector_check;
//                Looper.prepare();
//                stopProgressDialog();
//                logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                logToast.show();
//                Looper.loop();
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//            } catch (IOException e) {
//                Log.e(TAG, "HTTP request failed:", e);
//                returnVal = R.string.appRTC_toast_socketConnector_check;
//                Looper.prepare();
//                stopProgressDialog();
//                logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
//                logToast.show();
//                Looper.loop();
//                Message msg2 = new Message();
//                msg2.what = 1;
//                mHandler.sendMessage(msg2);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Message msg2 = new Message();
//            msg2.what = 1;
//            mHandler.sendMessage(msg2);
//            stopProgressDialog();
//        }
//        return null;
//    }

//    /*** 送出reset password API for web server */
//    private String sendResetPasswordToInternet(String host, String port, String username) {
//        String uriAPI = "https://" + host + ":" + port + "/pwd/req";
//        Log.d(TAG, "sendResetPasswordToInternet uri:" + uriAPI);
//        EasySSLSocketFactory socketFactory;
//        socketFactory = new EasySSLSocketFactory();
//        JSONObject http_data = new JSONObject();
//        JSONObject jsonResponse = null;
//        try {
//            http_data.put("user_name", username);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "http_data:" + http_data.toString());
//        try {
//            HttpParams params = new BasicHttpParams();
//            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//
//            SchemeRegistry registry = new SchemeRegistry();
//            registry.register(new Scheme("https", socketFactory, 9568));
//            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
//
//            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
//            HttpPost post = new HttpPost(uriAPI);
//            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
//            post.setHeader("brahma-lang", language);
//            StringEntity entity = null;
//            int responseCode = 0;
//            HttpResponse response = null;
//            try {
//                entity = new StringEntity(http_data.toString());
//                post.setEntity(entity);
//                response = httpclient.execute(post);
//                responseCode = response.getStatusLine().getStatusCode();
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                response.getEntity().writeTo(out);
//                out.close();
//                Log.d(TAG, "responseCode:" + responseCode);
//                Log.d(TAG, "response message:" + out.toString());
//                jsonResponse = new JSONObject(out.toString());
//                String msg = jsonResponse.getString("msg");
//                Log.d(TAG, "get json message:" + msg);
//
//                if (responseCode == 200) {
//                    Looper.prepare();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                } else {
//                    Looper.prepare();
//                    logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
//                    logToast.show();
//                    Looper.loop();
//                }
//            } catch (JSONException e) {
//                Log.e(TAG, "Failed to parse JSON response:", e);
//                Looper.prepare();
//                logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG);
//                logToast.show();
//                Looper.loop();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                Looper.prepare();
//                logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG);
//                logToast.show();
//                Looper.loop();
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//                Looper.prepare();
//                logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG);
//                logToast.show();
//                Looper.loop();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Looper.prepare();
//                logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG);
//                logToast.show();
//                Looper.loop();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Looper.prepare();
//            logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG);
//            logToast.show();
//            Looper.loop();
//        }
//        return null;
//    }

    /*** 一般AppStreaming登入 */
    public void loginApp() {
        pd.setPd(normalPassword);
        pd.setAD(false);//判定連線是否使用AD
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

        if (connectionInfo != null) {
            clerk.setProduct(1);
            //Log.d("onClick_stream_in Login",": "+clerk.getProduct());
            //clerk.getProduct()在還沒set值之前不可再執行，會導至無限等待set值

            switch (clerk.getProduct()) {
                case 1:
                    //通知登入訊息
                    deviceInfo.setUser(usernameView.getText().toString());

                    Intent intent = new Intent(BrahmaMainActivity.this, SlidingTab.class);
                    intent.putExtra("cName", usernameView.getText().toString());
                    intent.putExtra("connectionID", connectionInfo.getConnectionID());
                    // start the activity and expect a result intent when it is finished
                    machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success, getResources().getString(R.string.appRTC_toast_brahmaAuthenticator_success)); // STARTED -> AUTH
                    startActivity(intent);
                    break;
                case 2:
                    stopProgressDialog();
                    Looper.prepare();
                    logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_404, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                    break;
                case 3:
                    stopProgressDialog();
                    Looper.prepare();
                    logToast = Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_brahmaAuthenticator_fail, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                    break;
                case 4:
                    stopProgressDialog();
                    Looper.prepare();
                    logToast = Toast.makeText(BrahmaMainActivity.this, "get session info fail", Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                    break;
                case 5:
                    stopProgressDialog();
                    Looper.prepare();
                    logToast = Toast.makeText(BrahmaMainActivity.this, "Please check your network.", Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                    break;
                default:
                    break;
            }
        }
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
                        Log.d(TAG, "AES128 decode >>>" + decrypt);
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
                try {
                    //讀取照片，型態為Bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
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
                            Log.i(TAG, "AES128 decode >>>" + decrypt);
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
                }
            }
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
                Location locationGPS = lm.getLastKnownLocation(GPS_PROVIDER); //使用GPS定位座標
                Location locationNetwork = lm.getLastKnownLocation(NETWORK_PROVIDER); //使用Network定位座標

                if (locationGPS != null) {
                    //try catch NullPointerException
                    try {
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


        //setting input
        if (grantResults.length > 0 && requestCode == 1099) {
            isUsingOurInputMethod();
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
                long result;
                ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(1);
                if (connectionInfo != null) {
                    connectionInfo = new ConnectionInfo(1, "appMode", qrcodeID, normalIP, Integer.valueOf(normalPort), 1, 1, "", 0, "appstreaming");
                    result = dbHandler.updateConnectionInfo(connectionInfo);
                    Log.d(TAG, "Update ConnectionInfo");
                } else {
                    connectionInfo = new ConnectionInfo(1, "appMode", qrcodeID, normalIP, Integer.valueOf(normalPort), 1, 1, "", 0, "appstreaming");
                    result = dbHandler.insertConnectionInfo(connectionInfo);
                    Log.d(TAG, "Add new ConnectionInfo");
                }
                new QRcodeTask().execute(jsonObj);
//                Thread t = new Thread(new sendQRcodePostRunnable(qrcodePath, qrcodeID));
//                t.start();
            } else {
                Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(BrahmaMainActivity.this, R.string.scan_qrcode_error, Toast.LENGTH_LONG).show();
        }

    }

    private String sendQRcodePostDataToInternet(String qrcodePath, String qrcodeID) {
        String uriAPI = qrcodePath + "/loginByQrcode";
        Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
        EasySSLSocketFactory socketFactory;
        socketFactory = new EasySSLSocketFactory();
        JSONObject http_data = new JSONObject();

        //fake connectioninfo
        ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);

        String network = "";
        String device = "";
        String os = "";
        try {
            ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
            Log.e(TAG, "ERROR:" + e.getStackTrace());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "http_data:" + http_data.toString());
        int returnVal;
        try {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", socketFactory, 9568));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
            HttpPost post = new HttpPost(uriAPI);
            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
            post.setHeader("brahma-lang", language);
            StringEntity entity = null;

            try {
                entity = new StringEntity(http_data.toString());
                post.setEntity(entity);
                response = httpclient.execute(post);
                responseCode = response.getStatusLine().getStatusCode();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                Log.d(TAG, "responseCode:" + responseCode);
                Log.d(TAG, "response message:" + out.toString());
                jsonResponse = new JSONObject(out.toString());
                String msg = jsonResponse.getString("msg");
                String status_code = jsonResponse.getString("status_code");
                Log.d(TAG, "get json message:" + msg);

                if (responseCode == 200) {
                    if (status_code.equals("20000")) {
                        //judge type
                        String type = jsonResponse.getString("type");
                        if (!jsonResponse.isNull("qrcodeInfo")) {
                            qrcodeAPK = jsonResponse.getJSONObject("qrcodeInfo").getString("apk");
                            qrcodeFileName = jsonResponse.getJSONObject("qrcodeInfo").getString("file_name");
                            Log.d(TAG, "qrcodeFileName >>>>> " + qrcodeFileName);
                            Log.d(TAG, "qrcodeFileName.length() >>>>> " + qrcodeFileName.length());
                            if (qrcodeFileName.length() != 0) {
                                packageName = qrcodeAPK + ";" + qrcodeFileName;
                            } else {
                                packageName = qrcodeAPK;
                            }
                        }

                        if (type.equals("vmi")) {
                            _login = true;
                            authPrompt(connectionInfo);
                        } else {
                            appListObject = jsonResponse.getJSONObject("data");
                            user_id = jsonResponse.getString("user_id");
                            performanceToken = jsonResponse.getString("token");
                            loginApp();
                            Log.d(TAG, "appListObject:" + appListObject);
                        }
                    } else {
                        Looper.prepare();
                        stopProgressDialog();
                        logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                        logToast.show();
                        Looper.loop();
                    }
                    login_type = "qrcode";
                } else {
                    Looper.prepare();
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                }
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON response:", e);
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            } catch (SSLHandshakeException e) {
                String msg = e.getMessage();
                if (msg.contains("java.security.cert.CertPathValidatorException")) {
                    Log.e(TAG, "Untrusted server certificate!");
                    returnVal = R.string.appRTC_toast_socketConnector_failUntrustedServer;
                    Looper.prepare();
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                } else {
                    Log.e(TAG, "Error during SSL handshake: " + e.getMessage());
                    returnVal = R.string.appRTC_toast_socketConnector_failSSLHandshake;
                    Looper.prepare();
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                }
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            } catch (SSLException e) {
                if ("Connection closed by peer".equals(e.getMessage())) {
                    // connection failed, we tried to connect using SSL but REST API's SSL is turned off
                    Log.e(TAG, "Client encryption is on but server encryption is off:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                    Looper.prepare();
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                } else {
                    Log.e(TAG, "SSL error:", e);
                }
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            } catch (NoHttpResponseException e) {
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
                if ("The target server failed to respond".equals(e.getMessage())) {
                    // connection failed, we tried to connect without using SSL but REST API's SSL is turned on
                    Log.e(TAG, "Client encryption is off but server encryption is on:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                    Looper.prepare();
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                } else {
                    Log.e(TAG, "HTTP request failed, NoHttpResponseException:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_nohttprespons;
                    Looper.prepare();
                    stopProgressDialog();
                    logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                }

            } catch (IllegalArgumentException e) {
                Log.e(TAG, "HTTP request failed:", e);
                returnVal = R.string.appRTC_toast_socketConnector_check;
                Looper.prepare();
                stopProgressDialog();
                logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                logToast.show();
                Looper.loop();
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            } catch (IOException e) {
                Log.e(TAG, "HTTP request failed:", e);
                returnVal = R.string.appRTC_toast_socketConnector_check;
                Looper.prepare();
                stopProgressDialog();
                logToast = Toast.makeText(BrahmaMainActivity.this, returnVal, Toast.LENGTH_LONG);
                logToast.show();
                Looper.loop();
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message msg2 = new Message();
            msg2.what = 1;
            mHandler.sendMessage(msg2);
            stopProgressDialog();
        }
        return null;
    }

    private boolean isSecurityHost(String str) {
        Log.d(TAG, "DOMAIN_NAME >>>>>> " + android.util.Patterns.DOMAIN_NAME.matcher(str).matches());
        Log.d(TAG, "IP_ADDRESS >>>>>> " + android.util.Patterns.IP_ADDRESS.matcher(str).matches());
        return true;
    }

    private boolean isSecurityInput(String str) {
        Log.d(TAG, "EMAIL_ADDRESS >>>>>> " + android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches());
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches();
    }

    public void onResume() {
        Log.d("LifeCycle", "BrahmaMainActivity onResume!!!");
        pEditDView.setText("");
        setHistory();

        // 動態調整解析度
        if (sharedPreferences.getString("resolution_ratio", "0.5") != "")
            resolution_ratio = Double.parseDouble(sharedPreferences.getString("resolution_ratio", "0.5"));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        widthPixels = (int) (dm.widthPixels * resolution_ratio);
        heightPixels = (int) (dm.heightPixels * resolution_ratio);
        DPI = (int) (getResources().getDisplayMetrics().densityDpi * resolution_ratio);

        Log.d(TAG, "onResume resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "螢幕解析度(修正後):" + widthPixels + " x " + heightPixels);
        Log.d(TAG, "DPI(修正後): " + DPI);

        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    public void onPause() {
        Log.d("LifeCycle", "BrahmaMainActivity onPause!!!");
//        sharedPreferences2.edit().clear().commit();
        mDialog.dismiss();
        pEditDView.setText("");
        //hide keyboard
        hideSoftKeyboard();
        // 動態調整解析度
        if (sharedPreferences.getString("resolution_ratio", "0.5") != "")
            resolution_ratio = Double.parseDouble(sharedPreferences.getString("resolution_ratio", "0.5"));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        widthPixels = (int) (dm.widthPixels * resolution_ratio);
        heightPixels = (int) (dm.heightPixels * resolution_ratio);
        DPI = (int) (getResources().getDisplayMetrics().densityDpi * resolution_ratio);

        Log.d(TAG, "onResume resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "resolution_ratio:" + resolution_ratio);
        Log.d(TAG, "螢幕解析度(修正後):" + widthPixels + " x " + heightPixels);
        Log.d(TAG, "DPI(修正後): " + DPI);

        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    private void hideSoftKeyboard() {
        Log.d(TAG, "hideSoftKeyboard ");
        Log.d(TAG, "getCurrentFocus() >>>>> " + getCurrentFocus());
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //點擊空白區域隱藏鍵盤
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
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
        progressDialog = new ProgressDialog(BrahmaMainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //disconnectAndExit();
            }
        });
        progressDialog.show();
    }

    private void stopProgressDialog() {
        Log.d("LifeCycle", "BrahmaActivity stopProgressDialog");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * 1. copy iperf to the directory
     */
    public void copyiperf() {
        File localfile;
        Process p;
        try {
            localfile = new File(IPERF_PATH);
            p = Runtime.getRuntime().exec("chmod 777 " + localfile.getAbsolutePath());
            InputStream localInputStream = getAssets().open("iperf");
            Log.i(TAG, "chmod 777 " + localfile.getAbsolutePath());
            FileOutputStream localFileOutputStream = new FileOutputStream(localfile.getAbsolutePath());
            //FileOutputStream localFileOutputStream = new FileOutputStream("/sdcard/iperf");
            FileChannel fc = localFileOutputStream.getChannel();
            FileLock lock = fc.tryLock(); //给文件设置独占锁定
            if (lock == null) {
                Toast.makeText(this, "has been locked !", Toast.LENGTH_LONG).show();
                return;
            } else {
                FileOutputStream fos = new FileOutputStream(new File(IPERF_PATH));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = localInputStream.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                    Log.i(TAG, "byteCount: " + byteCount);
                }
                fos.flush();// 刷新缓冲区
                localInputStream.close();
                fos.close();
                //Test
                //fc.close();

            }
            //两次才能确保开启权限成功
            p = Runtime.getRuntime().exec("chmod 777 " + localfile.getAbsolutePath());
            lock.release();
            fc.close();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 2. 在Android中執行iperf命令
     */
    private void sercomfun(final String cmd) {
        Log.i(TAG, "sercomfun = " + cmd);
        Thread lthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String errorreply = "";
                    CommandHelper.DEFAULT_TIMEOUT = 150000;
                    CommandResult result = CommandHelper.exec(cmd);
                    if (result != null) {
                        //start to connect the service
                        if (result.getError() != null) {
                            errorreply = result.getError();
                            Message m = new Message();
                            m.obj = errorreply;
                            m.what = IPERF_ERROR;
                            handler.sendMessage(m);
                            Log.i(TAG, "Error:" + errorreply);
                        }
                        if (result.getOutput() != null) {
                            iperfreply = getThroughput(result.getOutput());
                            IPERF_OK = true;
                            Message m = new Message();
                            m.obj = iperfreply;
                            m.what = IPERF_SCCESS;
                            handler.sendMessage(m);
                            Log.i(TAG, "Output:" + iperfreply);
                        }
                        Log.i(TAG, "result.getExitValue(): " + result.getExitValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        lthread.start();
    }

    /**
     * 从获取到的吞吐量信息中截取需要的信息，如：
     * 0.0-10.0 sec  27.5 MBytes  23.0 Mbits/sec
     */
    private String getThroughput(String str) {
        String regx = "0.0-.+?/sec";
        String result = "";
        Matcher matcher = Pattern.compile(regx).matcher(str);
        Log.i(TAG, "matcher regx : " + regx + " is " + matcher.matches());
        if (matcher.find()) {
            Log.i(TAG, "group: " + matcher.group());
            result = matcher.group();
        }
        return result;
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

    class resetPasswordTask extends AsyncTask<String, String, JSONObject> {
        String uriAPI = "";
        EasySSLSocketFactory socketFactory;
        JSONObject http_data = null;
        JSONObject jsonResponse = null;
        int returnVal;

        @Override
        protected JSONObject doInBackground(String... strings) {
            uriAPI = "https://" + strings[0] + ":" + strings[1] + "/pwd/req";
            Log.d(TAG, "sendResetPasswordToInternet uri:" + uriAPI);
            socketFactory = new EasySSLSocketFactory();
            http_data = new JSONObject();
            try {
                http_data.put("user_name", strings[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "http_data:" + http_data.toString());
            try {
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", socketFactory, 9568));
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
                HttpPost post = new HttpPost(uriAPI);
                post.setHeader(HTTP.CONTENT_TYPE, "application/json");
                post.setHeader("brahma-lang", language);
                StringEntity entity = null;
                int responseCode = 0;
                HttpResponse response = null;
                try {
                    entity = new StringEntity(http_data.toString());
                    post.setEntity(entity);
                    response = httpclient.execute(post);
                    responseCode = response.getStatusLine().getStatusCode();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    Log.d(TAG, "responseCode:" + responseCode);
                    Log.d(TAG, "response message:" + out.toString());
                    jsonResponse = new JSONObject(out.toString());

                    String msg = jsonResponse.getString("msg");
                    Log.d(TAG, "get json message:" + msg);
                    if (responseCode == 200) {
                        Looper.prepare();
                        logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                        logToast.show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                        logToast.show();
                        Looper.loop();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 4;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 4;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                } catch (IOException e) {
                    e.printStackTrace();
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 4;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                returnVal = R.string.appRTC_toast_socketConnector_check;
                Message msg2 = new Message();
                msg2.what = 4;
                msg2.arg1 = returnVal;
                mHandler.sendMessage(msg2);
            }
            return null;
        }

        @Override
        public void onPostExecute(JSONObject jsonResponse) {
            String msg = null;
            try {
                msg = jsonResponse.getString("msg");
                Log.d(TAG, "get json message:" + msg);
                if (responseCode == 200) {
                    Looper.prepare();
                    logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                } else {
                    Looper.prepare();
                    logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
                    logToast.show();
                    Looper.loop();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class LoginTask extends AsyncTask<ConnectionInfo, String, JSONObject> {
        // <傳入參數, 處理中更新介面參數, 處理後傳出參數>
        String network = "";
        String device = "";
        String os = "";
        String uriAPI = "";
        EasySSLSocketFactory socketFactory;
        JSONObject http_data = null;
        int returnVal;
        ConnectionInfo myConnectionInfo;

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        protected void onPreExecute() {
            uriAPI = "https://" + normalIP + ":" + normalPort + "/login";
            Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
            socketFactory = new EasySSLSocketFactory();
            http_data = new JSONObject();
            try {
                ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                Log.e(TAG, "ERROR:" + e.getStackTrace());
            }

            try {
                http_data.put("user_name", normalUsername);
                http_data.put("password", normalPassword);
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
            Log.d(TAG, "http_data:" + http_data.toString());
        }

        @Override
        protected JSONObject doInBackground(ConnectionInfo... connectionInfo) {
            myConnectionInfo = connectionInfo[0];
            try {
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", socketFactory, 9568));
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
                HttpPost post = new HttpPost(uriAPI);
                post.setHeader(HTTP.CONTENT_TYPE, "application/json");
                post.setHeader("brahma-lang", language);
                StringEntity entity = null;

                try {
                    entity = new StringEntity(http_data.toString());
                    post.setEntity(entity);
                    response = httpclient.execute(post);
                    responseCode = response.getStatusLine().getStatusCode();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    Log.d(TAG, "responseCode:" + responseCode);
                    Log.d(TAG, "response message:" + out.toString());
                    jsonResponse = new JSONObject(out.toString());
                    String msg = jsonResponse.getString("msg");
                    String status_code = jsonResponse.getString("status_code");
                    Log.d(TAG, "get json message:" + msg);
                    // 背景工作處理完"後"需作的事
                    if (responseCode == 200) {
                        if (status_code.equals("20000")) {
                            //judge type
                            String type = jsonResponse.getString("type");
                            if (type.equals("vmi")) {
                                _login = true;
                                authPrompt(myConnectionInfo);
                            } else {
                                appListObject = jsonResponse.getJSONObject("data");
                                user_id = jsonResponse.getString("user_id");
                                performanceToken = jsonResponse.getString("token");
                                loginApp();
                                Log.d(TAG, "appListObject:" + appListObject);
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
                    Log.e(TAG, "Failed to parse JSON response:", e);
                    Message msg2 = new Message();
                    msg2.what = 1;
                    mHandler.sendMessage(msg2);
                } catch (NoHttpResponseException e) {
                    if ("The target server failed to respond".equals(e.getMessage())) {
                        // connection failed, we tried to connect without using SSL but REST API's SSL is turned on
                        Log.e(TAG, "Client encryption is off but server encryption is on:", e);
                        returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    } else {
                        Log.e(TAG, "HTTP request failed, NoHttpResponseException:", e);
                        returnVal = R.string.appRTC_toast_socketConnector_nohttprespons;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    }

                } catch (SSLHandshakeException e) {
                    String msg = e.getMessage();
                    if (msg.contains("java.security.cert.CertPathValidatorException")) {
                        Log.e(TAG, "Untrusted server certificate!");
                        returnVal = R.string.appRTC_toast_socketConnector_failUntrustedServer;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    } else {
                        Log.e(TAG, "Error during SSL handshake: " + e.getMessage());
                        returnVal = R.string.appRTC_toast_socketConnector_failSSLHandshake;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    }
                } catch (SSLException e) {
                    if ("Connection closed by peer".equals(e.getMessage())) {
                        // connection failed, we tried to connect using SSL but REST API's SSL is turned off
                        Log.e(TAG, "Client encryption is on but server encryption is off:", e);
                        returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    } else {
                        Log.e(TAG, "SSL error:", e);
                        Message msg2 = new Message();
                        msg2.what = 1;
                        mHandler.sendMessage(msg2);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "HTTP request failed:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                } catch (IOException e) {
                    Log.e(TAG, "HTTP request failed:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            }
            return null;
        }

        public void onPostExecute(JSONObject jsonResponse) {
            String msg = null;
//            try {
//                    msg = jsonResponse.getString("msg");
//                    String status_code = jsonResponse.getString("status_code");
//                    Log.d(TAG, "get json message:" + msg);
//                    // 背景工作處理完"後"需作的事
//                    if (responseCode == 200) {
//                        if (status_code.equals("20000")) {
//                            //judge type
//                            String type = jsonResponse.getString("type");
//                            if (type.equals("vmi")) {
//                                _login = true;
//                                authPrompt(myConnectionInfo);
//                            } else {
//                                appListObject = jsonResponse.getJSONObject("data");
//                                user_id = jsonResponse.getString("user_id");
//                                performanceToken = jsonResponse.getString("token");
//                                loginApp();
//                                Log.d(TAG, "appListObject:" + appListObject);
//                            }
//                        } else {
//                            Looper.prepare();
//                            stopProgressDialog();
//                            logToast = Toast.makeText(BrahmaMainActivity.this, msg, Toast.LENGTH_LONG);
//                            logToast.show();
//                            Looper.loop();
//                        }
//                        login_type = "normal";
//                    } else {
//                        Message msg2 = new Message();
//                        msg2.what = 3;
//                        msg2.obj = msg;
//                        mHandler.sendMessage(msg2);
//                    }
//                    Message msg2 = new Message();
//                    msg2.what = 1;
//                    mHandler.sendMessage(msg2);
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
    }

    class QRcodeTask extends AsyncTask<JSONObject, String, JSONObject> {
        String uriAPI = "";
        EasySSLSocketFactory socketFactory;
        JSONObject http_data = null;
        ConnectionInfo connectionInfo = null;
        String network = "";
        String device = "";
        String os = "";
        int returnVal;

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        protected void onPreExecute() {
            String uriAPI = qrcodePath + "/loginByQrcode";
            Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
            socketFactory = new EasySSLSocketFactory();
            http_data = new JSONObject();

            //fake connectioninfo
            connectionInfo = dbHandler.getConnectionInfo(2);
            try {
                ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                Log.e(TAG, "ERROR:" + e.getStackTrace());
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "http_data:" + http_data.toString());
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {

            try {
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("https", socketFactory, 9568));
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
                HttpPost post = new HttpPost(uriAPI);
                post.setHeader(HTTP.CONTENT_TYPE, "application/json");
                post.setHeader("brahma-lang", language);
                StringEntity entity = null;

                try {
                    entity = new StringEntity(http_data.toString());
                    post.setEntity(entity);
                    response = httpclient.execute(post);
                    responseCode = response.getStatusLine().getStatusCode();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    Log.d(TAG, "responseCode:" + responseCode);
                    Log.d(TAG, "response message:" + out.toString());
                    jsonResponse = new JSONObject(out.toString());
                    String msg = jsonResponse.getString("msg");
                    String status_code = jsonResponse.getString("status_code");
                    Log.d(TAG, "get json message:" + msg);

                    if (responseCode == 200) {
                        if (status_code.equals("20000")) {
                            //judge type
                            String type = jsonResponse.getString("type");
                            if (!jsonResponse.isNull("qrcodeInfo")) {
                                qrcodeAPK = jsonResponse.getJSONObject("qrcodeInfo").getString("apk");
                                qrcodeFileName = jsonResponse.getJSONObject("qrcodeInfo").getString("file_name");
                                Log.d(TAG, "qrcodeFileName >>>>> " + qrcodeFileName);
                                Log.d(TAG, "qrcodeFileName.length() >>>>> " + qrcodeFileName.length());
                                if (qrcodeFileName.length() != 0) {
                                    packageName = qrcodeAPK + ";" + qrcodeFileName;
                                } else {
                                    packageName = qrcodeAPK;
                                }
                            }

                            if (type.equals("vmi")) {
                                _login = true;
                                authPrompt(connectionInfo);
                            } else {
                                appListObject = jsonResponse.getJSONObject("data");
                                user_id = jsonResponse.getString("user_id");
                                performanceToken = jsonResponse.getString("token");
                                loginApp();
                                Log.d(TAG, "appListObject:" + appListObject);
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

                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response:", e);
                    Message msg2 = new Message();
                    msg2.what = 1;
                    mHandler.sendMessage(msg2);
                } catch (SSLHandshakeException e) {
                    String msg = e.getMessage();
                    if (msg.contains("java.security.cert.CertPathValidatorException")) {
                        Log.e(TAG, "Untrusted server certificate!");
                        returnVal = R.string.appRTC_toast_socketConnector_failUntrustedServer;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    } else {
                        Log.e(TAG, "Error during SSL handshake: " + e.getMessage());
                        returnVal = R.string.appRTC_toast_socketConnector_failSSLHandshake;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    }
                } catch (SSLException e) {
                    if ("Connection closed by peer".equals(e.getMessage())) {
                        // connection failed, we tried to connect using SSL but REST API's SSL is turned off
                        Log.e(TAG, "Client encryption is on but server encryption is off:", e);
                        returnVal = R.string.appRTC_toast_socketConnector_failSSL;
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.arg1 = returnVal;
                        mHandler.sendMessage(msg2);
                    } else {
                        Log.e(TAG, "SSL error:", e);
                        Message msg2 = new Message();
                        msg2.what = 1;
                        mHandler.sendMessage(msg2);
                    }

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "HTTP request failed:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                } catch (IOException e) {
                    Log.e(TAG, "HTTP request failed:", e);
                    returnVal = R.string.appRTC_toast_socketConnector_check;
                    Message msg2 = new Message();
                    msg2.what = 2;
                    msg2.arg1 = returnVal;
                    mHandler.sendMessage(msg2);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
            }
            return null;
        }

        public void onPostExecute(JSONObject jsonResponse) {
//            String msg = null;
//            try {
//                if (jsonResponse.has("msg")) {
//                    msg = jsonResponse.getString("msg");
//                    String status_code = jsonResponse.getString("status_code");
//                    Log.d(TAG, "get json message:" + msg);
//
//                    if (responseCode == 200) {
//                        if (status_code.equals("20000")) {
//                            //judge type
//                            String type = jsonResponse.getString("type");
//                            if (!jsonResponse.isNull("qrcodeInfo")) {
//                                qrcodeAPK = jsonResponse.getJSONObject("qrcodeInfo").getString("apk");
//                                qrcodeFileName = jsonResponse.getJSONObject("qrcodeInfo").getString("file_name");
//                                Log.d(TAG, "qrcodeFileName >>>>> " + qrcodeFileName);
//                                Log.d(TAG, "qrcodeFileName.length() >>>>> " + qrcodeFileName.length());
//                                if (qrcodeFileName.length() != 0) {
//                                    packageName = qrcodeAPK + ";" + qrcodeFileName;
//                                } else {
//                                    packageName = qrcodeAPK;
//                                }
//                            }
//
//                            if (type.equals("vmi")) {
//                                _login = true;
//                                authPrompt(connectionInfo);
//                            } else {
//                                appListObject = jsonResponse.getJSONObject("data");
//                                user_id = jsonResponse.getString("user_id");
//                                performanceToken = jsonResponse.getString("token");
//                                loginApp();
//                                Log.d(TAG, "appListObject:" + appListObject);
//                            }
//                        } else {
//                            Message msg2 = new Message();
//                            msg2.what = 3;
//                            msg2.obj = msg;
//                            mHandler.sendMessage(msg2);
//                        }
//                        login_type = "qrcode";
//                    } else {
//                        Message msg2 = new Message();
//                        msg2.what = 3;
//                        msg2.obj = msg;
//                        mHandler.sendMessage(msg2);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        }
    }

//    /*** 送出login API for web server ***/
//    class sendPostRunnable implements Runnable {
//        ConnectionInfo connectionInfo;
//
//        public sendPostRunnable(ConnectionInfo connectionInfo) {
//            this.connectionInfo = connectionInfo;
//        }
//
//        @Override
//        public void run() {
//            String result = sendPostDataToInternet(connectionInfo);
//        }
//    }

//    class sendQRcodePostRunnable implements Runnable {
//        String qrcodePath;
//        String qrcodeID;
//
//        public sendQRcodePostRunnable(String qrcodePath, String qrcodeID) {
//            this.qrcodePath = qrcodePath;
//            this.qrcodeID = qrcodeID;
//        }
//
//        @Override
//        public void run() {
//            String result = sendQRcodePostDataToInternet(qrcodePath, qrcodeID);
//        }
//    }

//    class sendRestPasswordRunnable implements Runnable {
//        String host;
//        String port;
//        String username;
//
//        public sendRestPasswordRunnable(String host, String port, String username) {
//            this.host = host;
//            this.port = port;
//            this.username = username;
//        }
//
//        @Override
//        public void run() {
//            String result = sendResetPasswordToInternet(host, port, username);
//        }
//    }


    /**
     * BroadcastReceiver
     */
    public class MyDialogReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MyDialogReceiver action:" + intent.getAction());
            Bundle bundle = intent.getExtras();
            stopProgressDialog();
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "AppRTCClient.auto_connect >>>>> " + AppRTCClient.auto_connect);
            Bundle bundle = intent.getExtras();
            times = bundle.getInt("reConnectTimes");
            myState = bundle.getString("myState");
            boolean auto_connect = bundle.getBoolean("auto_connect");
            String loginType = bundle.getString("type");

            machine.setState(StateMachine.STATE.ERROR, R.string.appRTC_toast_connection_finish, getResources().getString(R.string.appRTC_toast_connection_finish));
            mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

            if (StateMachine.STATE.ERROR == machine.getState() && loginType.equals("vmi") && auto_connect) {
                if (times <= 5 && myState.equals("ERROR")) {
                    Log.d(TAG, "doing reconnect");

                    reconnect_tv.setText("Reconnect to Server ,please wait...\nReconnect times:" + times + "/5");
                    reconnect_dialog.show();
                    //如果未連線的話，mNetworkInfo會等於null
                    ConnectionInfo connectionInfo = dbHandler.getConnectionInfo(2);
                    _login = true;
                    authPrompt(connectionInfo);

//                    } else {
//                        Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_SHORT).show();
//                    }
                } else {
//                    mDialog.dismiss();
                    reconnect_dialog.dismiss();
                    if (myState.equals("CONNECTED") || myState.equals("STARTED")) {
                        //Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_success, Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(BrahmaMainActivity.this, R.string.appRTC_toast_socketConnector_check, Toast.LENGTH_LONG).show();
                }
            } else {
                reconnect_dialog.dismiss();
                times = 0;
            }

            AppRTCClient.auto_connect = true;
        }
    }


}
