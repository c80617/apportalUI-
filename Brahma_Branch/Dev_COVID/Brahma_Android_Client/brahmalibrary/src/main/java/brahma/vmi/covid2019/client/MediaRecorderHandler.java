package brahma.vmi.covid2019.client;

import android.media.MediaRecorder;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.services.SessionService;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.context;


public class MediaRecorderHandler {
    private MediaRecorder mMediaRecorder;
    private String mSavePath;
    private String mCurrentFilePath;
    public static final String TAG = "MediaRecorderHandler";
    private File file;

    public MediaRecorderHandler() {
        Log.i(TAG,"MediaRecorderHandler");
        mSavePath = context.getFilesDir()+ "/Brahma.amr";
        Log.i(TAG,"mSavePath:"+mSavePath);
        file = new File(mSavePath);

        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.i(TAG,"create file failed");
            throw new IllegalStateException("create file failed" + file.toString());
        }

    }
    /**
     * 开始录音
     */
    public void startRecord() {
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

    /**
     * 停止录音
     */
    public void stopAndRelease() {
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
            file.delete();
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
        } catch(Exception e) {}
        return null;
    }

    public byte[] getBytes(File file){
        //File file = new File(data);
        ByteArrayOutputStream out = null;
        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {

                out.write(b, 0, b.length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] s = out.toByteArray();
        return s;

    }
}
