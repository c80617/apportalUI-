package brahma.vmi.covid2019.apprtc;

/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
/**
 * @file CallActivity
 * @brief 進行webrtc連接與顯示畫面之activity
 * <p>
 * 建立webrtc連線並且顯示視訊畫面
 * 處理使用者不同的sensor事件
 * 進行螢幕的解析度調整
 * 輸入法處理
 * 轉向與觸控之處理
 * <p>
 * @author YiWen Li
 * @date 2019/07/12
 **/

import android.annotation.TargetApi;
import android.app.AlertDialog;
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
import android.graphics.Color;
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
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import appspot.apprtc.VideoStreamsView;
import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.activities.AppRTCActivity;
import brahma.vmi.covid2019.biometriclib.BiometricPromptManager;
import brahma.vmi.covid2019.client.ConfigHandler;
import brahma.vmi.covid2019.client.KeyHandler;
import brahma.vmi.covid2019.client.RotationHandler;
import brahma.vmi.covid2019.client.TouchHandler;
import brahma.vmi.covid2019.client.VirtualKeyboardHandler;
import brahma.vmi.covid2019.client.VolumeChangeObserver;
import brahma.vmi.covid2019.database.Item;
import brahma.vmi.covid2019.database.ItemBio;
import brahma.vmi.covid2019.database.Login;
import brahma.vmi.covid2019.expandmenu.widget.FloatingView;
import brahma.vmi.covid2019.keyboard.KeyboardHeightObserver;
import brahma.vmi.covid2019.keyboard.KeyboardHeightProvider;
import brahma.vmi.covid2019.netspeed.SpeedCalculationService;
import brahma.vmi.covid2019.netspeed.WindowUtil;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.wcitui.SlidingTab;
import brahma.vmi.covid2019.wcitui.tab.AudioDialog;
import brahma.vmi.covid2019.widgets.MyEditText;

import static android.media.AudioManager.ACTION_HEADSET_PLUG;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.vm_platform;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.vm_version;
import static brahma.vmi.covid2019.client.KeyHandler.isKeyEvent;
import static brahma.vmi.covid2019.client.PingLatency.fps;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.device_type;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.heightPixels;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.login_type;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalPassword;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalPort;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalUsername;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.resolution_ratio;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.widthPixels;

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
    // Get the SurfaceView layout parameters
    public static int frameX = -1;
    public static int frameY = -1;
    public static Handler handler;
    public static Context callActivityContext;
    public static int KBheight = 0;
    //audio
    static MediaStream ms = null;
    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<VideoRenderer.Callbacks>();
    public float videoProportion = -1;
    public float screenProportion = -1;
    protected IntentFilter intentFilter2;
    protected HeadsetReceiver myBroadcastReceiver2;
    protected IntentFilter intentFilter3;
    protected BluetoothConnectionReceiver bluetoothReceiver;
    protected AppstreamingReceiver appstreamingReceiver;
    protected IntentFilter intentFilter4;
    int frameCount = 0;
    int beforeL;
    int NETREQUEST_CODE = 55;
    PeerConnectionFactory.Options options = null;
    InnerRecevier innerReceiver = new InnerRecevier();
    Handler mHanlder2;
    AudioFocusRequest mFocusRequest;
    AudioAttributes mPlaybackAttributes;
    AudioDialog audiodialog;
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
                    //rotationHandler.onOrientationChanged(225);
                    //rotationHandler.onOrientationChanged(45);
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
    private SharedPreferences settings;
    private MediaConstraints sdpMediaConstraints;
    private SDPObserver sdpObserver;
    private VideoStreamsView vsv = null;
    private PCObserver pcObserver;
    private TouchHandler touchHandler;
    private RotationHandler rotationHandler;
    private AudioManager audioMgr;
    private KeyHandler keyHandler;
    private ConfigHandler configHandler;
    //WebRTC63
    private PeerConnectionFactory factory;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = null;
    private VolumeChangeObserver mVolumeChangeObserver;
    private boolean hideKeyboard = true;
    //懸浮視窗提供home,back,multiApp
    private FloatingView mFloatingView;
    private AlertDialog touchID_dialog;
    private BiometricPromptManager mManager;
    private FloatingView.FabListener fablistener = new FloatingView.FabListener() {
        @Override
        public void click(int i) {
            switch (i) {
                case FloatingView.BACK:
                    Message msg = new Message();
                    msg.what = MSG_ID_VKB_SEND_KEYCODE;
                    msg.obj = KeyEvent.KEYCODE_BACK;
                    mHandler.sendMessage(msg);
                    break;
                case FloatingView.HOME:
                    KeyEvent eventHome = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME);
                    keyHandler.tryConsume(eventHome);
                    break;
                case FloatingView.MENU:
                    KeyEvent eventMenu = new KeyEvent(KeyEvent.ACTION_UP, 299);
                    keyHandler.tryConsume(eventMenu);
                    break;
                case FloatingView.MULTIAPP:
                    KeyEvent eventMultiApp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_APP_SWITCH);
                    keyHandler.tryConsume(eventMultiApp);
                    break;
                default:
                    break;
            }
        }
    };
    private RendererCommon.RendererEvents rendererEvents = new RendererCommon.RendererEvents() {
        @Override
        public void onFirstFrameRendered() {
        }

        @Override
        public void onFrameResolutionChanged(int width, int height, int orientation) {
            setAspectRatio(width, height, orientation);
        }
    };
    private int realWidth;
    private int realHeight;
    private View bioView;
    private int navigation_bar_height = 0;
    private int status_bar_height = 0;

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    public static MediaStream getMediaStream() {
        return ms;
    }

    public void setMediaStream(MediaStream ms) {
        this.ms = ms;
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
                logAndToast("ICE disconnected");
                iceConnected = false;
                disconnect();
                //close websocket
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

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    Log.d(TAG, "mute!");
                    audiodialog.show(msg.what);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            audiodialog.dismiss();
                        }
                    }, 500);
                } else {
                    Log.d(TAG, "Volume:" + msg.what);
                    audiodialog.show(msg.what);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            audiodialog.dismiss();
                        }
                    }, 500);
                }
            }
        };

        // Get info passed to Intent
        final Intent intent = getIntent();
        pkgName = intent.getStringExtra("pkgName");
        getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE); //Brahma limit screenshot
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);//The Brahma screen does not close
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

        settings = getSharedPreferences("user", 0);
        KBheight = settings.getInt("KbHeight", 0);

        keyboard_shown = false;
        iceConnected = false;
        signalingParameters = null;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkDrawOverlayPermission()) {
                init();
            }
        }
        hideNavigationBar();

        //全屏显示
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        getWindow().setAttributes(lp);

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

        intentFilter4 = new IntentFilter();
        intentFilter4.addAction("APPSTREAMING_MODE");
        appstreamingReceiver = new AppstreamingReceiver();
        registerReceiver(appstreamingReceiver, intentFilter4);

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

        itemBio = new ItemBio(CallActivity.this);
        if (itemBio.getCount() == 0) {
            itemBio.sample();
        }
        if (itemBio.getCount_login() == 0) {
            itemBio.sample2();
        }

        Login item2 = new Login(1, "true", normalUsername, normalPassword, normalIP, normalPort,"");
        boolean sqlupdate2 = itemBio.update(item2);

        //setting bioView UI
        bioView = LayoutInflater.from(CallActivity.this).inflate(R.layout.dialog_using_bio, null);
        TextView oldUsername = (TextView) bioView.findViewById(R.id.oldUsername);
        TextView newUsername = (TextView) bioView.findViewById(R.id.newUsername);
        if (itemBio.get(1).getbioUsername() != null) {
            oldUsername.append(itemBio.get(1).getbioUsername());
        }
        if (normalUsername != null) {
            newUsername.append(normalUsername);
        }
        //create dialog
        AlertDialog.Builder touchIDbuilder = new AlertDialog.Builder(CallActivity.this);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, NETREQUEST_CODE);
                return false;
            }
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
        int statusBarHeight = rectangle.top;
        Log.d("YiWen", "statusBarHeight >>>>> " + statusBarHeight);
        return statusBarHeight;
    }

    @Override
    protected void connectToRoom() {
        Log.d("LifeCycle", "Callactivity connectToRoom!");

        hideKeyboard = true;
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        if (this.getFlagRemoteLandscape() == true) {
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
            navigation_bar_height = resources.getDimensionPixelSize(resourceId);
            Log.d(TAG, "navigation_bar_height:" + navigation_bar_height);
        }

        int resourceId2 = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId2 > 0) {
            status_bar_height = getApplicationContext().getResources().getDimensionPixelSize(resourceId2);
            Log.d(TAG, "status_bar_height:" + status_bar_height);
        }

        EglBase rootEglBase = EglBase.create();
        pipRenderer.init(rootEglBase.getEglBaseContext(), rendererEvents);
        pipRenderer.getHolder().setFixedSize(displaySize.x, displaySize.y);
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        pipRenderer.setZOrderMediaOverlay(false);
        pipRenderer.setEnableHardwareScaler(true);

//        String saveRemoteVideoToFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/vvvvv_video.y4m";
//        try {
//            videoFileRenderer = new VideoFileRenderer(saveRemoteVideoToFile, 1280, 720, rootEglBase.getEglBaseContext());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        remoteRenderers.add(videoFileRenderer);

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

        inputText.SetOnBackPressedListener((keyCode, event) -> {
            // TODO Auto-generated method stub
            Log.d("KeyEvents", "KeyEvent.KEYCODE_ENTER: " + KeyEvent.KEYCODE_ENTER + ", keyCode: " + keyCode);
            Message msg = new Message();
            msg.what = MSG_ID_VKB_SEND_KEYCODE;
            msg.obj = KeyEvent.KEYCODE_BACK;
            mHandler.sendMessage(msg);
        });

        touchHandler = new TouchHandler(this, displaySize, performanceAdapter);
        rotationHandler = new RotationHandler(this);
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        keyHandler = new KeyHandler(this, audioMgr);
        configHandler = new ConfigHandler(this);
        kbHandler = new VirtualKeyboardHandler(this);

        audiodialog = new AudioDialog(this, audioMgr.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));

        //audio manager request focuse
        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
//                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                    Log.d(TAG, "AUDIOFOCUS_LOSS");
//                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                    Log.d(TAG, "AUDIOFOCUS_GAIN");
//                }
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
        factory = new PeerConnectionFactory(options);

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

        mFloatingView = new FloatingView(getApplicationContext());
        mFloatingView.setFabListener(fablistener);
        mFloatingView.show(vmRotation);

        hideNavigationBar();
        super.connectToRoom();
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

//    @TargetApi(21)
//    private void startScreenCapture() {
//        MediaProjectionManager mediaProjectionManager =
//                (MediaProjectionManager) getApplication().getSystemService(
//                        Context.MEDIA_PROJECTION_SERVICE);
//        startActivityForResult(
//                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
//            return;
//        mediaProjectionPermissionResultCode = resultCode;
//        mediaProjectionPermissionResultData = data;
//        startCall();
//    }

//    private boolean useCamera2() {
//        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
//    }
//
//    private boolean captureToTexture() {
//        return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
//    }
//
//    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
//        final String[] deviceNames = enumerator.getDeviceNames();
//
//        // First, try to find front facing camera
//        Logging.d(TAG, "Looking for front facing cameras.");
//        for (String deviceName : deviceNames) {
//            if (enumerator.isFrontFacing(deviceName)) {
//                Logging.d(TAG, "Creating front facing camera capturer.");
//                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//
//        // Front facing camera not found, try something else
//        Logging.d(TAG, "Looking for other cameras.");
//        for (String deviceName : deviceNames) {
//            if (!enumerator.isFrontFacing(deviceName)) {
//                Logging.d(TAG, "Creating other camera capturer.");
//                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//        return null;
//    }


    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        //Fab for back...
        mFloatingView.hide();

        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
//        if (peerConnectionClient != null && !screencaptureEnabled) {
//            peerConnectionClient.stopVideoSource();
//        }
        //cpuMonitor.pause();
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
//        if (peerConnectionClient != null && !screencaptureEnabled) {
//            peerConnectionClient.startVideoSource();
//        }
        //cpuMonitor.resume();
    }

    @Override
    protected void onDestroy() {
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
        unregisterReceiver(appstreamingReceiver);
        super.onDestroy();
        keyboardHeightProvider.close();
        hideSoftKeyboard();

    }

//    private void startCall() {
//        //first time
//        if (hideKeyboard && device_type.equals("tablet") && null != this.getCurrentFocus()) {
//            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            hideKeyboard = false;
//            mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
//        }
//
////        if (appRtcClient == null) {
////            Log.e(TAG, "AppRTC client is not allocated for a call.");
////            return;
////        }
////        callStartedTimeMs = System.currentTimeMillis();
////
////        // Start room connection.
////        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl));
////        appRtcClient.connectToRoom(roomConnectionParameters);
//    }

    // Disconnect from remote resources, dispose of local resources, and exit.
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
        finish();
    }

//    private void disconnectWithErrorMessage(final String errorMessage) {
//        if (commandLineRun || !activityRunning) {
//            Log.e(TAG, "Critical error: " + errorMessage);
//            disconnect();
//        } else {
//            new AlertDialog.Builder(this)
//                    .setTitle(getText(R.string.channel_error_title))
//                    .setMessage(errorMessage)
//                    .setCancelable(false)
//                    .setNeutralButton(R.string.yes,
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                    disconnect();
//                                }
//                            })
//                    .create()
//                    .show();
//        }
//    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        logToast.show();
    }

//    private void reportError(final String description) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (!isError) {
//                    isError = true;
//                    disconnectWithErrorMessage(description);
//                }
//            }
//        });
//    }

    public void setAspectRatio(int width, int height, int orientation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.videoFrameLayout);
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

//    private VideoCapturer createVideoCapturer() {
//        VideoCapturer videoCapturer = null;
//        String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
//        if (videoFileAsCamera != null) {
//            try {
//                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
//            } catch (IOException e) {
//                reportError("Failed to open video file for emulated camera");
//                return null;
//            }
//        } else if (screencaptureEnabled) {
//            return createScreenCapturer();
//        } else if (useCamera2()) {
//            if (!captureToTexture()) {
//                reportError(getString(R.string.camera2_texture_only_error));
//                return null;
//            }
//
//            Logging.d(TAG, "Creating capturer using camera2 API.");
//            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
//        } else {
//            Logging.d(TAG, "Creating capturer using camera1 API.");
//            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
//        }
//        if (videoCapturer == null) {
//            reportError("Failed to open camera");
//            return null;
//        }
//        return videoCapturer;
//    }

    /////////////////////////////////////////////////////////////////////
    // Audio set
    /////////////////////////////////////////////////////////////////////

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
        if (unistring.length() > 4 || !unistring.startsWith("3")) return false;
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
        if (vsv != null)
            vsv.setBackgroundColor(Color.DKGRAY); // if it isn't already set, make the background color dark gray
        super.startProgressDialog();
    }

    @Override
    public void onPause() {
        Log.d("LifeCycle", "Callactivity onPause!!!");
        if (!checkOpenCamera()) {
            if (vsv != null) vsv.onPause();
        }
//        stopService(new Intent(this, NetMService.class));
        stopService(new Intent(CallActivity.this, SpeedCalculationService.class));
        mVolumeChangeObserver.unregisterReceiver();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
        hideSoftKeyboard();
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d("LifeCycle", "Callactivity onResume!!!");
        hideNavigationBar();
        resetFloatingView(vmRotation);
        if (!checkOpenCamera()) {
            if (vsv != null) vsv.onResume();
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

//        //Ian; NetMService
//        if(roleData.getRole().equals("developer")) {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//                Log.d("IAN", roleData.getRole());
//            Intent intentNetMService = new Intent(getApplicationContext(), NetMService.class); //getApp
//            startService(intentNetMService);
        //netM
        startService(new Intent(CallActivity.this, SpeedCalculationService.class));
        hideSoftKeyboard();
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

    // MessageHandler interface method
    // Called when a message is sent from the server, and the SessionService doesn't consume it
    public boolean onMessage(BRAHMAProtocol.Response data) {
        switch (data.getType()) {
            case APPS:
                if (data.hasApps() && data.getApps().getType() == BRAHMAProtocol.AppsResponse.AppsResponseType.EXIT) {
                    // we have exited a remote app; exit back to our parent activity and act accordingly
                    disconnectAndExit();
                }
                break;
            case SCREENINFO:
                handleScreenInfo(data);
                setBioSetting();
                setSize(data);
                break;
            case VKeyboardInfo:
            	/*
            	inputText.requestFocus();
            	inputText.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(inputText, InputMethodManager.SHOW_IMPLICIT);
				Message msg = new Message();
				msg.what = MSG_ID_VKB_INIT_HEIGHT;
				msg.obj = 100;
				mHandler.sendMessage(msg);
				*/
                handleKbShowStatus(data);
                //handleVKeyboardInfoResponse(data);
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
                    if (type.equals("candidate")) {
                        IceCandidate candidate = new IceCandidate(
                                (String) json.get("id"),
                                json.getInt("label"),
                                (String) json.get("candidate"));
                        getPCObserver().addIceCandidate(candidate);
                    } else if (type.equals("answer") || type.equals("offer")) {
                        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type),
                                (String) json.get("sdp"));

                        Log.d(TAG, "WEBRTC sdp:" + sdp.description);
                        getPCObserver().getPC().setRemoteDescription(sdpObserver, sdp);
                    } else if (type.equals("bye")) {
                        logAndToast(R.string.appRTC_toast_clientHandler_finish);
                        disconnectAndExit();
                    } else {
                        throw new RuntimeException("Unexpected message: " + data);
                    }
                } catch (JSONException e) {
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
        mManager = BiometricPromptManager.from(this);
        if (mManager.isBiometricPromptEnable()) {
            if (!itemBio.get(1).getbioUsername().equals(normalUsername) && login_type != "qrcode")
                touchID_dialog.show();
            Log.d("isUsingBio", "成功進入生物辨識setting");
        } else {
            Log.d("isUsingBio", "無法使用生物辨識");
        }
    }

    private void setSize(BRAHMAProtocol.Response data) {
        vmX = data.getScreenInfo().getX();
        vmY = data.getScreenInfo().getY();

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        Log.d(TAG, "setFrameSize Got the ServerInfo: vmX=" + vmX + " ; vmY=" + vmY);
        Log.d(TAG, "setFrameSize Got the ServerInfo: displaySize x=" + displaySize.x + " ; displaySize y=" + displaySize.y);
        vmX = displaySize.x;
        vmY = displaySize.y;
        int videoWidth = displaySize.x;
        int videoHeight = displaySize.y;

//        int videoWidth = data.getScreenInfo().getX();
//        int videoHeight = data.getScreenInfo().getY();
        videoProportion = (float) videoWidth / (float) videoHeight;

        // Get the width of the screen
        int screenWidth = 0;
        int screenHeight = 0;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        Log.d(TAG, "手機銀幕大小為 " + metrics.widthPixels + " X " + metrics.heightPixels);

        if (displaySize.y >= displaySize.x) {
            screenWidth = displaySize.x;
            screenHeight = displaySize.y;
            realWidth = metrics.widthPixels;
            realHeight = metrics.heightPixels;
        } else {
            screenWidth = displaySize.y;
            screenHeight = displaySize.x;
            realWidth = metrics.heightPixels;
            realHeight = metrics.widthPixels;
        }

        screenProportion = (float) screenWidth / (float) screenHeight;
        Log.d(TAG, "videoProportion = " + videoProportion + " ; screenProportion = " + screenProportion);

        if (videoProportion > screenProportion) {//frame比例較大
//            Log.d(TAG, "videoProportion >> screenProportion");
            frameX = screenWidth;
            frameY = (int) ((float) screenWidth / videoProportion);
            Log.d(TAG, "lp.width:" + frameX + ", lp.height:" + frameY);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);


            float ratio = Math.max((float) heightPixels / (float) frameY, (float) frameY / (float) heightPixels);
            Log.d(TAG, "setSize ratio:" + ratio);

            pipRenderer.setScaleY(ratio);
            pipRenderer.setLayoutParams(layoutParams);

        } else if (videoProportion < screenProportion) {//螢幕比例較大
//            Log.d(TAG, "videoProportion << screenProportion");
            frameX = (int) (videoProportion * (float) screenHeight);
            frameY = screenHeight;
            Log.d(TAG, "lp.width:" + frameX + ", lp.height:" + frameY);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            float ratio = Math.max((float) widthPixels / (float) frameX, (float) frameX / (float) widthPixels);
            Log.d(TAG, "setSize ratio:" + ratio);
            pipRenderer.setScaleX(ratio);
            pipRenderer.setLayoutParams(layoutParams);
        } else {//比例相同,do nothing
//            Log.d(TAG, "videoProportion == screenProportion");
            pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            frameX = displaySize.x;
            frameY = displaySize.y;


        }
    }

    public void setRotation(int rotation) {

        vmRotation = rotation;
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        RelativeLayout.LayoutParams layoutParams;
        //辨別vm_version
        String ss = vm_version.replace(".", "");

        if (vm_platform != null) {
            if (android.provider.Settings.System.getInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
//                Log.d(TAG, "Rotation ON");

                if (true) {
                    //test
                    switch (rotation) {
                        case 0:
                        case 2:
                            // do nothing
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
                            changeSizeVertical(vmX, vmY, realWidth, realHeight, pipRenderer);
                            break;
                        case 1:
                            Log.d(TAG, "realHeight >>>>> " + realHeight + " , realWidth >>>>>> " + realWidth);
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Brahma PORTRAIT
                            changeSizeHorizontal(vmY, vmX, realHeight, realWidth, pipRenderer);
                            break;
                        case 3:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //Brahma PORTRAIT
                            changeSizeHorizontal(vmY, vmX, realHeight, realWidth, pipRenderer);
                            break;
                        default:
                            Log.d(TAG, "setRotation >>>>> default");
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
                            changeSizeVertical(vmX, vmY, realWidth, realHeight, pipRenderer);
                            break;
                    }
                } else {
                    Log.d(TAG, "Rotation >>>>> " + rotation);
                    switch (rotation) {
                        case 0:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
                            pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT, ScalingType.SCALE_ASPECT_FIT);
                            layoutParams = new RelativeLayout.LayoutParams(frameX, frameY);
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                            pipRenderer.setLayoutParams(layoutParams);
                            break;
                        case 1:
                            Log.d(TAG, "realHeight >>>>> " + realHeight + " , realWidth >>>>>> " + realWidth);
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Brahma PORTRAIT
                            pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT, ScalingType.SCALE_ASPECT_FIT);
                            layoutParams = new RelativeLayout.LayoutParams(realHeight, realWidth);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);//CENTER_IN_PARENT
                            pipRenderer.setLayoutParams(layoutParams);
                            break;
                        case 2:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT); //Brahma PORTRAIT
                            pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT, ScalingType.SCALE_ASPECT_FIT);
                            layoutParams = new RelativeLayout.LayoutParams(frameX, frameY);
                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                            pipRenderer.setLayoutParams(layoutParams);
                            break;
                        case 3:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); //Brahma PORTRAIT
                            pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT, ScalingType.SCALE_ASPECT_FIT);
                            layoutParams = new RelativeLayout.LayoutParams(realHeight, realWidth);
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);//CENTER_IN_PARENT
                            pipRenderer.setLayoutParams(layoutParams);
                            break;
                        default:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
                            break;
                    }
                }

            } else {
                vmRotation = 0;
//                Log.d(TAG, "Rotation OFF");
            }
        }
    }

    private void changeSizeVertical(int vm_x, int vm_y, int my_x, int my_y, SurfaceViewRenderer pipRenderer) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        float vm_proportion = (float) vm_x / (float) vm_y;
        float my_proportion = (float) my_x / (float) my_y;

        if (vm_proportion > my_proportion) {
            int CHANGE_X = my_x;
            int CHANGE_Y = (int) ((float) my_x / vm_proportion);
            float ratio2 = Math.max((float) my_y / (float) CHANGE_Y, (float) CHANGE_Y / (float) my_y);
            pipRenderer.setScaleY(ratio2);
            pipRenderer.setScaleX(1.0f);
        } else if (vm_proportion < my_proportion) {
            int CHANGE_X = (int) (vm_proportion * (float) my_y);
            int CHANGE_Y = my_y;
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

    private void changeSizeHorizontal(int vm_x, int vm_y, int my_x, int my_y, SurfaceViewRenderer pipRenderer) {
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        float vm_proportion = (float) vm_x / (float) vm_y;
        float my_proportion = (float) my_x / (float) my_y;

        if (vm_proportion > my_proportion) {
            int CHANGE_X = my_x;
            int CHANGE_Y = (int) ((float) my_x / vm_proportion);
            float ratio2 = Math.max((float) my_y / (float) CHANGE_Y, (float) CHANGE_Y / (float) my_y);
            pipRenderer.setScaleX(ratio2);
            pipRenderer.setScaleY(1.0f);
        } else if (vm_proportion < my_proportion) {
            int CHANGE_X = (int) (vm_proportion * (float) my_y);
            int CHANGE_Y = my_y;
            float ratio2 = Math.max((float) CHANGE_X / (float) my_x, (float) my_x / (float) CHANGE_X);
            pipRenderer.setScaleX(ratio2);
            pipRenderer.setScaleY(1.0f);
        } else {
            pipRenderer.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            frameX = vm_x;
            frameY = vm_y;
            pipRenderer.setScaleX(1.0f);
            pipRenderer.setScaleY(1.0f);

        }
        pipRenderer.setLayoutParams(layoutParams);
    }

    @Override
    protected void onDisconnectAndExit() {
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
                imm.showSoftInput(inputText, InputMethodManager.SHOW_FORCED);

//                Message msg = new Message();
//                msg.what = MSG_ID_VKB_INIT_HEIGHT;
//                if (KBheight > 0) {
//                    msg.obj = KBheight;
//                } else {
//                    msg.obj = 180;
//                }
//                mHandler.sendMessage(msg);
            } else if (status == 0) {
                //Hide keyboard
                keyboard_shown = false;
                hideSoftKeyboard();
                hideNavigationBar();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "handleKbShowStatus error:" + e.getStackTrace());
        }
    }

//    private void handleVKeyboardInfoResponse(BRAHMAProtocol.Response msg) {
//        kbHandler.handleVKeyboardInfoResponse(msg);
//    }

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
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return touchHandler.onTouchEvent(event);
    }

    void hideSoftKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void showNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode:" + keyCode);
        Log.d(TAG, "event:" + event);
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            Log.d(TAG, "KEYCODE_BACK");
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

    public boolean requestAudioFocus(AudioManager.OnAudioFocusChangeListener focusChangeListener,
                                     int streamType, int audioFocusGain) {
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
            //noinspection deprecation
            r = audioMgr.requestAudioFocus(focusChangeListener, streamType, audioFocusGain);
        }
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void abandonAudioFocus(AudioManager.OnAudioFocusChangeListener focusChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioMgr.abandonAudioFocusRequest(
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(focusChangeListener)
                            .build());
        } else {
            //noinspection deprecation
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
            e.printStackTrace();
        }
        if (gravity == 1) {
            return true;
        }
        return false;
    }

    public void resetFloatingView(int newRotation) {
        Log.d("Rotation", "resetFloatingView");
//        if(mFloatingView.isActivated())
//        mFloatingView.hide();
        mFloatingView.show(newRotation);
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        String orientationLabel = orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape";
        KBheight = (int) (height * resolution_ratio);
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
        public TimeCount(long millisInFuture, long countDownInterval) {
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
//            Log.d(TAG, "frame width:" + frame.width + " , height:" + frame.height);
            frameCount++;
            target.renderFrame(frame);
        }

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }
            target.onFrame(frame);
        }

        synchronized public void setTarget(T target) {
            this.target = target;
        }
    }

    public class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LifeCycle", "HeadsetReceiver!!!");
            String action = intent.getAction();
            switch (action) {
                //耳機插拔觸發廣播
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1) {
                        setAudioDevice(AudioDevice.WIRED_HEADSET);
                    } else if (state == 0) {
                        if (audioMgr.isBluetoothA2dpOn())//沒有接上耳機可是有用藍芽
                            break;
                        else
                            setAudioDevice(AudioDevice.SPEAKER_PHONE);
                    }
                    break;
                default:
                    break;
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
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

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
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        Log.i(TAG, "多工鍵被監聽");
                        disconnectAndExit();
                        disconnect();
                    }
                }
            }
        }
    }

    public class AppstreamingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Callactivity AppstreamingReceiver action:" + intent.getAction());
            Intent intent2 = new Intent(CallActivity.this, SlidingTab.class);
//            intent2.putExtra("cName", usernameView.getText().toString());
//            intent2.putExtra("connectionID", 1);
//            machine.setState(StateMachine.STATE.STARTED, R.string.appRTC_toast_brahmaAuthenticator_success); // STARTED -> AUTH
            startActivity(intent2);
        }
    }
}
