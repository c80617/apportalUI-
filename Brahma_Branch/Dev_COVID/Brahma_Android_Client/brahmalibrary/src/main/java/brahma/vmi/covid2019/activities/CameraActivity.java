package brahma.vmi.covid2019.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.services.SessionService;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = "CameraActivity";
    private SurfaceView sfv_preview;
    private SurfaceHolder mSurfaceHolder;
    private Button btn_take;
    private Camera camera = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        // 全螢幕
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 直式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        bindViews();
    }

    private void bindViews() {
        sfv_preview = (SurfaceView) findViewById(R.id.sfv_preview);

        mSurfaceHolder = sfv_preview.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        btn_take = (Button) findViewById(R.id.btn_take);
        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.autoFocus(CameraActivity.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //保存文件的方法
    private String saveFile(byte[] bytes) {
        try {
            File file = File.createTempFile("img", "");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //Ian
    public Bitmap rotationBitmap(Bitmap picture) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (data != null) {
                        forwardIntent(data);
                        finish();
                    } else {
                        Toast.makeText(CameraActivity.this, "保存照片失败", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

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

        //>>>Add by Yuyu 20171225
        //Intent intent = new Intent();
        //intent.setClass(this, BrahmaMainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//去除先前運行的Activity(ConnectionList)
        //intent.putExtra("connectionID", SessionService.getConnectionID());
        //startActivity(intent);

        Log.i(TAG, "Forwarding intent. Timestamp: " + timeStamp + " " + System.currentTimeMillis());
        Log.i(TAG, "msg >>>>> " + msg.build().toString());
        SessionService.sendMessageStatic(msg.build());

    }

    private ByteString getByteString(byte[] data) {
        try {
            return ByteString.copyFrom(data);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();

        if (Build.VERSION.SDK_INT >= 8)
            camera.setDisplayOrientation(90);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            camera.release();
            camera = null;
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        camera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        //Add by Yuyu 20171225
        //AppRTCActivity.setOpenCamera(false);
    }


}
