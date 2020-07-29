package brahma.vmi.brahmalibrary.activities;

import android.app.Activity;
import android.graphics.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import brahma.vmi.brahmalibrary.R;

public class MediaRecorderActivity extends Activity implements View.OnClickListener {

    Button record, stop;
    // 系統視訊檔案
    File viodFile;
    MediaRecorder mRecorder;
    // 顯示視訊的SurfaceView
    SurfaceView sView;
    // 記錄是否正在進行錄製
    boolean isRecording = false;

    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        record = (Button) findViewById(R.id.record);
        stop = (Button) findViewById(R.id.stop);
        sView = (SurfaceView) findViewById(R.id.dView);
        // stop按鈕不可用
        stop.setEnabled(false);

        // 設定Surface不需要維護自己的緩衝區
        sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 設定解析度
        sView.getHolder().setFixedSize(320, 280);
        // 設定該元件不會讓螢幕自動關閉
        sView.getHolder().setKeepScreenOn(true);

//        record.setOnClickListener(this);
//        stop.setOnClickListener(this);

    }

    public void onClick_record(View v) {
        Log.d("videoRecord","onClick_record");
        try {
            // 建立MediaPlayer物件
            mRecorder = new MediaRecorder();
            mRecorder.reset();

            //要在例項化MediaRecorder之前就解鎖好相機
//            mCamera.unlock();
            mRecorder = new MediaRecorder();
//           /* camera = Camera.open();
//            camera.unlock();
//            camera.setDisplayOrientation(0);
//            mRecorder.setCamera(camera);*/
            // 建立儲存錄制視訊的視訊檔案
            viodFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myVideo.mp4");

            if (!viodFile.exists())
                viodFile.createNewFile();

            // 設定從麥克風採集聲音
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 設定從攝像頭採集影象
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 設定視訊、音訊的輸出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            // 設定音訊的編碼格式、
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            // 設定影象編碼格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mRecorder.setOrientationHint(90);
            // 設定視訊錄製的解析度。必須放在設定編碼和格式的後面，否則報錯
//            mRecorder.setVideoSize(1920, 1080);
            // 設定錄製的視訊幀率。必須放在設定編碼和格式的後面，否則報錯
//            mRecorder.setVideoFrameRate(30);
//            mRecorder.setVideoEncodingBitRate(1024*1024*20);
            mRecorder.setOutputFile(viodFile.getAbsolutePath());
            // 指定SurfaceView來預覽視訊
            mRecorder.setPreviewDisplay(sView.getHolder().getSurface());
            mRecorder.prepare();
            // 開始錄製
            mRecorder.start();
            // 讓record按鈕不可用
            record.setEnabled(false);
            // 讓stop按鈕可用
            stop.setEnabled(true);
            isRecording = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick_stop(View v) {
        // 如果正在錄製
        if (isRecording) {
            // 停止錄製
            mRecorder.stop();
            // 釋放資源
            mRecorder.release();
            mRecorder = null;
            // 讓record按鈕可用
            record.setEnabled(true);
            // 讓stop按鈕不可用
            stop.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
