/*
Copyright 2013 The MITRE Corporation, All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package brahma.vmi.covid2019.client;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import com.google.protobuf.ByteString;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.activities.AppRTCActivity;
import brahma.vmi.covid2019.activities.CameraActivity;
import brahma.vmi.covid2019.activities.QrActivity;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;

import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.context;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.isSelectFile;

/**
 * @developer Ian
 * Receives Intents from the server to act upon on the client-side
 */
public class IntentHandler {
    private static final String TAG = IntentHandler.class.getName();
    private static MediaRecorderHandler mrh = new MediaRecorderHandler();

    public static void inspect(BRAHMAProtocol.Response response, Context context) {
        BRAHMAProtocol.Intent intent = response.getIntent();
        String temp = getCurrentActivityName(context);
        Log.d("IntentHandler", "temp:"+temp);
        switch(intent.getAction()) {
            case ACTION_DIAL:
                Log.d(TAG, String.format("Received 'call' Intent for number '%s'", intent.getData()));
                int telephonyEnabled = isTelephonyEnabled(context);
                if (telephonyEnabled == 0) {
                    Intent call = new Intent(Intent.ACTION_CALL);
                    call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    call.setData(Uri.parse(intent.getData()));
                    context.startActivity(call);
                }
                else {
                // phone calls are not supported on this device; send a Toast to the user to let them know
                    Toast toast = Toast.makeText(context, telephonyEnabled, Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case ACTION_VIEW:
                Log.d(TAG, String.format("Received 'view' Intent for uri '%s'", intent.getData()));
                Intent view = new Intent(Intent.ACTION_VIEW);

                //Add by Yuyu 20171226
                if(intent.getData().equals("ACTION_QRCODE_SCAN")) {
                    int permission = ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA);

//                    if (permission != PackageManager.PERMISSION_GRANTED) {
//                        //未取得權限
//                        Log.d(TAG,"camera:false");
//                        Toast.makeText(context,R.string.NO_HAS_CAREMA_PERMISSION,Toast.LENGTH_SHORT).show();
//
//                    }else{
//                        //已有權限
//                        Log.d(TAG,"camera:true");
//                        AppRTCActivity.setOpenCamera(true);
//                        Intent QrCamera = new Intent(context, QrActivity.class);
//                        QrCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(QrCamera);
//                    }
                    isSelectFile = true;
                    AppRTCActivity.setOpenCamera(true);
                    Intent QrCamera = new Intent(context, QrActivity.class);
                    QrCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(QrCamera);
                }
                else if(intent.getData().equals("ACTION_IMAGE_CAPTURE")) {
                    //Add by Yuyu 20171225
                    Log.d(TAG,"ACTION_IMAGE_CAPTURE");
                    Log.d(TAG,"check camera permission");
                    int permission = ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA);

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        //未取得權限
                        Log.d(TAG,"camera:false");
                        Toast.makeText(context,R.string.NO_HAS_CAREMA_PERMISSION,Toast.LENGTH_LONG).show();
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.CAMERA},
//                                15151);
                    }else{
                        //已有權限
                        Log.d(TAG,"camera:true");
                        AppRTCActivity.setOpenCamera(true);
                        Intent intentCamera = new Intent(context,CameraActivity.class);
                        intentCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentCamera);
                    }
                }
                else if(intent.getData().equals("ACTION_BARCODE_SCAN")) {
                    isSelectFile = true;
                    //Add by YiWen 20180313
                    AppRTCActivity.setOpenCamera(true);
                    Intent QrCamera = new Intent(context, QrActivity.class);
                    QrCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(QrCamera);
                }else{
                    view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (intent.hasFile()) {
                        BRAHMAProtocol.File f = intent.getFile();
                        Log.e(TAG, "Receiving a file with filename: " + f.getFilename());
                        saveToFile(f);
                        File savedFile = new File("/sdcard/" + f.getFilename());
                        view.setDataAndType(Uri.fromFile(savedFile), "application/pdf");
                    } else {
                        view.setData(Uri.parse(intent.getData()));
                    }
                    context.startActivity(view);
                }
                break;

            case ACTION_AUDIO_START:
                Log.d("AudioHandler", "ACTION_AUDIO_START");
//                isRecording = true;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("AudioHandler","start");
                        mrh.startRecord();
                        //ah.StartRecord();
                    }
                });
                thread.start();
                break;
            case ACTION_AUDIO_STOP:
//                isRecording = false;
                //ah.StopRecord();
                mrh.stopAndRelease();
                Log.d("AudioHandler", "ACTION_AUDIO_STOP");
                break;
            default:
                break;
        }
    }

    protected static void saveToFile(BRAHMAProtocol.File f) {
        try {
            ByteString bs = f.getData();
            byte[] arr = bs.toByteArray();
            FileOutputStream out = new FileOutputStream("/sdcard/" + f.getFilename());
            out.write(arr);
            out.close();
        } catch(Exception e) {}
    }

    // returns an error message if telephony is not enabled
    private static int isTelephonyEnabled(Context context){
        int resId = 0;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM
                    && !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
                resId = R.string.intentHandler_toast_noTelephonyCDMA;
            else if (tm.getSimState() != TelephonyManager.SIM_STATE_READY)
                resId = R.string.intentHandler_toast_noTelephonyGSM;
        }
        return resId;
    }


    private static String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);


        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);


        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    private static boolean checkPermission(String permName, String pkgName){
        PackageManager pm = context.getPackageManager();
        if(PackageManager.PERMISSION_GRANTED == pm.checkPermission(permName, pkgName)){
            Log.d(TAG,"has permission : " + permName);
            return true;
        }else{
            Log.d(TAG,"not has permission : " + permName);
//            String[] permission = {permName};
//            ActivityCompat.requestPermissions(BrahmaMainActivity.this,permission,100);
            return false;
        }
    }
}
