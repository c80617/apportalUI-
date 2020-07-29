package brahma.vmi.brahmalibrary.netspeed;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import java.text.NumberFormat;

import static android.content.Context.MODE_PRIVATE;
import static brahma.vmi.brahmalibrary.client.PingLatency.bandwidth;


public class WindowUtil {
    public static final int INTERVAL = 2000;
    public static int statusBarHeight = 0;
    //记录悬浮窗的位置
    public static int initX, initY;
    public static boolean isShowing = false;
    private static int uid = 0;
    private static int ping = 0;
    public SpeedView speedView;
    SharedPreferences sharedPreferences;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private Context context;
    private long preRxBytes, preSeBytes;
    private long rxBytes, seBytes;
    private static final int UNSUPPORTED = -1;
    String TAG = "WindowUtil";
    private Handler handler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            calculateNetSpeed();
            sendEmptyMessageDelayed(0, INTERVAL);
        }
    };

    public WindowUtil(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        speedView = new SpeedView(context);
        params = new WindowManager.LayoutParams();
        params = new WindowManager.LayoutParams();
        params.x = initX;
        params.y = initY;
        params.width = params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //params.type= WindowManager.LayoutParams.TYPE_PHONE;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        //设置悬浮窗可以拖拽至状态栏的位置
//        | WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            uid = info.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void setUid(int id) {
        uid = id;
    }

    public static void setPing(int pg) {
        ping = pg;
    }

    public boolean isShowing() {
        return isShowing;
    }

    private void calculateNetSpeed() {
//        Log.d("calculateNetSpeed", String.valueOf(uid));

//        rxBytes= TrafficStats.getUidRxBytes(uid);
//        seBytes= TrafficStats.getUidTxBytes(uid)-rxBytes;

        rxBytes = TrafficStats.getTotalRxBytes();
        seBytes = TrafficStats.getTotalTxBytes();

        double downloadSpeed = (rxBytes - preRxBytes) ;
        double uploadSpeed = (seBytes - preSeBytes) ;

        preRxBytes = rxBytes;
        preSeBytes = seBytes;

        //根据范围决定显示单位
        String upSpeed = null;
        String downSpeed = null;
        NumberFormat df = java.text.NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(2);

        downloadSpeed /= (1024);
        bandwidth = (int) downloadSpeed;

        if (downloadSpeed > 1024 * 1024) {
            downloadSpeed /= (1024 * 1024);
            downSpeed = df.format(downloadSpeed) + "GB/s";
        } else if (downloadSpeed > 1024) {
            downloadSpeed /= (1024);
            downSpeed = df.format(downloadSpeed) + "MB/s";
        } else {
            downSpeed = df.format(downloadSpeed) + "KB/s";
        }
//            Log.d(TAG, "downSpeed : " + downSpeed);

        if (uploadSpeed > 1024 * 1024) {
            uploadSpeed /= (1024 * 1024);
            upSpeed = df.format(uploadSpeed) + "MB/s";
        } else if (uploadSpeed > 1024) {
            uploadSpeed /= (1024);
            upSpeed = df.format(uploadSpeed) + "KB/s";
        } else {
            upSpeed = df.format(uploadSpeed) + "B/s";
        }

        updateSpeed("↓ " + downSpeed, "↑ " + upSpeed);
    }

    public void showSpeedView() {
        Log.d("showSpeedView", String.valueOf(uid));
        if (sharedPreferences.getString("setting_bandwidth", "false").equals("true")) {
            windowManager.addView(speedView, params);
        }

        isShowing = true;
        preRxBytes = TrafficStats.getTotalRxBytes();
        preSeBytes = TrafficStats.getTotalTxBytes();

//        preRxBytes= TrafficStats.getUidRxBytes(uid);
//        preSeBytes= TrafficStats.getUidTxBytes(uid)-preRxBytes;

        handler.sendEmptyMessage(0);
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    public void closeSpeedView() {
        windowManager.removeView(speedView);
        isShowing = false;
    }

    public void updateSpeed(String downSpeed, String upSpeed) {
        speedView.upText.setText(upSpeed);
        speedView.downText.setText(downSpeed + "\nPING: " + ping + " ms");
    }

}
