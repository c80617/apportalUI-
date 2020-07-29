package brahma.vmi.brahmalibrary.client;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.services.SessionService;

public class MediaRecorderHandler {
    private MediaRecorder mMediaRecorder;
    private String mSavePath;
    private String mCurrentFilePath;
    public static final String TAG = "MediaRecorderHandler";
    private File file;

    MediaRecorderHandler(Context context) {
        Log.i(TAG, "MediaRecorderHandler");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath(), "Brahma.amr");
        } else {
            file = new File(context.getFilesDir(), "/Brahma.amr");
        }
        Log.d(TAG, "file path :" + file.getPath());
        if (file.exists()){
            boolean result2 = file.delete();
            Log.d(TAG, "MediaRecorderHandler delete: "+result2);
        }
        try {
            boolean result3 = file.createNewFile();
            Log.d(TAG, "MediaRecorderHandler createNewFile: "+result3);
        } catch (IOException e) {
            Log.i(TAG, "create file failed");
            throw new IllegalStateException("create file failed" + file.toString());
        }
    }

    void startRecord() {
        Log.i(TAG,"startRecord");
        try {
            mMediaRecorder = new MediaRecorder();
            File file = new File(mSavePath);
            mCurrentFilePath = file.getAbsolutePath();
            // 设置录音文件的保存位置
            mMediaRecorder.setOutputFile(mCurrentFilePath);
            // 设置录音的来源（从哪里录音）
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置录音的保存格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            // 设置录音的编码
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopAndRelease() {
        Log.i(TAG,"stopAndRelease");
        forwardIntent(getBytes(file));
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                // TODO 如果当前java状态和jni里面的状态不一致，
                //e.printStackTrace();
                mMediaRecorder = null;
                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.stop();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void cancel() {
        this.stopAndRelease();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            Boolean result = file.delete();
            Log.d(TAG, "MediaRecorderHandler delete: "+result);
            mCurrentFilePath = null;
        }
    }

    private void forwardIntent(byte[] data) {
        Log.d(TAG,"forwardIntent");

        final BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder();
        BRAHMAProtocol.Intent.Builder intentProtoBuffer = BRAHMAProtocol.Intent.newBuilder();

        String timeStamp = new SimpleDateFormat("HH.mm.ss.SS").format(new Date());

        intentProtoBuffer.setAction(BRAHMAProtocol.IntentAction.ACTION_AUDIO_STOP);
        if(data != null) {
            BRAHMAProtocol.File.Builder f = BRAHMAProtocol.File.newBuilder();
            f.setFilename("Brahma.wav");
            f.setData(getByteString(data));
            intentProtoBuffer.setFile(f);
        }
        //Set the Request message params and send it off.
        msg.setType(BRAHMAProtocol.Request.RequestType.INTENT);
        msg.setIntent(intentProtoBuffer.build());
//		RemoteServerClient.sendMessage(msg.build());

        Log.i(TAG, "Forwarding intent. Timestamp: " + timeStamp + " " + System.currentTimeMillis());
        Log.d(TAG,"msg:"+msg.build().toString());
        Log.d(TAG,"file:"+ file.getName());
        Log.d(TAG,"file:"+ file.getPath());
        SessionService.sendMessageStatic(msg.build());
    }

    private ByteString getByteString(byte[] data) {
        try {
            return ByteString.copyFrom(data);
        } catch(Exception ignored) {}
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