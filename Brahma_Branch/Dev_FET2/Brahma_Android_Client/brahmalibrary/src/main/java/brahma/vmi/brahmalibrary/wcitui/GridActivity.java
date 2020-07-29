package brahma.vmi.brahmalibrary.wcitui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import brahma.vmi.brahmalibrary.auth.AuthData;
import brahma.vmi.brahmalibrary.common.ConnectionInfo;
import brahma.vmi.brahmalibrary.common.StateMachine;
import brahma.vmi.brahmalibrary.services.SessionService;
import brahma.vmi.brahmalibrary.wcitui.adapter.CustomGrid;

import static brahma.vmi.brahmalibrary.activities.AppRTCActivity.OpenAppMode;
import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.appListArray;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.dbHandler;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.gridcontext;


public class GridActivity extends AppCompatActivity {
    public static final int REQUEST_REFRESHAPPS_QUICK = 200; // send a request for the app list, get result, and finish
    public static final int REQUEST_REFRESHAPPS_FULL = 201; // send a request for the app list, get result, and finish
    public static final int REQUEST_STARTAPP_RESUME = 202; // resume the activity after returning
    public static final int REQUEST_STARTAPP_FINISH = 203; // if we return without errors, finish the activity
    ArrayList<String> app_name = new ArrayList<>();
    ArrayList<String> app_icon = new ArrayList<>();
    ArrayList<String> packageName = new ArrayList<>();
    AlertDialog progressBarDialog;
    private JSONObject jsonResponse = null;
    private String TAG = "GridActivity";
    ProgressBar progressBar;
    private TextView tv_progress;
    private String sendappPackage;
    private int sendRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        gridcontext = this;
//        Intent intent = getIntent();
        View progressView = LayoutInflater.from(GridActivity.this).inflate(R.layout.dialog_progresscircle, null);
        progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
        tv_progress = (TextView) progressView.findViewById(R.id.tv_progress);
        tv_progress.setText(getResources().getString(R.string.appRTC_toast_connection_start));
        MaterialAlertDialogBuilder progressBuilder = new MaterialAlertDialogBuilder(GridActivity.this);
        progressBuilder.setView(progressView);
        progressBarDialog = progressBuilder.create();

//        CustomGrid adapter = new CustomGrid(GridActivity.this, app_name, app_icon, packageName);
//        grid = (GridView) findViewById(R.id.grid);
//        grid.setAdapter(adapter);
//        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                openApp(packageName.get(position), true);
//            }
//        });
    }

    private void getAppList() {
        Log.d(TAG, "get APP list >>>>> " + appListArray.toString());
        packageName.clear();
        app_icon.clear();
        app_name.clear();
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
                try {
                    Log.d(TAG, "applist data:" + jsonResponse.toString());
                    Log.d(TAG, "applist data:" + jsonResponse.getString("app_name"));
                    Log.d(TAG, "applist data:" + jsonResponse.getString("app_icon"));
                    Log.d(TAG, "applist data:" + jsonResponse.getString("package_name"));
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

    protected void openApp(String appPackage, boolean resume) {

        this.sendRequestCode = resume ? REQUEST_STARTAPP_RESUME : REQUEST_STARTAPP_FINISH;
        this.sendappPackage = appPackage;
        Log.d(TAG, "openApp sendappPackage:" + sendappPackage);
        authPrompt(); // utilizes "startActivityForResult", which uses this.sendRequestCode
        startProgressDialog(getString(R.string.login_wait));
    }

    private void authPrompt() {
        Log.d("LifeCycle", "authPrompt");
        OpenAppMode = true;
        startAppRTCWithAuth();
    }

    // prepares a JSONObject using the auth dialog input, then starts the AppRTC connection
    private void startAppRTCWithAuth() {
        Log.d("LifeCycle", "startAppRTCWithAuth");

        // store the user credentials to be used by the AppRTCClient
//        AuthData.setAuthJSON(connectionInfo, jsonObject);
        // start the connection
        startAppRTC();
    }

    // Start the AppRTC service and allow child to start correct AppRTC activity
    protected void startAppRTC() {
        Log.d("LifeCycle", "startAppRTC");
        // if the session service is running for a different connection, stop it
        boolean stopService = SessionService.getState() != StateMachine.STATE.NEW;
//                SessionService.getConnectionID() != connectionInfo.getConnectionID()

        if (stopService)
            stopService(new Intent(this, SessionService.class));

        // make sure the session service is running for this connection
        if (stopService || SessionService.getState() == StateMachine.STATE.NEW) {
            startService(new Intent(this, SessionService.class).putExtra("connectionID", 1));
//                startService(new Intent(this, NetMService.class).putExtra("connectionID", connectionInfo.getConnectionID()));
        }
        // after we make sure the service is started, we can start the AppRTC actions for this activity
        afterStartAppRTC();
    }

    private void afterStartAppRTC() {
        Log.d("LifeCycle", "BrahmaMainActivity afterStartAppRTC");
        Intent intent = new Intent();
        Log.d(TAG, "packageName >>>>> " + packageName);
        intent.putExtra("pkgName", sendappPackage);
//        packageName = null;
        intent.setClass(GridActivity.this, CallActivity.class);
        intent.putExtra("connectionID", 1);
        startActivityForResult(intent, sendRequestCode);
//        finish();
    }

    public void onDestroy() {
        Log.d("LifeCycle", "GridActivity onDestroy");
        super.onDestroy();
    }

    public void onPause() {
        Log.d("LifeCycle", "GridActivity onPause");
        super.onPause();
        stopProgressDialog();
    }

    public void onResume() {
        Log.d("LifeCycle", "GridActivity onResume");
        super.onResume();
        View progressView = LayoutInflater.from(GridActivity.this).inflate(R.layout.dialog_progresscircle, null);
        progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar);
        tv_progress = (TextView) progressView.findViewById(R.id.tv_progress);
        tv_progress.setText(getResources().getString(R.string.appRTC_toast_connection_start));
        MaterialAlertDialogBuilder progressBuilder = new MaterialAlertDialogBuilder(GridActivity.this);
        progressBuilder.setView(progressView);
        progressBarDialog = progressBuilder.create();

        getAppList();

        CustomGrid adapter = new CustomGrid(GridActivity.this, app_name, app_icon, packageName);
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openApp(packageName.get(position), true);
            }
        });

    }

    private void startProgressDialog(String msg) {
        progressBarDialog.show();

    }

    private void stopProgressDialog() {
        if (progressBarDialog != null) {
            progressBarDialog.dismiss();
            progressBarDialog = null;
        }
    }
}
