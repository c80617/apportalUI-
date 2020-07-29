package brahma.vmi.brahmalibrary.activities;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.services.SessionService;

public class CameraFragment extends AppCompatActivity {
    private static final int TAKE_SUCCESS = 1;
    private static final int TAKE_FAIL = 0;
    ProcessCameraProvider cameraProvider;
    PreviewView viewFinder;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Camera camera;
    ExecutorService cameraExecutor;
    int displayId = -1;
    int lensFacing = CameraSelector.LENS_FACING_BACK;
    Context context;
    ImageButton camera_switch_button, camera_capture_button, photo_view_button;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String TAG = "MyCameraX";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAKE_SUCCESS:
                    Toast.makeText(context, R.string.success, Toast.LENGTH_LONG).show();
                    break;
                case TAKE_FAIL:
                    Toast.makeText(context, R.string.fail, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    private DisplayManager displayManager;
    private DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int i) {
            Log.d(TAG, "onDisplayAdded >>>>>> " + i);
        }

        @Override
        public void onDisplayRemoved(int i) {
            Log.d(TAG, "onDisplayRemoved >>>>>> " + i);
        }

        @Override
        public void onDisplayChanged(int i) {
            Log.d(TAG, "onDisplayChanged >>>>>> " + i);

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera);
        context = this;
        viewFinder = findViewById(R.id.view_finder);

        camera_switch_button = findViewById(R.id.camera_switch_button);
        camera_capture_button = findViewById(R.id.camera_capture_button);
        photo_view_button = findViewById(R.id.photo_view_button);
//        photo_view_button.setVisibility(View.INVISIBLE);
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        displayManager = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
        assert displayManager != null;
        displayManager.registerDisplayListener(displayListener, null);

        // Wait for the views to be properly laid out
        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "viewFinder Runnable");
                // Keep track of the display in which this view is attached
                displayId = (int) viewFinder.getRotation();

                // Build UI controls
                updateCameraUi();

                // Set up the camera and its use cases
                setUpCamera();
            }
        });
    }

    private void setUpCamera() {
        Log.d(TAG, "setUpCamera");
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    if (hasBackCamera()) {
                        lensFacing = CameraSelector.LENS_FACING_BACK;
                    } else if (hasFrontCamera()) {
                        lensFacing = CameraSelector.LENS_FACING_FRONT;
                    } else {
                        Log.d(TAG, "\"Back and front camera are unavailable\"");
                    }
                } catch (ExecutionException | InterruptedException | CameraInfoUnavailableException e) {
                    e.printStackTrace();
                }

                updateCameraSwitchButton();
                bindCameraUseCases();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void updateCameraSwitchButton() {
        Log.d(TAG, "updateCameraSwitchButton");
        try {
            if (hasBackCamera() && hasFrontCamera()) {
                camera_switch_button.setEnabled(true);
            } else {
                camera_switch_button.setEnabled(false);
            }
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
    }

    private boolean hasBackCamera() throws CameraInfoUnavailableException {
        Log.d(TAG, "hasBackCamera");
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
    }

    private boolean hasFrontCamera() throws CameraInfoUnavailableException {
        Log.d(TAG, "hasFrontCamera");
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
    }

    private void updateCameraUi() {
        Log.d(TAG, "updateCameraUi");
        camera_capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "updateCameraUi");
                if (imageCapture != null) {
                    File photoFile;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        photoFile = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath(), "picture.jpg");
                    } else {
                        photoFile = new File(context.getFilesDir(), "picture.jpg");
                    }

                    if (!photoFile.exists()) {
                        try {
                            photoFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

//                    // Mirror image when using the front camera
//                    ImageCapture.Metadata metadata = new ImageCapture.Metadata();
//                    metadata.isReversedHorizontal();
//                    metadata.isReversedVertical();

//                    boolean isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT;

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "NEW_IMAGE");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");

                    ImageCapture.OutputFileOptions.Builder outputFileOptionsBuilder =
                            new ImageCapture.OutputFileOptions.Builder(photoFile);

                    imageCapture.takePicture(outputFileOptionsBuilder.build(), Runnable::run, new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Log.d(TAG, "image saved success >>>>>> " + photoFile.getAbsolutePath());
                            Message msg = new Message();
                            msg.what = 1;
                            handler.sendMessage(msg);
                            forwardIntent(getBytes(photoFile));
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.d(TAG, "image saved success error");
                            Message msg = new Message();
                            msg.what = 0;
                            handler.sendMessage(msg);
                            exception.printStackTrace();
                        }
                    });
                }
            }
        });

        camera_switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                } else {
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
                }
                bindCameraUseCases();
            }
        });

        photo_view_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void bindCameraUseCases() {
        Log.d(TAG, "bindCameraUseCases");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        Log.d(TAG, "Screen metrics: " + metrics.widthPixels + " x " + metrics.heightPixels);
        int screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels);
        int rotation = displayId;
        Log.d(TAG, "rotation:" + rotation);
        // CameraSelector
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                Log.d(TAG, "setAnalyzer");
            }
        });

        cameraProvider.unbindAll();
//        cameraControl.startFocusAndMetering();


        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, imageAnalysis);
        CameraControl cameraControl = camera.getCameraControl();
        preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.getCameraInfo()));

        viewFinder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int actionEvent = motionEvent.getAction();
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                switch (actionEvent) {
                    case MotionEvent.ACTION_DOWN:
                        MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(metrics.widthPixels, metrics.heightPixels);
                        MeteringPoint point = factory.createPoint(x, y);
                        FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                                .setAutoCancelDuration(5, TimeUnit.SECONDS)
                                .build();

                        ListenableFuture future = cameraControl.startFocusAndMetering(action);
                        future.addListener(() -> {
//                            FocusMeteringResult result = null;
//                            try {
//                                result = (FocusMeteringResult) future.get();
//                            } catch (ExecutionException e) {
//                                e.printStackTrace();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            Log.d(TAG, "result isFocusSuccessful:" + result.isFocusSuccessful());
                        }, cameraExecutor);
                }
                return false;
            }
        });
    }

    private int aspectRatio(int width, int height) {
        Log.d(TAG, "aspectRatio");
        Double previewRatio = (double) (Math.max(width, height) / Math.min(width, height));
        Double RATIO_4_3_VALUE = 4.0 / 3.0;
        Double RATIO_16_9_VALUE = 16.0 / 9.0;
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    @Override
    public void onResume() {
        super.onResume();
//check permission
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Shut down our background executor
        cameraExecutor.shutdown();
        displayManager.unregisterDisplayListener(displayListener);
    }


    private void forwardIntent(byte[] data) {
        //Put together the Intent protobuffer.
        Log.i(TAG, "GOING TO SEND URL INTENT with URL: " + getIntent().getDataString());
        final BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder();
        BRAHMAProtocol.Intent.Builder intentProtoBuffer = BRAHMAProtocol.Intent.newBuilder();

        String timeStamp = new SimpleDateFormat("HH.mm.ss.SS").format(new Date());

        intentProtoBuffer.setAction(BRAHMAProtocol.IntentAction.ACTION_SEND);
        if (data != null) {
            BRAHMAProtocol.File.Builder f = BRAHMAProtocol.File.newBuilder();
            f.setFilename("Brahma" + timeStamp + ".jpg");

            Bitmap bit = BitmapFactory.decodeByteArray(data, 0, data.length);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            bit.compress(Bitmap.CompressFormat.JPEG, 70 /* Ignored for PNGs */, blob);
            byte[] bitmapdata = blob.toByteArray();

            f.setData(getByteString(bitmapdata));
            intentProtoBuffer.setFile(f);

        } else {
            Log.i(TAG, "data is null");
        }

        //Set the Request message params and send it off.
        msg.setType(BRAHMAProtocol.Request.RequestType.INTENT);
        msg.setIntent(intentProtoBuffer.build());
//		RemoteServerClient.sendMessage(msg.build());

        Log.i(TAG, "Forwarding intent. Timestamp: " + timeStamp + " " + System.currentTimeMillis());
        Log.i(TAG, "msg >>>>> " + msg.build().toString());
        SessionService.sendMessageStatic(msg.build());

    }

    private ByteString getByteString(byte[] data) {
        try {
            return ByteString.copyFrom(data);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return null;
    }

    public byte[] getBytes(File file){
        //File file = new File(data);
        ByteArrayOutputStream out = null;
        try (FileInputStream in = new FileInputStream(file)) {
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {
                out.write(b, 0, b.length);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert out != null;
        return out.toByteArray();

    }
}


