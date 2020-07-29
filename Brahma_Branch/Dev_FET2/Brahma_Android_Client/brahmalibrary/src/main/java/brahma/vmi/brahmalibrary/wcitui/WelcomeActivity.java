package brahma.vmi.brahmalibrary.wcitui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.FutureTask;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.database.ItemBio;
import brahma.vmi.brahmalibrary.database.Login;
import brahma.vmi.brahmalibrary.database.MyDBHelper;
import brahma.vmi.brahmalibrary.widgets.APKVersionCodeUtils;
import brahma.vmi.brahmalibrary.widgets.VersionAPI;

/**
 * @author YiWen Li
 * @file WelcomeActivity
 * @brief app啟動後的第一個activity
 * <p>
 * 包含對使用者要求必要的權限、
 * 檢查app版本是否為最新版、
 * 檢查網路狀態(目前暫時拔除)、
 * 傳遞notification intent訊息
 * @date 2019/07/10
 **/
public class WelcomeActivity extends Activity {

    private static final int GOTO_MAIN_ACTIVITY = 0;
    public static boolean isSelectFile = false;//用來控制是否要強制kill app process
    public static double Latitude = 24.7740655;
    public static double Longitude = 121.0440186;
    public static String oldVersion = "";
    String currentVersion = "";
    Context context;
    ProgressBar pb1;
    int progress = 0;
    boolean isPermissionReady = false;
    boolean isNetworkReady = true;//false;
    boolean isVerisonReady = true;//false;
    Intent intent = new Intent();
    SharedPreferences sharedPreferences2;
    Handler handler;
    TextView tv, appVersion;
    Button bt;
    ItemBio itemBio;
    FutureTask<Boolean> task;
    private String TAG = "WelcomeActivity";
    private android.app.AlertDialog newVersionDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GOTO_MAIN_ACTIVITY:
                    Log.d("FCM", "GOTO_MAIN_ACTIVITY");
//                    intent.setClass(WelcomeActivity.this, BrahmaMainActivity.class);
                    intent.setClass(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;

                default:
                    break;
            }
        }

    };
    private Runnable mutiThread = new Runnable() {
        public void run() {
            try {
                tv.setText(R.string.version_check);
                VersionAPI v = new VersionAPI();
                currentVersion = v.getPlayStoreAppVersion();
                if (currentVersion != null) {
                    int playVersion = Integer.parseInt(currentVersion.replaceAll("\\.", ""));
                    int userVersion = Integer.parseInt(oldVersion.replaceAll("\\.", ""));
                    Log.d(TAG, "playVersion:" + playVersion);
                    Log.d(TAG, "userVersion:" + userVersion);

                    if (playVersion <= userVersion) {
                        isVerisonReady = true;
                    } else {
                        isVerisonReady = false;
                        handler.sendEmptyMessage(0);
                    }
                }
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        pb1 = (ProgressBar) findViewById(R.id.progress_id);
        pb1.setVisibility(progress < 100 ? View.VISIBLE : View.GONE);
        pb1.setProgress(progress);
        tv = (TextView) findViewById(R.id.tv);
        bt = (Button) findViewById(R.id.bt);
        tv.setText(R.string.checking);
        context = this;
        sharedPreferences2 = getSharedPreferences("data2", MODE_PRIVATE);
        Log.d(TAG, "context.getFilesDir():" + context.getFilesDir());

        //Display App Version
        appVersion = (TextView) findViewById(R.id.appVersion);
        appVersion.append(APKVersionCodeUtils.getVerName(this));

        //Check Network
        isNetworkReady = true;//isConnected();

        //Check APP Version
        oldVersion = APKVersionCodeUtils.getVerName(this);
        newVersionDialog = alertDialog(getResources().getString(R.string.have_new_app_version), getResources().getString(R.string.update_app_or_not));

        MyDBHelper dbHelper = new MyDBHelper(this);
        itemBio = new ItemBio(dbHelper.getDatabase(this));

        if (itemBio.getCount() == 0) {
            itemBio.sample();
        }
        if (itemBio.getCount_login() == 0) {
            itemBio.sample2();
        }

        Intent intent2 = getIntent();
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
                if (key.equals("google.message_id")) {
                    Log.d(TAG, "google.message_id");
                }
            }
        }

        Log.d("FCM", "Welcome Activity >>>>>> intent action:" + intent2.getAction());
        boolean isNotification = false;
        String package_name = "";
        //1.從外面的通知進來的,看google.message_id
        //2.從裡面的通知進來的
        //3.url login,看intent action ：NOTIFICATION_ACTION

        if (intent2.getAction() != null) {
            if (getIntent().getExtras() != null) {
                for (String key : getIntent().getExtras().keySet()) {
                    Object value = getIntent().getExtras().get(key);
                    Log.d(TAG, "Key: " + key + " Value: " + value);
                    if (key.equals("google.message_id")) {
                        Log.d(TAG, "google.message_id");
                        isNotification = true;
                    }
                    if (key.equals("message")) {
                        try {
                            JSONObject jsonObj = new JSONObject(value.toString());
                            package_name = jsonObj.getString("package_name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (isNotification) {
                itemBio.update_package(1, package_name);
                itemBio.update_loginType(1, "notification");
                intent.setAction("NOTIFICATION_ACTION");
            } else if (intent2.getAction().equals("NOTIFICATION_ACTION")) {//我自己設定的action
                itemBio.update_loginType(1, "notification");
                intent.setAction("NOTIFICATION_ACTION");
            } else if (Intent.ACTION_VIEW.equals(intent2.getAction())) {
                intent.setAction("URL_LOGIN_ACTION");
                Uri intentData = intent2.getData();
                Log.d("FCM", "intentData >>>>> " + intentData.toString());
                String account = intentData.getQueryParameter("account");
                String pwd = intentData.getQueryParameter("pwd");
                String urlPackage = intentData.getQueryParameter("package");
                String ip = intentData.getQueryParameter("ip");
                String port = intentData.getQueryParameter("port");

                Login loginItem = new Login(1, "url", account, pwd, ip, port, urlPackage);
                itemBio.update(loginItem);
            } else {
                itemBio.update_loginType(1, "false");
                sharedPreferences2.edit().clear().apply();
            }
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        newVersionDialog.show();
                        break;
                    case 1:
//                        try {
//                            if (isNetworkReady && isPermissionReady && task.get()) {
                        if (isNetworkReady && isPermissionReady) {
                            tv.setText(R.string.welcome_connect_success);
                            mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 0);
                        } else {
                            pb1.setVisibility(View.GONE);
                            tv.setText(R.string.welcome_connect_failed);
                        }
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        } catch (ExecutionException e) {
//                            e.printStackTrace();
//                        }
                        break;
                }
            }
        };

        //Check Network
//        if (isNetworkReady) {
//            task = new FutureTask<>(new versionThread());
//            new Thread(task).start();
//            try {
//                isVerisonReady = task.get();
//                Log.d(TAG, "future task:" + task.get());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//            Log.d(TAG, "isConnected:true");
//        } else {
//            Log.d(TAG, "isConnected:false");
//            Toast.makeText(context, "Please check your network.", Toast.LENGTH_LONG).show();
//        }

        //Check Permission
        if (!Settings.canDrawOverlays(context)) {
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            myIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(myIntent, 101);
        } else {
            if (isOpenPermission()) {
                isPermissionReady = true;
                isSelectFile = false;
                handler.sendEmptyMessage(1);
            } else {
                isPermissionReady = false;
                isSelectFile = true;

                ActivityCompat.requestPermissions(WelcomeActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS},
                        15151);

            }
        }


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(R.string.reconnecting);
                //check network
//                isNetworkReady = isConnected();
//                Log.d(TAG, "bt！！！！");
//                if (isNetworkReady) {
//                    task = new FutureTask<>(new versionThread());
//                    new Thread(task).start();
//                    try {
//                        isVerisonReady = task.get();
//                        Log.d(TAG, "future task:" + task.get());
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
//
//                    Log.d(TAG, "isConnected:true");
//                } else {
//                    Log.d(TAG, "isConnected:false");
//                }

                if (!Settings.canDrawOverlays(context)) {
                    Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    myIntent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(myIntent, 101);
                } else {
                    if (isOpenPermission()) {
                        isPermissionReady = true;
                        isSelectFile = false;
                        handler.sendEmptyMessage(1);
                    } else {
                        isPermissionReady = false;
                        isSelectFile = true;

                        ActivityCompat.requestPermissions(WelcomeActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE,
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.MODIFY_AUDIO_SETTINGS},
                                15151);

                    }
                }
            }
        });
//
//        newVersionDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//
//                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//
//        //對話框碰到外邊不會被取消
//        newVersionDialog.setCanceledOnTouchOutside(false);


    }

    /**
     * 用來判斷是否已經取得必要的權限
     *
     * @return true or false
     * @see #isOpenPermission()
     */
    private boolean isOpenPermission() {
        tv.setText(R.string.check_permission);
        int permission3 = ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        int permission4 = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        int permission5 = ActivityCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS);

        return permission3 == PackageManager.PERMISSION_GRANTED
                && permission4 == PackageManager.PERMISSION_GRANTED
                && permission5 == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isConnected() {
        tv.setText(R.string.check_network);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        //check connect to google
        //            if(isOnline()){
        //                return true;
        //            }else{
        //                return false;
        //            }
        return networkInfo != null && networkInfo.isConnected();
    }

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            return (returnVal == 0);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
        return false;
    }

    private android.app.AlertDialog alertDialog(String title, String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                go2GooglePlay();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new android.app.AlertDialog.Builder(WelcomeActivity.this)
                        .setTitle(getResources().getString(R.string.exit_app))
                        .setMessage(getResources().getString(R.string.exit_app_description))
                        .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                go2GooglePlay();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        });
        return builder.create();
    }

    private void go2GooglePlay() {
        final String appPackageName = this.getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode :" + requestCode);
        Log.d(TAG, "resultCode :" + resultCode);
        if (requestCode == 101) {
            if (!Settings.canDrawOverlays(context)) {
                handler.sendEmptyMessage(1);
            } else {
                if (isOpenPermission()) {
                    isPermissionReady = true;
                    isSelectFile = false;
                    handler.sendEmptyMessage(1);
                } else {
                    isPermissionReady = false;
                    isSelectFile = true;

                    ActivityCompat.requestPermissions(WelcomeActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE,
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS},
                            15151);
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 15151) {
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    isPermissionReady = true;
                    isSelectFile = true;
                } else {
                    isPermissionReady = false;
                }
            } else {
                isPermissionReady = false;
                // 導引到permission頁面
                new android.app.AlertDialog.Builder(WelcomeActivity.this)
                        .setTitle(getResources().getString(R.string.open_permission))
                        .setMessage(getResources().getString(R.string.open_permission_description))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "setPositiveButton");
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                                //finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "setNegativeButton");
                                finish();
                            }
                        })
                        .show();
            }
            isSelectFile = false;
        } else {
            isSelectFile = false;
            isPermissionReady = false;
        }
        handler.sendEmptyMessage(1);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
