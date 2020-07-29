package brahma.vmi.brahmalibrary.lifecycle;

import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class SampleApp extends Application {

    private SampleLifecycleListener lifecycleListener = new SampleLifecycleListener();

    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
        setupLifecycleListener();
    }

    private void setupLifecycleListener() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleListener);
    }
}
