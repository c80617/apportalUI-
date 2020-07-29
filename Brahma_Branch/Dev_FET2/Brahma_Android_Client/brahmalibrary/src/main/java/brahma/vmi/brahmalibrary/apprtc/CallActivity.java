package brahma.vmi.brahmalibrary.apprtc;
/**
 * @file CallActivity
 * @brief 進行webrtc連接與顯示畫面之activity
 * @author YiWen Li
 * @date 2019/07/12
 **/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.system.ErrnoException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.activities.AppRTCActivity;
import brahma.vmi.brahmalibrary.client.ConfigHandler;
import brahma.vmi.brahmalibrary.client.KeyHandler;
import brahma.vmi.brahmalibrary.client.RotationHandler;
import brahma.vmi.brahmalibrary.client.TouchHandler;
import brahma.vmi.brahmalibrary.client.VirtualKeyboardHandler;
import brahma.vmi.brahmalibrary.client.VolumeChangeObserver;
import brahma.vmi.brahmalibrary.database.Item;
import brahma.vmi.brahmalibrary.database.ItemBio;
import brahma.vmi.brahmalibrary.database.Login;
import brahma.vmi.brahmalibrary.database.MyDBHelper;
import brahma.vmi.brahmalibrary.keyboard.KeyboardHeightObserver;
import brahma.vmi.brahmalibrary.keyboard.KeyboardHeightProvider;
import brahma.vmi.brahmalibrary.netspeed.SpeedCalculationService;
import brahma.vmi.brahmalibrary.netspeed.WindowUtil;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity;
import brahma.vmi.brahmalibrary.wcitui.GridActivityAfter;
import brahma.vmi.brahmalibrary.widgets.MyEditText;

import static android.media.AudioManager.ACTION_HEADSET_PLUG;
import static android.system.Os.setenv;
import static brahma.vmi.brahmalibrary.client.KeyHandler.isKeyEvent;
import static brahma.vmi.brahmalibrary.client.PingLatency.fps;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.SafeInsetTop;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.device_type;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.heightPixels;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.heightPixels_org;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.login_type;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalPassword;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalPort;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalUsername;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.resolution_ratio;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.widthPixels;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.widthPixels_org;

//import brahma.vmi.brahmalibrary.wcitui.tab.AudioDialog;

public class CallActivity extends AppRTCActivity implements PCObserver.PeerConnectionEvents, VolumeChangeObserver.VolumeChangeListener, KeyboardHeightObserver {
    public static final String INIT_X = "init_x";
    public static final String INIT_Y = "init_y";
    public static final String IS_SHOWN = "is_shown";
    private static final String TAG = "CallActivity";
    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};
    // Peer connection statistics callback period in ms.
    private final static int MSG_ID_VKB_SEND_STRING = 1;
    private final static int MSG_ID_VKB_SEND_KEYCODE = 2;
    private final static int MSG_ID_VKB_INIT_HEIGHT = 3;
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final String VIDEO_H264_HIGH_PROFILE_FIELDTRIAL = "WebRTC-H264HighProfile/Enabled/";
    private static final String VIDEO_FRAME_EMIT_FIELDTRIAL =
            PeerConnectionFactory.VIDEO_FRAME_EMIT_TRIAL + "/" + PeerConnectionFactory.TRIAL_ENABLED
                    + "/";
    //Record Keyboard Status
    public static boolean keyboard_shown;
    public static String pkgName; // what app we want to launch when we finish connecting
    public static int vmX = -1;
    public static int vmY = -1;
    public static int vmRotation = 0;//landscape
    public static Handler handler;
    public static Context callActivityContext;
    public static int KBheight = 0;
    public static String packageName = "";
    public static int RESULTCODE_APPLIST = 98989;
    public static int RESULTCODE_CLOSECALL = 55555;
    //audio
    static MediaStream ms = null;
    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    //    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<VideoRenderer.Callbacks>();
    // Get the SurfaceView layout parameters
    public int frameX = -1;
    public int frameY = -1;
    public float videoProportion = -1;
    public float screenProportion = -1;
    protected IntentFilter intentFilter2;
    protected HeadsetReceiver myBroadcastReceiver2;
    protected IntentFilter intentFilter3;
    protected BluetoothConnectionReceiver bluetoothReceiver;
    //    protected AppstreamingReceiver appstreamingReceiver;
    protected IntentFilter intentFilter4;
    int oldRotation = 0;
    int newRotation = -1;
    int frameCount = 0;
    int beforeL;
    int NETREQUEST_CODE = 55;
    PeerConnectionFactory.Options options = null;
    InnerRecevier innerReceiver = new InnerRecevier();
    Handler mHanlder2;
    AudioFocusRequest mFocusRequest;
    AudioAttributes mPlaybackAttributes;
    //    AudioDialog audiodialog;
    ItemBio itemBio;
    private AppRTCClient.SignalingParameters signalingParameters;
    //    private MySurfaceViewRender pipRenderer;
    private SurfaceViewRenderer pipRenderer;
    private VideoFileRenderer videoFileRenderer;
    private Toast logToast;
    private boolean activityRunning;
    private boolean iceConnected;
    private boolean isError;
    private MyEditText inputText = null;
    private KeyboardHeightProvider keyboardHeightProvider;
    private VirtualKeyboardHandler kbHandler;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ID_VKB_INIT_HEIGHT:
                    if (msg.obj == null) break;
                    kbHandler.sendVKeyboardReqMessage(VirtualKeyboardHandler.TYPE_HEIGHT, msg.obj);
                    break;
                case MSG_ID_VKB_SEND_STRING:
                    if (msg.obj == null) break;
                    kbHandler.sendVKeyboardReqMessage(VirtualKeyboardHandler.TYPE_STRING, msg.obj);
                    break;
                case MSG_ID_VKB_SEND_KEYCODE:
                    if (msg.obj == null) break;
                    kbHandler.sendVKeyboardReqMessage(VirtualKeyboardHandler.TYPE_KEYCODE, msg.obj);
                    break;
            }
        }
    };
    private MediaConstraints sdpMediaConstraints;
    private SDPObserver sdpObserver;
    //    private VideoStreamsView vsv = null;
    private PCObserver pcObserver;
    private TouchHandler touchHandler;
    private RotationHandler rotationHandler;
    private AudioManager audioMgr;
    private KeyHandler keyHandler;
    private ConfigHandler configHandler;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = null;
    private VolumeChangeObserver mVolumeChangeObserver;
    private boolean hideKeyboard = true;
    private AlertDialog touchID_dialog;
    private RendererCommon.RendererEvents rendererEvents = new RendererCommon.RendererEvents() {
        @Override
        public void onFirstFrameRendered() {
        }

        @Override
        public void onFrameResolutionChanged(int width, int height, int orientation) {
            Log.d(TAG, "onFrameResolutionChanged!!!!!!!");
//            setAspectRatio(width, height, orientation);

        }
    };
    private int realw;
    private int realh;
    private SpeedDialView speedDialView;
    Handler floatingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            initMaterialFloatinButton(vmRotation);
        }
    };
    private DisplayMetrics dm;
    private int widthPixels_call;
    private int heightPixels_call;
    private int REQUESTCODE_BAKETOAPPLIST = 78787;
    private boolean onOpen;
    private RenderServerView mRenderServerView;
    private SurfaceHolder mSurfaceHolder;
    private boolean initialized = false;



    public static MediaStream getMediaStream() {
        return ms;
    }

    public void setMediaStream(MediaStream ms) {
        CallActivity.ms = ms;
    }

    public void setAspectRatio(int width, int height, int orientation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "setAspectRatio");
                pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

//                                AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.videoFrameLayout);
//                layout.setAspectRatio(((double)originalWidth) / (double) originalHeight);
//                videoView = (GLSurfaceView) findViewById(R.id.videoView);
//                int width_ = videoView.getWidth();
//                int viewHeight = (width_ * originalHeight ) / originalWidth;
//                Log.i("Example", "Setting height to " + viewHeight + " " + width_);
//                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
//                lp.height = viewHeight;
            }
        });
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = true;
                remoteProxyRenderer.setTarget(pipRenderer);
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "ICE disconnected");
//                logAndToast(getResources().getString(R.string.connection_end));
                iceConnected = false;
                disconnect();
                //close websocket
                if (appRtcClient != null)
                    appRtcClient.disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
    }

    @Override
    public void onPeerConnectionError(String description) {
    }

    @Override
    public void onVolumeChanged(int volume) {
        if (isKeyEvent) {
            isKeyEvent = false;
        } else {
            keyHandler.setMediaVolumeOppsite(volume);
            isKeyEvent = true;
        }
        hideNavigationBar();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycle", "Callactivity onCreate!!!");
        callActivityContext = this;

        final Intent intent = getIntent();
        pkgName = intent.getStringExtra("pkgName");
        getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE); //Brahma limit screenshot
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);//The Brahma screen does not close
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences settings = getSharedPreferences("user", 0);
        KBheight = settings.getInt("KbHeight", 0);

        keyboard_shown = false;
        iceConnected = false;
        signalingParameters = null;

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        widthPixels_call = dm.widthPixels;
        heightPixels_call = dm.heightPixels;

        if (checkDrawOverlayPermission()) {
            init();
        }
        hideNavigationBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            getWindow().setAttributes(lp);
        }
        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }

        intentFilter2 = new IntentFilter();
        intentFilter2.addAction(ACTION_HEADSET_PLUG);
        myBroadcastReceiver2 = new HeadsetReceiver();
        registerReceiver(myBroadcastReceiver2, intentFilter2);

        intentFilter3 = new IntentFilter();
        intentFilter3.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter3.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothReceiver = new BluetoothConnectionReceiver();
        registerReceiver(bluetoothReceiver, intentFilter3);

//        intentFilter4 = new IntentFilter();
//        intentFilter4.addAction("APPSTREAMING_MODE");
//        appstreamingReceiver = new AppstreamingReceiver();
//        registerReceiver(appstreamingReceiver, intentFilter4);

        //動態註冊廣播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(innerReceiver, intentFilter);

        mVolumeChangeObserver = new VolumeChangeObserver(this);
        mVolumeChangeObserver.setVolumeChangeListener(this);

        mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                    .build();
        }
        mHanlder2 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TimeCount tc = new TimeCount(1000, 100);
                tc.start();
                super.handleMessage(msg);
            }
        };

        //計算bandwidth的timer
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mHanlder2.sendEmptyMessage(1);//通知UI更新
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 5000);

        MyDBHelper dbHelper = new MyDBHelper(this);
        itemBio = new ItemBio(dbHelper.getDatabase(this));
        if (itemBio.getCount() == 0) {
            itemBio.sample();
        }
        if (itemBio.getCount_login() == 0) {
            itemBio.sample2();
        }

        Login item2 = new Login(1, "true", normalUsername, normalPassword, normalIP, normalPort, "");
        itemBio.update(item2);

        //setting bioView UI
        View bioView = LayoutInflater.from(CallActivity.this).inflate(R.layout.dialog_using_bio, null);
        TextView oldUsername = (TextView) bioView.findViewById(R.id.oldUsername);
        TextView newUsername = (TextView) bioView.findViewById(R.id.newUsername);
        if (itemBio.get(1).getbioUsername() != null) {
            oldUsername.append(itemBio.get(1).getbioUsername());
        }
        if (normalUsername != null) {
            newUsername.append(normalUsername);
        }
        //create dialog
        MaterialAlertDialogBuilder touchIDbuilder = new MaterialAlertDialogBuilder(CallActivity.this);
        // Add the buttons
        touchIDbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d("isUsingBio", "user choose use bio");
                Item item = new Item(1, "true", normalUsername, normalPassword, normalIP, normalPort);
                boolean sqlupdate = itemBio.update(item);
                Log.d("isUsingBio", "sqlupdate >>>>> " + sqlupdate);
            }
        });
        touchIDbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "使用者不記憶Bio");
            }
        });

        // Create the AlertDialog
        touchIDbuilder.setTitle(R.string.update_verfication);
        touchIDbuilder.setView(bioView);
        touchID_dialog = touchIDbuilder.create();


        keyboardHeightProvider = new KeyboardHeightProvider(this);
        View view2 = findViewById(R.id.call_rl);
        view2.post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });

        speedDialView = (SpeedDialView) findViewById(R.id.speedDial);
//        speedDialView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
//                Log.d("uouououo", "onLayoutChange:"+i);
//                if (view.getWidth() != speedialView_width) {
//                    speedialView_width_exp = view.getWidth();
//                }
//                Log.d("uouououo", "speedialView_width_exp:"+speedialView_width_exp);
//                RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) view.getLayoutParams();
////                if(layoutParams2.leftMargin == widthPixels_org-speedialView_width){
//                    speedDialView.setExpansionMode(SpeedDialView.ExpansionMode.LEFT);
//                    layoutParams2.leftMargin = widthPixels_org - speedialView_width_exp;
//                    speedDialView.setLayoutParams(layoutParams2);
//                    speedDialView.invalidate();
////                }
//            }
//        });


        speedDialView.setOnTouchListener(new View.OnTouchListener() {

            private int _xDelta;
            private int _yDelta;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "speedDialView onTouch");
                final int X = (int) motionEvent.getRawX();
                final int Y = (int) motionEvent.getRawY();
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        _xDelta = X - lParams.leftMargin;
                        _yDelta = Y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_UP:
                        lParams.leftMargin = 0;
                        view.setLayoutParams(lParams);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

                        int l = X - _xDelta;
                        int t = Y - _yDelta;
                        int limitWidth = widthPixels_call - view.getWidth();
                        int limitHeight = heightPixels_call - view.getHeight();
                        Log.d("uououou", "limitWidth:" + limitWidth);
                        Log.d("uououou", "limitHeight:" + limitHeight);
                        Log.d("uououou", widthPixels_call + " x " + heightPixels_call);

                        if (l > limitWidth) {
                            layoutParams.leftMargin = limitWidth;//貼右
                        } else if (l < 0) {
                            layoutParams.leftMargin = 0;//貼左
                        } else {
                            layoutParams.leftMargin = l;
                        }

                        if (t > limitHeight) {
                            layoutParams.topMargin = limitHeight;
                        } else if (t < 0) {
                            layoutParams.topMargin = 0;
                        } else {
                            layoutParams.topMargin = t;
                        }

                        layoutParams.rightMargin = -250;
                        layoutParams.bottomMargin = -250;
                        view.setLayoutParams(layoutParams);
                        break;
                }
                view2.invalidate();
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkDrawOverlayPermission() {
        Log.d("yoyoyo", "checkDrawOverlayPermission:" + Settings.canDrawOverlays(this));
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, NETREQUEST_CODE);
            return false;
        }
        return true;
    }

    private void init() {
        WindowUtil.statusBarHeight = getStatusBarHeight();
    }

    private int getStatusBarHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void connectToRoom() {
        Log.d("LifeCycle", "Callactivity connectToRoom!");
        onOpen = false;
        hideKeyboard = true;
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        if (this.getFlagRemoteLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Brahma LANDSCAPE
            int width = displaySize.x;
            int height = displaySize.y;
            Log.d(TAG, "displaySize(width,height)=(" + width + "," + height + ")");
            displaySize.x = height;
            displaySize.y = width;
            Log.d(TAG, "sway,displaySize(width,height)=(" + displaySize.x + "," + displaySize.y + ")");
        }

        setContentView(R.layout.activity_call);
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);

        remoteRenderers.add(remoteProxyRenderer);

        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int navigation_bar_height = resources.getDimensionPixelSize(resourceId);
            Log.d(TAG, "navigation_bar_height:" + navigation_bar_height);
        }

        int resourceId2 = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId2 > 0) {
            int status_bar_height = getApplicationContext().getResources().getDimensionPixelSize(resourceId2);
            Log.d(TAG, "status_bar_height:" + status_bar_height);
        }

        EglBase rootEglBase = EglBase.create();
        pipRenderer.init(rootEglBase.getEglBaseContext(), rendererEvents);
        pipRenderer.getHolder().setFixedSize(displaySize.x, displaySize.y);
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        pipRenderer.setZOrderMediaOverlay(false);
        pipRenderer.setEnableHardwareScaler(true);

        inputText = findViewById(R.id.inputText2);
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                Log.d("KeyEvents", "onTextChanged = " + s);
                //因原先setText("")會造成使用數字鍵盤輸入之後,輸入法跳到其他頁面的問題.
                //輸入法
                if (s.length() > 0 && !StartWithZhuyin(s.toString())) {
                    int ascii = (int) s.charAt(0);
                    Log.d("KeyEvents", "ascii = " + ascii);
                    if (ascii == 10) {
                        Message msg = new Message();
                        msg.what = MSG_ID_VKB_SEND_KEYCODE;
                        msg.obj = KeyEvent.KEYCODE_ENTER;
                        mHandler.sendMessage(msg);
                        inputText.setText("");
                    } else {
                        Message msg = new Message();
                        msg.what = MSG_ID_VKB_SEND_STRING;
                        msg.obj = s.toString();//.substring(s.toString().length()-1);
                        mHandler.sendMessage(msg);
                        if (ascii >= 48 && ascii <= 57) {
                            //Numeral 0~9
                            inputText.getText().clear();
                        } else {
                            inputText.setText("");
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                Log.d("KeyEvents", "beforeTextChanged = " + s);
                Log.d("KeyEvents", "length: " + s.toString().length() + " ,after: " + after);
                beforeL = s.toString().length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                Log.d("KeyEvents", "afterTextChanged = " + s.toString());
            }
        });

        MyEditText.SetOnBackPressedListener((keyCode, event) -> {
            // TODO Auto-generated method stub
            Log.d("KeyEvents", "KeyEvent.KEYCODE_ENTER: " + KeyEvent.KEYCODE_ENTER + ", keyCode: " + keyCode);
            Message msg = new Message();
            msg.what = MSG_ID_VKB_SEND_KEYCODE;
            msg.obj = KeyEvent.KEYCODE_BACK;
            mHandler.sendMessage(msg);
        });

        touchHandler = new TouchHandler(this, displaySize);
        rotationHandler = new RotationHandler(this);
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        keyHandler = new KeyHandler(this, audioMgr);
        configHandler = new ConfigHandler(this);
        kbHandler = new VirtualKeyboardHandler(this);
//        assert audioMgr != null;
//        audiodialog = new AudioDialog(this, audioMgr.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));

        //audio manager request focus
        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        };
        requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // Initialize field trials.
        PeerConnectionFactory.initializeInternalTracer();
        String fieldTrials = "";
        fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
        fieldTrials += VIDEO_FRAME_EMIT_FIELDTRIAL;
        fieldTrials += VIDEO_H264_HIGH_PROFILE_FIELDTRIAL;
        PeerConnectionFactory.initializeFieldTrials(fieldTrials);
        Log.d(TAG, "Field trials: " + fieldTrials);

        PeerConnectionFactory.initializeAndroidGlobals(this, true);
        if (options != null) {
            Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask);
        }
        //WebRTC63
//        PeerConnectionFactory factory = new PeerConnectionFactory(options);

        //Create observers.
        sdpObserver = new SDPObserver(this);
        pcObserver = new PCObserver(this);
        pcObserver.addRender(remoteRenderers);

        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuOveruseDetection", "false"));

        //check bluetooth
        if (audioMgr.isBluetoothA2dpOn())
            setAudioDevice(AudioDevice.BLUEBOOTH_HEADSET);
        else
            setAudioDevice(AudioDevice.SPEAKER_PHONE);
//
//        mFloatingView = new FloatingView(getApplicationContext());
//        mFloatingView.setFabListener(fablistener);
//        mFloatingView.show(vmRotation);


        hideNavigationBar();
        super.connectToRoom();
    }

    private void initMaterialFloatinButton(int vmRotation) {

        speedDialView.setExpansionMode(SpeedDialView.ExpansionMode.RIGHT);
//        speedDialView.setExpansionMode(SpeedDialView.ExpansionMode.LEFT);
//        speedDialView.setExpansionMode(SpeedDialView.ExpansionMode.TOP);
        speedDialView.setMainFabOpenedBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.item_nomal, getTheme()));
        speedDialView.setMainFabClosedBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.item_nomal, getTheme()));
        speedDialView.getMainFab().setSize(FloatingActionButton.SIZE_MINI);

        LayoutParams mParams = new LayoutParams();
//        mParams.gravity = Gravity.TOP | Gravity.START;
        mParams.x = 0;
        oldRotation = 0;
        if (vmRotation == 0 || vmRotation == 2) {
            mParams.y = heightPixels_org / 3;
        } else {
            mParams.y = widthPixels_org / 3;
        }
        //设置图片格式，效果为背景透明
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        mParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;

//        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        windowManager.addView(speedDialView,mParams);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mParams.width, mParams.height);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
//        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.setMargins(0, mParams.y, 0, 0);

        speedDialView.setLayoutParams(params);
        speedDialView.setVisibility(View.VISIBLE);

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_menu, R.mipmap.menu_ftb)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.item_nomal, getTheme()))
                .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.color_primaryDark, getTheme()))
                .setLabelClickable(true)
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_back, R.mipmap.back_ftb)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.item_nomal, getTheme()))
                .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.color_primaryDark, getTheme()))
                .setLabelClickable(true)
                .create());
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_home, R.mipmap.home_ftb)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.item_nomal, getTheme()))
                .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.color_primaryDark, getTheme()))
                .setLabelClickable(true)
                .create());

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_multi_app, R.mipmap.mulitapp_ftb)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.item_nomal, getTheme()))
                .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.color_primaryDark, getTheme()))
                .setLabelClickable(true)
                .create());

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                Log.d(TAG, "onActionSelected");
                switch (speedDialActionItem.getId()) {
                    case R.id.fab_menu:
                        KeyEvent eventMenu = new KeyEvent(KeyEvent.ACTION_UP, 299);
                        keyHandler.tryConsume(eventMenu);
                        return true; // true to keep the Speed Dial open
                    case R.id.fab_back:
                        Message msg = new Message();
                        msg.what = MSG_ID_VKB_SEND_KEYCODE;
                        msg.obj = KeyEvent.KEYCODE_BACK;
                        mHandler.sendMessage(msg);
                        return true; // true to keep the Speed Dial open
                    case R.id.fab_home:
                        KeyEvent eventHome = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME);
                        keyHandler.tryConsume(eventHome);
                        return true; // true to keep the Speed Dial open
                    case R.id.fab_multi_app:
                        KeyEvent eventMultiApp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_APP_SWITCH);
                        keyHandler.tryConsume(eventMultiApp);
                        return true; // true to keep the Speed Dial open
                    default:
                        return false;
                }
            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
//        mFloatingView.hide();
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
    }

    @Override
    public void onDestroy() {
        Log.d("LifeCycle", "Callactivity onDestroy!!!");
        dbHandler.close();
//        itemBio.close();
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        PeerConnectionFactory.shutdownInternalTracer();
        disconnectAndExit();
        unregisterReceiver(bluetoothReceiver);
        unregisterReceiver(myBroadcastReceiver2);
        unregisterReceiver(innerReceiver);
//        unregisterReceiver(appstreamingReceiver);
        keyboardHeightProvider.close();
        hideSoftKeyboard();

        super.onDestroy();
    }

    private void disconnect() {
        Log.e("LifeCycle", "Callactivity disconnect");
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }

        abandonAudioFocus(mAudioFocusChangeListener);

        if (iceConnected && !isError) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        Intent intent = new Intent(CallActivity.this, BrahmaMainActivity.class);
        startActivity(intent);
        finish();
//        finish();
    }

    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        logToast.show();
    }

    public SurfaceViewRenderer getSVR() {
        return pipRenderer;
    }

    /**
     * Changes selection of the currently active audio device.
     */
    public void setAudioDevice(AudioDevice device) {
        Log.d(TAG, "setAudioDevice(device=" + device + ")");
        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                audioMgr.setBluetoothScoOn(false);
                audioMgr.stopBluetoothSco();
                break;
            case EARPIECE:
            case WIRED_HEADSET://耳機
                setSpeakerphoneOn(false);
                audioMgr.setBluetoothScoOn(false);
                audioMgr.stopBluetoothSco();
                break;
            case BLUEBOOTH_HEADSET://藍芽耳機
                setSpeakerphoneOn(false);
                audioMgr.setBluetoothScoOn(true);
                audioMgr.startBluetoothSco();
                break;
            default:
                Log.e(TAG, "Invalid audio device selection");
                break;
        }
    }

    /**
     * Sets the speaker phone mode.
     */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioMgr.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioMgr.setSpeakerphoneOn(on);
    }

    private boolean StartWithZhuyin(String str) {//進行中文轉換
        String unistring = Integer.toHexString(str.charAt(0) | 0x10000).substring(1);
        if (!unistring.startsWith("3")) return false;
        int unicode = Integer.parseInt(unistring, 16);
        if (unicode >= 12549 && unicode <= 12585) {
            return true;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // send the updated configuration to the VM (i.e. whether or not a hardware keyboard is plugged in)
        configHandler.handleConfiguration(newConfig);
    }

    public PCObserver getPCObserver() {
        return pcObserver;
    }

    // called from PCObserver
    public MediaConstraints getPCConstraints() {
        MediaConstraints value = null;
        if (appRtcClient != null)
            value = appRtcClient.getSignalingParams().pcConstraints;
        return value;
    }

    @Override
    protected void startProgressDialog() {
//        if (vsv != null)
//            vsv.setBackgroundColor(Color.DKGRAY); // if it isn't already set, make the background color dark gray
        super.startProgressDialog();
    }

    @Override
    public void onPause() {
        Log.d("LifeCycle", "Callactivity onPause!!!");

//        if (!checkOpenCamera()) {
//            if (vsv != null) vsv.onPause();
//        }

//        stopService(new Intent(this, NetMService.class));
        stopService(new Intent(CallActivity.this, SpeedCalculationService.class));
        mVolumeChangeObserver.unregisterReceiver();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
        hideSoftKeyboard();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult >>>>> " + requestCode);
        Log.d(TAG, "onActivityResult >>>>> " + resultCode);
        if (requestCode == REQUESTCODE_BAKETOAPPLIST) {
            if (resultCode == RESULTCODE_APPLIST) {
                pkgName = data.getStringExtra("packageName");
                Log.d(TAG, "onActivityResult packageName>>>>> " + packageName);
                sendAppsMessage();
            } else if (resultCode == RESULTCODE_CLOSECALL) {
                disconnectAndExit();
                Intent intent = new Intent();
                intent.setClass(CallActivity.this, BrahmaMainActivity.class);
                startActivity(intent);
            }
        }
//        Log.d(TAG, "onActivityResult >>>>> "+data.getStringExtra("mode"));
    }

    @Override
    public void onResume() {
        Log.d("LifeCycle", "Callactivity onResume!!!");
        hideNavigationBar();
//        resetFloatingView(vmRotation);
        if (!checkOpenCamera()) {
//            if (vsv != null) vsv.onResume();
        } else {
            setOpenCamera(false);
        }
        mVolumeChangeObserver.registerReceiver();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
        super.onResume();
    }

    // MessageHandler interface method
    // Called when the client connection is established
    @Override
    public void onOpen() {
        Log.d("LifeCycle", "Callactivity onOpen!!!");
        super.onOpen();
        // set up ICE servers
        pcObserver.onIceServers(appRtcClient.getSignalingParams().iceServers);

        // send timezone information
        BRAHMAProtocol.Request.Builder request = BRAHMAProtocol.Request.newBuilder();
        request.setType(BRAHMAProtocol.Request.RequestType.TIMEZONE);
        request.setTimezoneId(TimeZone.getDefault().getID());
        sendMessage(request.build());

        touchHandler.sendScreenInfoMessage();
        rotationHandler.initRotationUpdates();
        rotationHandler.initRotationPortrait();

        // send the initial configuration to the VM
        Configuration config = getResources().getConfiguration();
        configHandler.handleConfiguration(config);

        // tell the VM what app we want to start
        sendAppsMessage();

        PeerConnection pc = pcObserver.getPC();
        if (pc != null)
            pc.createOffer(sdpObserver, sdpMediaConstraints);

        startService(new Intent(CallActivity.this, SpeedCalculationService.class));
        hideSoftKeyboard();
        floatingHandler.sendEmptyMessage(0);
        onOpen = true;
        // }
        //}
    }

    // sends "APPS" request to VM; if pkgName is not null, start that app, otherwise go to the Launcher
    private void sendAppsMessage() {
        BRAHMAProtocol.AppsRequest.Builder aBuilder = BRAHMAProtocol.AppsRequest.newBuilder();
        aBuilder.setType(BRAHMAProtocol.AppsRequest.AppsRequestType.LAUNCH);
        // if we've been given a package name, start that app
        if (pkgName != null) {
            aBuilder.setPkgName(pkgName);
            Log.d(TAG, "sendAppsMessage pkgName:" + pkgName);
        }

        BRAHMAProtocol.Request.Builder rBuilder = BRAHMAProtocol.Request.newBuilder();
        rBuilder.setType(BRAHMAProtocol.Request.RequestType.APPS);
        rBuilder.setApps(aBuilder);
        sendMessage(rBuilder.build());
        Log.d(TAG, "rBuilder.build():" + rBuilder.build().toString());
    }

    public Activity getActivity() {
        return this;
    }

    // MessageHandler interface method
    // Called when a message is sent from the server, and the SessionService doesn't consume it
    public boolean onMessage(BRAHMAProtocol.Response data) {
        switch (data.getType()) {
            case APPS:
                if (data.hasApps() && data.getApps().getType() == BRAHMAProtocol.AppsResponse.AppsResponseType.EXIT) {
                    Log.d(TAG, "onMessage:" + data.getApps().getType());
                    Log.d(TAG, "get APPS EXIT !!!!!");

                    OpenAppMode = true;
                    Intent intent = new Intent(CallActivity.this, GridActivityAfter.class);
                    startActivityForResult(intent, REQUESTCODE_BAKETOAPPLIST);
//                    disconnectAndExit();
                }
                break;
            case SCREENINFO:
                handleScreenInfo(data);
                setBioSetting();
                setSize();
                break;
            case VKeyboardInfo:
                handleKbShowStatus(data);
                break;
            case WEBRTC:
                try {
                    JSONObject json = new JSONObject(data.getWebrtcMsg().getJson());
                    Log.d(TAG, "Received WebRTC message from peer:\n" + json.toString(4));
                    String type;
                    // peerconnection_client doesn't put a "type" on candidates
                    try {
                        type = (String) json.get("type");
                    } catch (JSONException e) {
                        json.put("type", "candidate");
                        type = (String) json.get("type");
                    }
                    Log.d(TAG, "Received WebRTC message " + type);
                    //Check out the type of WebRTC message.
                    switch (type) {
                        case "candidate":
                            IceCandidate candidate = new IceCandidate(
                                    (String) json.get("id"),
                                    json.getInt("label"),
                                    (String) json.get("candidate"));
                            getPCObserver().addIceCandidate(candidate);
                            break;
                        case "answer":
                        case "offer":
                            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type),
                                    (String) json.get("sdp"));

                            Log.d(TAG, "WEBRTC sdp:" + sdp.description);
                            getPCObserver().getPC().setRemoteDescription(sdpObserver, sdp);
                            break;
                        case "bye":
//                            logAndToast(R.string.appRTC_toast_clientHandler_finish);

                            disconnectAndExit();
                            break;
                        default:
                            throw new RuntimeException("Unexpected message: " + data);
                    }
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    throw new RuntimeException(e);
                }
                break;
            default:
                // any messages we don't understand, pass to our parent for processing
                super.onMessage(data);
        }
        return true;
    }

    private void setBioSetting() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                if (!itemBio.get(1).getbioUsername().equals(normalUsername) && login_type != "qrcode")
                    touchID_dialog.show();
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
//                Toast.makeText(this, R.string.BIOMETRIC_ERROR_NO_HARDWARE, Toast.LENGTH_LONG).show();
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
//                Toast.makeText(this, R.string.BIOMETRIC_ERROR_HW_UNAVAILABLE, Toast.LENGTH_LONG).show();
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
//                Toast.makeText(this, R.string.BIOMETRIC_ERROR_NONE_ENROLLED, Toast.LENGTH_LONG).show();
                Log.e("MY_APP_TAG", "The user hasn't associated any biometric credentials with their account.");
                break;
        }

//        BiometricPromptManager mManager = BiometricPromptManager.from(this);
//        if (mManager.isBiometricPromptEnable()) {
//            if (!itemBio.get(1).getbioUsername().equals(normalUsername) && login_type != "qrcode")
//                touchID_dialog.show();
//            Log.d("isUsingBio", "成功進入生物辨識setting");
//        } else {
//            Log.d("isUsingBio", "無法使用生物辨識");
//        }
    }

    private void setSize() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);

        vmX = widthPixels_org;
        vmY = heightPixels_org;

        // Get the width of the screen
        int screen1 = 0;
        int screen2 = 0;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        if (displaySize.y >= displaySize.x) {
            screen1 = displaySize.x;
            screen2 = displaySize.y;
            realw = metrics.widthPixels;
            realh = metrics.heightPixels;
        } else {//x,y互換
            screen1 = displaySize.y;
            screen2 = displaySize.x;
            realw = metrics.heightPixels;
            realh = metrics.widthPixels;
        }

        videoProportion = (float) vmX / (float) vmY;
        screenProportion = (float) screen1 / (float) screen1;
        Log.d(TAG, "videoProportion = " + videoProportion + " ; screenProportion = " + screenProportion);

        if (videoProportion > screenProportion) {//frame比例較大
            frameX = screen1;
            frameY = (int) ((float) screen1 / videoProportion);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            float ratio = Math.max((float) heightPixels / (float) frameY, (float) frameY / (float) heightPixels);
            pipRenderer.setScaleY(ratio);
            pipRenderer.setLayoutParams(layoutParams);
        } else if (videoProportion < screenProportion) {//螢幕比例較大
            frameX = (int) (videoProportion * (float) screen1);
            frameY = screen1;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            float ratio = Math.max((float) widthPixels / (float) frameX, (float) frameX / (float) widthPixels);
            pipRenderer.setScaleX(ratio);
            pipRenderer.setLayoutParams(layoutParams);
        } else {//比例相同,do nothing
//            pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            frameX = displaySize.x;
            frameY = displaySize.y;
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setRotation(int rotation) {
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels_call = dm.widthPixels;
        heightPixels_call = dm.heightPixels;


        vmRotation = rotation;
        if (android.provider.Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            switch (rotation) {
                case 0:
                case 2:
                    // do nothing
                    resetSpeedDialogView(rotation);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
                    changeSizeVertical(vmX, vmY, realw, realh, pipRenderer);
                    break;
                case 1:
                    resetSpeedDialogView(rotation);
                    Log.d(TAG, "realHeight >>>>> " + realh + " , realWidth >>>>>> " + realw);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Brahma PORTRAIT
                    changeSizeHorizontal(vmY, vmX, realh, realw, pipRenderer);
                    break;
                case 3:
                    resetSpeedDialogView(rotation);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //Brahma PORTRAIT
                    changeSizeHorizontal(vmY, vmX, realh, realw, pipRenderer);
                    break;
                default:
                    Log.d(TAG, "setRotation >>>>> default");
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
                    changeSizeVertical(vmX, vmY, realw, realh, pipRenderer);
                    break;
            }
        } else {
            vmRotation = 0;
        }
    }

    private void resetSpeedDialogView(int rotation) {
        if (oldRotation != newRotation) {
            LayoutParams mParams = new LayoutParams();
            mParams.x = 0;
            if (rotation == 0 || rotation == 2) {
                mParams.y = heightPixels_call / 3;
            } else {
                mParams.y = widthPixels_call / 3;
            }

            mParams.format = PixelFormat.RGBA_8888;
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            mParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            mParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mParams.width, mParams.height);
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.setMargins(0, mParams.y, 0, 0);

            speedDialView.setLayoutParams(params);
        }
        oldRotation = newRotation;
        newRotation = rotation;

    }

    private void changeSizeVertical(int vm_x, int vm_y, int my_x, int my_y, SurfaceViewRenderer pipRenderer) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        float vm_proportion = (float) vm_x / (float) vm_y;
        float my_proportion = (float) my_x / (float) my_y;

        if (vm_proportion > my_proportion) {
            int CHANGE_Y = (int) ((float) my_x / vm_proportion);
            float ratio2 = Math.max((float) my_y / (float) CHANGE_Y, (float) CHANGE_Y / (float) my_y);
            pipRenderer.setScaleY(ratio2);
            pipRenderer.setScaleX(1.0f);
        } else if (vm_proportion < my_proportion) {
            int CHANGE_X = (int) (vm_proportion * (float) my_y);
            float ratio2 = Math.max((float) CHANGE_X / (float) my_x, (float) my_x / (float) CHANGE_X);
            pipRenderer.setScaleY(ratio2);
            pipRenderer.setScaleX(1.0f);
        } else {
            pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            frameX = my_x;
            frameY = my_y;
            pipRenderer.setScaleX(1.0f);
            pipRenderer.setScaleY(1.0f);
        }
        pipRenderer.setLayoutParams(layoutParams);
    }

    private void changeSizeHorizontal(int a, int b, int c, int d, SurfaceViewRenderer pipRenderer) {
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        float vm_proportion = (float) a / (float) b;
        float my_proportion = (float) c / (float) d;

        if (vm_proportion > my_proportion) {
            int CHANGE_Y = (int) ((float) c / vm_proportion);
            float ratio2 = Math.max((float) d / (float) CHANGE_Y, (float) CHANGE_Y / (float) d);
            pipRenderer.setScaleX(ratio2);
            pipRenderer.setScaleY(1.0f);
        } else if (vm_proportion < my_proportion) {
            int CHANGE_X = (int) (vm_proportion * (float) d);
            float ratio2 = Math.max((float) CHANGE_X / (float) c, (float) c / (float) CHANGE_X);
            pipRenderer.setScaleX(ratio2);
            pipRenderer.setScaleY(1.0f);
        } else {
            pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            frameX = a;
            frameY = b;
            pipRenderer.setScaleX(1.0f);
            pipRenderer.setScaleY(1.0f);

        }
        pipRenderer.setLayoutParams(layoutParams);
    }

    @Override
    protected void onDisconnectAndExit() {
        Log.d(TAG, "onDisconnectAndExit");
        if (rotationHandler != null)
            rotationHandler.cleanupRotationUpdates();
        if (pcObserver != null)
            pcObserver.quit();
    }

    private void handleKbShowStatus(BRAHMAProtocol.Response message) {
        Log.d(TAG, "handleKbShowStatus");
        int status = kbHandler.handleKbShowStatus(message);
        Log.d(TAG, "WEBhandleKbShowStatusRTC status:" + status);
        try {
            if (status == 1) {
                //Show keyboard
                keyboard_shown = true;
                inputText.requestFocus();
                inputText.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.showSoftInput(inputText, InputMethodManager.SHOW_FORCED);
            } else if (status == 0) {
                //Hide keyboard
                keyboard_shown = false;
                hideSoftKeyboard();
                hideNavigationBar();
            }
        } catch (NullPointerException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    private void handleScreenInfo(BRAHMAProtocol.Response msg) {
        touchHandler.handleScreenInfoResponse(msg);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //first time
        if (hideKeyboard && device_type.equals("tablet") && null != this.getCurrentFocus()) {
            Log.i(TAG, "onTouchEvent()");
            Log.d(TAG, "getCurrentFocus():" + this.getCurrentFocus());
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            hideKeyboard = false;
            assert mInputMethodManager != null;
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return touchHandler.onTouchEvent(event);
    }

    void hideSoftKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
//            focused.clearFocus();
        }
    }

    // intercept KeyEvent before it is dispatched to the window
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //return keyHandler.tryConsume(event) || super.dispatchKeyEvent(event);
        int keyaction = event.getAction();
        Log.d("KeyEvents", "dispatchKeyEvent keyaction:" + keyaction);
        if (keyaction == KeyEvent.ACTION_DOWN) {
            Log.d("KeyEvents", "keyboard KEYCODE: " + String.valueOf(event.getKeyCode()));
            Log.d("KeyEvents", "Edittext String: " + inputText.getText().toString());

            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                Log.d("KeyEvents", "KeyEvent.KEYCODE_ENTER");
            	/*
	            Message msg = new Message();
				msg.what = MSG_ID_VKB_SEND_KEYCODE;
				msg.obj = KeyEvent.KEYCODE_ENTER;//KeyEvent.KEYCODE_CLEAR;
				mHandler.sendMessage(msg);
            	*/
                return super.dispatchKeyEvent(event);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                //就算TEXT已經刪完字元，還可以繼續送刪除鍵。
                Log.d("KeyEvents", "dispatchKeyEvent >> KeyEvent.KEYCODE_DEL");
                Message msg = new Message();
                msg.what = MSG_ID_VKB_SEND_KEYCODE;
                msg.obj = KeyEvent.KEYCODE_DEL;
                mHandler.sendMessage(msg);
//                inputText.setText("");
                return super.dispatchKeyEvent(event);
            }
            /*
            else if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            	//hideSoftKeyboard();
	            Message msg = new Message();
				msg.what = MSG_ID_VKB_SEND_KEYCODE;
				msg.obj = KeyEvent.KEYCODE_BACK;
				mHandler.sendMessage(msg);
				Log.d("TEST", "KeyEvent.KEYCODE_BACK");
				return true;
            }*/
        } else if (keyaction == KeyEvent.ACTION_UP && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)) {
            //handle volume event, do nothing
            Log.d("KeyEvents", "KeyEvent.ACTION_UP && KEYCODE_VOLUME_DOWN || KEYCODE_VOLUME_UP ");
            return true;

        }
        return keyHandler.tryConsume(event) || super.dispatchKeyEvent(event);
    }

    protected void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode:" + keyCode);
        Log.d(TAG, "event:" + event);
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            Log.d(TAG, "KEYCODE_BACK");
            if (!onOpen) {
                finish();
            }
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.d(TAG, "KEYCODE_MENU");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.d(TAG, "KEYCODE_HOME");
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

//    public void resetFloatingView(int newRotation) {
//        Log.d("Rotation", "resetFloatingView");
//        mFloatingView.show(newRotation);
//    }

    public void requestAudioFocus(AudioManager.OnAudioFocusChangeListener focusChangeListener, int streamType, int audioFocusGain) {
        int r;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            r = audioMgr.requestAudioFocus(
                    new AudioFocusRequest.Builder(audioFocusGain)
                            .setAudioAttributes(
                                    new AudioAttributes.Builder()
                                            .setLegacyStreamType(streamType)
                                            .build())
                            .setOnAudioFocusChangeListener(focusChangeListener)
                            .build());
        } else {
            r = audioMgr.requestAudioFocus(focusChangeListener, streamType, audioFocusGain);
        }
        Log.d(TAG, "r >>>>" + r);

    }

    public void abandonAudioFocus(AudioManager.OnAudioFocusChangeListener focusChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioMgr.abandonAudioFocusRequest(
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(focusChangeListener)
                            .build());
        } else {
            audioMgr.abandonAudioFocus(focusChangeListener);
        }
        //release bluetooth
        audioMgr.setBluetoothScoOn(false);
        audioMgr.stopBluetoothSco();
    }

    public boolean screenIsOpenRotate() {
        int gravity = 0;
        try {
            gravity = Settings.System.getInt(callActivityContext.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
        return gravity == 1;
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        String orientationLabel = orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape";
        KBheight = (int) ((height + SafeInsetTop) * resolution_ratio);
        Log.i(TAG, "onKeyboardHeightChanged in pixels: " + height + " " + orientationLabel);
        Log.i(TAG, "onKeyboardHeightChanged KBheight: " + KBheight);

        if (KBheight < 0)
            KBheight = 0;

        Message msg = new Message();
        msg.what = MSG_ID_VKB_INIT_HEIGHT;
        msg.obj = KBheight;
        mHandler.sendMessage(msg);
    }

    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     */
    // TODO(henrika): add support for BLUETOOTH as well.
    public enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE,
        BLUEBOOTH_HEADSET,
    }

    private class TimeCount extends CountDownTimer {
        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onTick(long millisUntilFinished) {
            //Log.d("PingSend","onTick frame count:"+frameCount);
        }

        @Override
        public void onFinish() {
            //Log.d(TAG,"onFinish frame count:"+frameCount);
            fps = (int) frameCount / 5;
            frameCount = 0;
            //Log.d("PingSend","onFinish frame count:"+fps);
        }
    }

    //    public static class ProxyVideoSink implements VideoSink {
//        private VideoSink mTarget;
//        @Override
//        synchronized public void onFrame(VideoFrame frame) {
//            if (mTarget == null) {
//                Log.d(TAG, "Dropping frame in proxy because target is null.");
//                return;
//            }
//            mTarget.onFrame(frame);
//            frame
//            Log.d(TAG, "frame width:" + frame.getRotatedWidth() + " , height:" + frame.getRotatedHeight());
//        }
//        synchronized void setTarget(VideoSink target) {
//            this.mTarget = target;
//        }
//    }
    private class ProxyRenderer<T extends VideoRenderer.Callbacks & VideoSink>
            implements VideoRenderer.Callbacks, VideoSink {
        private T target;

        @Override
        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }
            target.renderFrame(frame);
//            Log.d(TAG, "frame width:" + frame.width + " , height:" + frame.height);
            frameCount++;
        }

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }
            Log.d(TAG, "frame width >>>>>>");
            target.onFrame(frame);
        }

        synchronized void setTarget(T target) {
            Log.d(TAG, "setTarget");
            this.target = target;
        }
    }

    public class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LifeCycle", "HeadsetReceiver!!!");
            String action = intent.getAction();
            //耳機插拔觸發廣播
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    setAudioDevice(AudioDevice.WIRED_HEADSET);
                } else if (state == 0) {
                    if (!audioMgr.isBluetoothA2dpOn())//沒有接上耳機可是有用藍芽
                        setAudioDevice(AudioDevice.SPEAKER_PHONE);
                }
            }
        }
    }

    public class BluetoothConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LifeCycle", "BluetoothConnectionReceiver!!!");
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {        //蓝牙连接状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state == BluetoothAdapter.STATE_CONNECTED) {
                    setAudioDevice(AudioDevice.BLUEBOOTH_HEADSET);
                } else {
                    //|| state == BluetoothAdapter.STATE_DISCONNECTED
                    setAudioDevice(AudioDevice.SPEAKER_PHONE);
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {    //本地蓝牙打开或关闭
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    setAudioDevice(AudioDevice.SPEAKER_PHONE);
                } else {
                    setAudioDevice(AudioDevice.BLUEBOOTH_HEADSET);
                }
            }

        }
    }

    public class InnerRecevier extends BroadcastReceiver {
        final static String SYSTEM_DIALOG_REASON_KEY = "reason";
        final static String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final static String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        Log.i(TAG, "Home鍵被監聽");
                        hideSoftKeyboard();
                        disconnectAndExit();
                        disconnect();

                        Intent intent3 = new Intent();
                        intent3.setClass(CallActivity.this, BrahmaMainActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        OpenAppMode = false;
                        startActivity(intent3);
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        Log.i(TAG, "多工鍵被監聽");
                        disconnectAndExit();
                        disconnect();

                        Intent intent3 = new Intent();
                        intent3.setClass(CallActivity.this, BrahmaMainActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        OpenAppMode = false;
                        startActivity(intent3);
                    }
                }
            }
        }
    }
}
