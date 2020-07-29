package brahma.vmi.brahmalibrary.netspeed;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import brahma.vmi.brahmalibrary.apprtc.CallActivity;


public class SpeedCalculationService extends Service {
    private WindowUtil windowUtil;
    SharedPreferences sharedPreferences;
    @Override
    public void onCreate() {
        super.onCreate();
        WindowUtil.initX = (int) SharedPreferencesUtils.getFromSpfs(this, CallActivity.INIT_X, 0);
        WindowUtil.initY = (int) SharedPreferencesUtils.getFromSpfs(this, CallActivity.INIT_Y, 0);
        windowUtil = new WindowUtil(this);
        sharedPreferences = this.getSharedPreferences("data", MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!windowUtil.isShowing()) {
            windowUtil.showSpeedView();
        }

        //SharedPreferencesUtils.putToSpfs(this,AppRTCVideoActivity.IS_SHOWN,true);
        SharedPreferencesUtils.putToSpfs(this, CallActivity.IS_SHOWN, true);
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sharedPreferences.getString("setting_bandwidth", "false").equals("true")) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) windowUtil.speedView.getLayoutParams();
            SharedPreferencesUtils.putToSpfs(this, CallActivity.INIT_X, params.x);
            SharedPreferencesUtils.putToSpfs(this, CallActivity.INIT_Y, params.y);
            windowUtil.closeSpeedView();
            SharedPreferencesUtils.putToSpfs(this, CallActivity.IS_SHOWN, false);
        }
        Log.d("WindowUtil", "SpeedCalculationService onDestroy");
    }
}
