package brahma.vmi.covid2019.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;

import static brahma.vmi.covid2019.wcitui.WelcomeActivity.isSelectFile;

public class SampleLifecycleListener implements LifecycleObserver {

    private String TAG = "LifecycleObserver";

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onAny(LifecycleOwner owner, Lifecycle.Event event) {
        Log.e(TAG, "onAny:" + event.name());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate() {
        Log.e(TAG, "onCreate");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        Log.e(TAG, "onDestroy");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        Log.e(TAG, "onPause isSelectFile:" + isSelectFile);
        if (!isSelectFile) {
            Log.e(TAG, "System.exit(0),強制結束應用程式");
//            System.exit(0);
        }
    }
}