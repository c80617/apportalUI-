package brahma.vmi.covid2019.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import brahma.vmi.covid2019.wcitui.BrahmaMainActivity;

public class AppStreamingService extends Service {

    private AppStreamingService.MyBinder mBinder = new AppStreamingService.MyBinder();
    public static final String TAG = "AppStreamingService";
    BrahmaMainActivity bActivity = new BrahmaMainActivity();

    @Override
    public void onCreate() {
        //當app退到背景的時候才開始執行
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
//        if (Build.VERSION.SDK_INT >= 26) {
        startForeground(1, new Notification.Builder(this).build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        //run loginApp()
        bActivity.loginApp();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder {

    }

}