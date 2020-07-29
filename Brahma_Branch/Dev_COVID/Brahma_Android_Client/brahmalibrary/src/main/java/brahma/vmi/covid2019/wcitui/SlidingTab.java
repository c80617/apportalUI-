package brahma.vmi.covid2019.wcitui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.apprtc.CallActivity;
import brahma.vmi.covid2019.auth.AuthData;
import brahma.vmi.covid2019.client.HomeListen;
import brahma.vmi.covid2019.common.AppInfo;
import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.common.Constants;
import brahma.vmi.covid2019.common.DatabaseHandler;
import brahma.vmi.covid2019.common.StateMachine;
import brahma.vmi.covid2019.services.SessionService;
import brahma.vmi.covid2019.wcitui.devicelog.DeviceInfo;
import brahma.vmi.covid2019.wcitui.fragment.TabFragment;

import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.dbHandler;

/**
 * @author YiWen Li
 * @file SlidingTab
 * @brief 顯示appStreaming mode之滑動視窗
 * <p>
 * 取得appstreaming資訊之後,
 * 利用Fragment產生多個tab後顯示,
 * 使用者也可點擊後登入app進行操作
 * <p>
 * @date 2019/07/12
 **/
public class SlidingTab extends AppCompatActivity implements Constants, NavigationView.OnNavigationItemSelectedListener {
    //    public class SlidingTab extends BrahmaActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final int REQUEST_REFRESHAPPS_QUICK = 200; // send a request for the app list, get result, and finish
    public static final int REQUEST_REFRESHAPPS_FULL = 201; // send a request for the app list, get result, and finish
    public static final int REQUEST_STARTAPP_RESUME = 202; // resume the activity after returning
    public static final int REQUEST_STARTAPP_FINISH = 203; // if we return without errors, finish the activity
    public static final String TAG = "SlidingTab";
    protected boolean busy = false; // is set to 'true' immediately after starting a connection, set to 'false' when resuming
    public static View cv;
    public static Context context;
    public FragmentManager fm;
    protected int connectionID;
    protected ConnectionInfo connectionInfo;
    protected boolean launchedFromShortcut;
    AuthData pd = new AuthData();
    long result;
    DeviceInfo deviceInfo = new DeviceInfo();
//    LogoutStream logoutStream = new LogoutStream();
    //Notification alert
    String packageName;
    String appName;
//    InnerRecevier innerReceiver = new InnerRecevier();
    private int sendRequestCode;
    private AppInfo sendAppInfo;
    private String cName = "Error";
    private String sendappPackage;
    private HomeListen mHomeListen = null;
    private boolean showVM = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycle", "SlidingTab onCreate() executed");
        //send broadcast
        Intent intent2 = new Intent("STOP_PROGRESS");
        sendBroadcast(intent2);

        stopProgressDialog();
        //set context
        context = this;
        View view = View.inflate(getApplicationContext(), R.layout.dialog_layout, null);
        cv = getWindow().getDecorView();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Brahma PORTRAIT
        dbHandler = new DatabaseHandler(this);
        //Home key listen init
        initHomeListen();

        //建立廣播
        //InnerRecevier innerReceiver = new InnerRecevier();
        //動態註冊廣播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //啟動廣播
//        registerReceiver(innerReceiver, intentFilter);
        // get intent data
        Intent intent = getIntent();
        connectionID = intent.getIntExtra("connectionID", 0);
        connectionInfo = dbHandler.getConnectionInfo(connectionID);
        if (intent.getAction() == "NOTIFICATION_ACTION") {
            connectionInfo = new ConnectionInfo(1, "appMode", intent.getStringExtra("username"), intent.getStringExtra("host"), Integer.valueOf(intent.getStringExtra("port")), 1, 1, "", 0, "appstreaming");
            result = dbHandler.updateConnectionInfo(connectionInfo);
            packageName = intent.getStringExtra("packageName");
            appName = intent.getStringExtra("appName");

            launchedFromShortcut = true;
            AppInfo appInfo = dbHandler.getAppInfo(connectionID, packageName);
            openApp(appInfo, false); // open the app; if we return without errors, finish the activity
        }

        if (intent.hasExtra("cName")) {
            cName = intent.getStringExtra("cName");
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("SlidingTabs");//設定barTitle
        initTabFragment(savedInstanceState);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //real display size
//        Point displaySize = new Point();
//        getWindowManager().getDefaultDisplay().getSize(displaySize);
//        DrawerLayout.LayoutParams layoutParams= new DrawerLayout.LayoutParams(displaySize.x, displaySize.y);
//        drawer.setLayoutParams(layoutParams);

        //Navigation part
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Use the native icon.
        navigationView.setItemIconTintList(null);
        //Set header content.
        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.nav_header);
        name.setText(cName);

        // if this was started by a desktop shortcut, just launch the requested app
        if (ACTION_LAUNCH_APP.equals(intent.getAction())) {
            launchedFromShortcut = true;
            String packageName = intent.getStringExtra("packageName");

            AppInfo appInfo = dbHandler.getAppInfo(connectionID, packageName);
            openApp(appInfo, false); // open the app; if we return without errors, finish the activity
        }

        if (intent.hasExtra("tagPkgName")) {
            sendappPackage = intent.getStringExtra("tagPkgName");
            new Thread() {
                public void run() {
                    openApp(sendappPackage, true);
                }
            }.start();
            Log.d(TAG, "tagPkgName sendappPackage:" + sendappPackage);
        }
        //navigation bar hide
//        final  View decorView = getWindow().getDecorView();
//        final int  uiOption = View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//
//        decorView.setSystemUiVisibility(uiOption);
//
//        // This code will always hide the navigation bar
//        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){
//            @Override
//            public void  onSystemUiVisibilityChange(int visibility)
//            {
//                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
//                {
//                    decorView.setSystemUiVisibility(uiOption);
//                }
//            }
//        });
    }

    private void initHomeListen() {
        mHomeListen = new HomeListen(this);
        mHomeListen.setOnHomeBtnPressListener(new HomeListen.OnHomeBtnPressLitener() {
            @Override
            public void onHomeBtnPress() {
                Log.d(TAG, "onHomeBtnPress()");
            }

            @Override
            public void onHomeBtnLongPress() {//Not work but is find
                Log.d(TAG, "onHomeBtnLongPress()");
            }
        });
    }

    @Override
    public void onResume() {
        Log.d("LifeCycle", "SlidingTab onResume");
        super.onResume();
        mHomeListen.start();
    }

    @Override
    protected void onUserLeaveHint() {
        finish();
        super.onUserLeaveHint();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHomeListen.stop();
        stopProgressDialog();
        Log.d("LifeCycle", " SlidingTab onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        stopProgressDialog();
        Log.d("LifeCycle", "SlidingTab onStop()");
    }

    // activity returns
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        busy = false;

        Log.d("LifeCycle", "Sliding onActivityResult");
//        Fragment fragment = fm.findFragmentByTag("GoFragment");
//        fragment.onActivityResult(requestCode, resultCode, data);

        // QRcode result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scanner fail", Toast.LENGTH_LONG).show();
            } else {
                openApp(result.getContents(), true);
            }
        }
//        if (requestCode == REQUEST_REFRESHAPPS_QUICK || requestCode == REQUEST_REFRESHAPPS_FULL) {
//            if (resultCode == RESULT_CANCELED) {
//                // the activity ended before processing the Apps response
//                toastShort(R.string.appList_toast_refreshFail);
//            }
//            else if (resultCode == RESULT_OK) {
//                toastShort(R.string.appList_toast_refreshSuccess);
//                super.onActivityResult(requestCode, RESULT_REPOPULATE, data);
//            }
//            else {
//                // this is probably a result of an AUTH_FAIL, let superclass handle it
//                super.onActivityResult(requestCode, resultCode, data);
//            }
//        }
        else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_STARTAPP_FINISH) {
            // the user intentionally canceled the activity, and we are supposed to finish this activity after resuming
            finish();
        } else // fall back to superclass method
            super.onActivityResult(requestCode, resultCode, data);
    }

    // this can be triggered by clicking an app in this activity, or by clicking a shortcut on the desktop
    protected void openApp(AppInfo appInfo, boolean resume) {
        if (appInfo != null) {
            this.sendRequestCode = resume ? REQUEST_STARTAPP_RESUME : REQUEST_STARTAPP_FINISH;
            this.sendAppInfo = appInfo;
            authPrompt(connectionInfo); // utilizes "startActivityForResult", which uses this.sendRequestCode

            Looper.prepare();
            startProgressDialog(getString(R.string.login_wait));
            Looper.loop();
        }
    }

    // this can be triggered by clicking an app in this activity, or by clicking a shortcut on the desktop
    protected void openApp(String appPackage, boolean resume) {

        this.sendRequestCode = resume ? REQUEST_STARTAPP_RESUME : REQUEST_STARTAPP_FINISH;
        this.sendappPackage = appPackage;
        Log.d(TAG, "openApp sendappPackage:" + sendappPackage);
        //need to get node
        authPrompt(connectionInfo); // utilizes "startActivityForResult", which uses this.sendRequestCode

        Looper.prepare();
        startProgressDialog(getString(R.string.login_wait));
        Looper.loop();
    }

    private void initTabFragment(Bundle savedInstanceState) {
        Log.d("LifeCycle", "SlidingTab initTabFragment");
        //isUserLeave = true;
        if (savedInstanceState == null) {
            TabFragment tabFragment = new TabFragment(connectionInfo);//執行tabFragment

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_fragment, tabFragment)
                    .commit();
        }
    }

    private void authPrompt(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "authPrompt");
        if (busy)
            return;
        busy = true;
        startAppRTCWithAuth(connectionInfo);
    }

    // prepares a JSONObject using the auth dialog input, then starts the AppRTC connection
    private void startAppRTCWithAuth(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "startAppRTCWithAuth");
        // create a JSON Object
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
        Log.d("LifeCycle", "startAppRTC");
        // if the session service is running for a different connection, stop it
        boolean stopService = SessionService.getConnectionID() != connectionInfo.getConnectionID()
                && SessionService.getState() != StateMachine.STATE.NEW;
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

    protected void afterStartAppRTC(ConnectionInfo connectionInfo) {
        Log.d("LifeCycle", "afterStartAppRTC()");
        // after we have handled the auth prompt and made sure the service is started...

        // create explicit intent
        Intent intent = new Intent();
        if (this.sendRequestCode == REQUEST_REFRESHAPPS_QUICK || this.sendRequestCode == REQUEST_REFRESHAPPS_FULL) {
            // we're refreshing our cached list of apps that reside on the VM
            //intent.setClass(SlidingTab.this, AppRTCRefreshAppsActivity.class);
            if (this.sendRequestCode == REQUEST_REFRESHAPPS_FULL)
                intent.putExtra("fullRefresh", true);
        } else {

            // we're starting the video feed and launching a specific app
            //intent.setClass(SlidingTab.this, AppRTCVideoActivity.class);
            intent.setClass(SlidingTab.this, CallActivity.class);
            Log.d(TAG, "afterStartAppRTC sendappPackage:" + sendappPackage);
            intent.putExtra("pkgName", sendappPackage);
            showVM = true;
        }
        intent.putExtra("connectionID", connectionInfo.getConnectionID());

        // start the AppRTCActivity
        startActivityForResult(intent, this.sendRequestCode);
    }

    @Override
    public void onBackPressed() {
        Log.d("LifeCycle", "SlidingTab onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        //dbHandler.close();//實作在父類別的屬性onDestroy()
    }

    @Override
    public void onDestroy() {
        //dbHandler.close();
        Log.d("LifeCycle", "SlidingTab onDestroy");
        stopProgressDialog();
//        unregisterReceiver(innerReceiver);
        super.onDestroy();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//        if (id == R.id.nav_Connect) {
//
//            // Handle the Connect action
//            Intent intent = new Intent(SlidingTab.this, BrahmaMainActivity.class);
//            // start the activity and expect a result intent when it is finished
//            startActivity(intent);
//            finish();
//            Log.i("TAG", "onClick: " +id);
//
//        } else
//            if (id == R.id.nav_QRcode) {
////            // exit and resume previous activity, report results in the intent
////                if(dbHandler.getSessionInfo(connectionInfo).getToken()!=null) {
////                // we have updated this ConnectionInfo; if session info is stored for this ConnectionInfo, remove it
////                dbHandler.clearSessionInfo(connectionInfo);
////            }
////            result = dbHandler.updateConnectionInfo(connectionInfo);
//
//            final Activity activity=this;
//            IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
//            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//            intentIntegrator.setPrompt("Scan");
//            intentIntegrator.setCameraId(0);
//            intentIntegrator.setBeepEnabled(false);
//            intentIntegrator.setBarcodeImageEnabled(false);
//            intentIntegrator.initiateScan();
//            Log.i(TAG, "onClick: " +id);
//
//        } else
        if (id == R.id.nav_LOGOUT) {
            Intent intent = new Intent(SlidingTab.this, BrahmaMainActivity.class);
            startActivity(intent);
            dbHandler.close();
            Log.i(TAG, "onClick: " + id);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startProgressDialog(String msg) {
        progressDialog = new ProgressDialog(SlidingTab.this);
        progressDialog.setCanceledOnTouchOutside(false);
        //pd.setTitle(R.string.appRTC_progressDialog_title);
        //pd.setMessage(getResources().getText(R.string.appRTC_progressDialog_message));
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
        Log.d("LifeCycle", "stopProgressDialog");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    class InnerRecevier extends BroadcastReceiver {
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
//                        logoutStream.logoutStream(deviceInfo.getmUser(), connectionInfo.getHost(), String.valueOf(connectionInfo.getPort()));
                        //Toast.makeText(MainActivity.this, "Home鍵被監聽", Toast.LENGTH_SHORT).show();
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        Log.i(TAG, "多工鍵被監聽");
//                        logoutStream.logoutStream(deviceInfo.getmUser(), connectionInfo.getHost(), String.valueOf(connectionInfo.getPort()));
                        //Toast.makeText(MainActivity.this, "多工鍵被監聽", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}

