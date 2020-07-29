package brahma.vmi.brahmalibrary.wcitui;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.system.ErrnoException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.appcompat.app.AppCompatActivity;

import javax.microedition.khronos.opengles.GL10;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.apprtc.RenderServerView;

import static android.system.Os.setenv;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "RENDER_SERVER";

    // Used to load the 'native-lib' library on application startup.
    //change or add an environment variable
    static {
        try {
            setenv("ASAN_OPTIONS",
                    "detect_leaks=0:detect_container_overflow=0:detect_odr_violation=0:symbolize=1",
                    true);
            setenv("ANDROID_EMU_TEST_WITH_HOST_GPU", "1", true);
            setenv("ANDROID_EMU_TEST_WITH_WINDOW", "1", true);
            setenv("SHOW_FPS_STATS", "1", true);
            setenv("SHOW_PERF_STATS", "1", true);

        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getRealMetrics(displayMetrics);

        createEMUGLEngine(displayMetrics.widthPixels, displayMetrics.heightPixels);

        mRenderServerView = findViewById(R.id.render_server_view);
        //mRenderServerView.setRenderer(new RootGLRenderer());
        mSurfaceHolder = mRenderServerView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart()");
        nativeOnStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        nativeOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause()");
        nativeOnPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop()");
        nativeOnStop();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.v(TAG, "surfaceCreated");

        //holder.setFormat(PixelFormat.RGB_888);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // This method is always called at least once, after surfaceCreated(SurfaceHolder).
        Log.v(TAG, "surfaceChanged format=" + format + "," +
                " width=" + width + ", height=" + height);
        if(!initialized)    {
            initialized = true;
            mFormat = format;
            mWidth = width;
            mHeight = height;
            nativeSetSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.v(TAG, "surfaceDestroyed");
        nativeSetSurface(null);
    }

    class RootGLRenderer implements GLSurfaceView.Renderer {
        public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config)    {
            gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            gl.glViewport(0, 0, w, h);
        }

        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        }
    }

    private static native boolean createEMUGLEngine(int displayWidth, int displayHeight);
    private static native void shutdownEMUGLEngine();
    private static native void nativeOnStart();
    private static native void nativeOnResume();
    private static native void nativeOnPause();
    private static native void nativeOnStop();
    private static native void nativeSetSurface(Surface surface);

    private boolean initialized = false;
    private int mFormat;
    private int mWidth;
    private int mHeight;
    private RenderServerView mRenderServerView;
    private SurfaceHolder mSurfaceHolder;
}
