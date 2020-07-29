package brahma.vmi.brahmalibrary.wcitui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.apprtc.CallActivity;
import brahma.vmi.brahmalibrary.common.ConnectionInfo;
import brahma.vmi.brahmalibrary.common.DatabaseHandler;
import brahma.vmi.brahmalibrary.wcitui.adapter.CustomGridAfter;

import static brahma.vmi.brahmalibrary.activities.AppRTCActivity.OpenAppMode;
import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.appListArray;
import static brahma.vmi.brahmalibrary.apprtc.CallActivity.RESULTCODE_APPLIST;
import static brahma.vmi.brahmalibrary.apprtc.CallActivity.RESULTCODE_CLOSECALL;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.gridcontext;


public class GridActivityAfter extends AppCompatActivity {
    ArrayList<String> app_name = new ArrayList<>();
    ArrayList<String> app_icon = new ArrayList<>();
    ArrayList<String> packageName = new ArrayList<>();
    AlertDialog progressBarDialog;
    InnerRecevier innerReceiver = new InnerRecevier();
    private JSONObject jsonResponse = null;
    private String TAG = "GridActivityAfter";
    private AlertDialog logout_dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        getAppList();
        gridcontext = this;
        Intent intent = getIntent();

        View progressView = LayoutInflater.from(GridActivityAfter.this).inflate(R.layout.dialog_progresscircle, null);
//        ProgressBar progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
        TextView tv_progress = (TextView) progressView.findViewById(R.id.tv_progress);
        tv_progress.setText(getResources().getString(R.string.appRTC_toast_connection_start));
        MaterialAlertDialogBuilder progressBuilder = new MaterialAlertDialogBuilder(GridActivityAfter.this);
        progressBuilder.setView(progressView);
        progressBarDialog = progressBuilder.create();

        View logoutView = LayoutInflater.from(GridActivityAfter.this).inflate(R.layout.dialog_message, null);
        TextView message_tv = (TextView) logoutView.findViewById(R.id.message_tv);
        message_tv.setText(getResources().getString(R.string.logout_applist2));
        MaterialAlertDialogBuilder logoutBuilder = new MaterialAlertDialogBuilder(GridActivityAfter.this);

        logoutBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            Intent intent = new Intent();
            setResult(RESULTCODE_CLOSECALL, intent);
            OpenAppMode = false;
            finish();
            }
        });
        logoutBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        logoutBuilder.setTitle(R.string.logout_applist);
        logoutBuilder.setView(logoutView);
        logout_dialog = logoutBuilder.create();

        //動態註冊廣播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(innerReceiver, intentFilter);

        CustomGridAfter adapter = new CustomGridAfter(GridActivityAfter.this, app_name, app_icon, packageName);
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openApp(packageName.get(position), true, intent);
            }
        });
    }

    private void getAppList() {
        Log.d(TAG, "get APP list >>>>> " + appListArray.toString());
        parserAppList(appListArray);
    }


    private void parserAppList(JSONArray appListArray) {
        for (int k = 0; k < appListArray.length(); k++) {
            try {
                jsonResponse = appListArray.getJSONObject(k);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonResponse != null) {
                Log.d(TAG, "jsonResponse is not null");
                try {

                    Log.d(TAG, "applist data:" + jsonResponse.toString());
                    app_name.add(jsonResponse.getString("app_name"));
                    app_icon.add(jsonResponse.getString("app_icon"));
                    packageName.add(jsonResponse.getString("package_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "jsonResponse is NULL!!");
            }
        }
    }

    protected void openApp(String appPackage, boolean resume, Intent intent) {
        Log.d(TAG, "openApp:" + appPackage);
        Bundle bundle = new Bundle();
        bundle.putString("packageName", appPackage);
        intent.putExtras(bundle);
        setResult(RESULTCODE_APPLIST, intent);
        finish();
    }

    public void onDestroy() {
        Log.d("LifeCycle", "GridActivityAfter onDestroy");
        super.onDestroy();
    }

    public void onPause() {
        Log.d("LifeCycle", "GridActivityAfter onPause");
        unregisterReceiver(innerReceiver);
        super.onPause();
    }

    public void onResume() {
        Log.d("LifeCycle", "GridActivityAfter onResume");
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode:" + keyCode);
        Log.d(TAG, "event:" + event);
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            Log.d(TAG, "KEYCODE_BACK");
            logout_dialog.show();
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.d(TAG, "KEYCODE_MENU");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.d(TAG, "KEYCODE_HOME");
            return false;
        }
        return super.onKeyDown(keyCode, event);
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
                        Intent intent2 = new Intent();
                        intent2.setClass(GridActivityAfter.this, BrahmaMainActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        OpenAppMode = false;
                        startActivity(intent2);
                        finish();
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        Log.i(TAG, "多工鍵被監聽");
                        Intent intent3 = new Intent();
                        intent3.setClass(GridActivityAfter.this, BrahmaMainActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        OpenAppMode = false;
                        startActivity(intent3);
                        finish();
                    }
                }
            }
        }
    }
}
