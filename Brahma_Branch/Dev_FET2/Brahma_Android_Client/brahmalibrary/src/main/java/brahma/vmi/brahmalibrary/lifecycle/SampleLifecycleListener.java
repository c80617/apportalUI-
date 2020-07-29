package brahma.vmi.brahmalibrary.lifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import android.util.Log;

import static brahma.vmi.brahmalibrary.wcitui.WelcomeActivity.isSelectFile;

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
//        if (!isSelectFile) {
//            Log.e(TAG, "System.exit(0),強制結束應用程式");
//        }
    }
}