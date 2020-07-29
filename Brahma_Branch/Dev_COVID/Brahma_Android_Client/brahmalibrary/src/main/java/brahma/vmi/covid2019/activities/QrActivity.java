package brahma.vmi.covid2019.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.services.SessionService;

/**
 * Created by Yuyu on 2017/12/26.
 */

public class QrActivity extends Activity{
    private final static String TAG = "MyQrActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        IntentIntegrator intentIntegrator = new IntentIntegrator(QrActivity.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Scan");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG,"onActivityResult");
        //onActivityResult:當子介面的任務完成之後,返回父介面
        //QRcode result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null){
                //Toast.makeText(QrActivity.this, "Scanner fail", Toast.LENGTH_SHORT).show();
                //回傳null訊息
                forwardIntent("cancel");//回傳資訊給server
                finish();
            }
            else{
                //Toast.makeText(QrActivity.this, result.getContents(), Toast.LENGTH_SHORT).show();
                //send QRcode string to server
                forwardIntent(result.getContents());//回傳資訊給server
                finish();
                //finishActivity(requestCode);
            }
        }else{

        }
    }
    //add by YiWen 20180313
    @Override
    public void onBackPressed() {
        Log.d(TAG,"onBackPressed");
        super.onBackPressed();
    }

    private void forwardIntent(String qrstring){
        Log.d(TAG,"forwardIntent內容");
        if(qrstring != null && qrstring.length() > 0) {
            BRAHMAProtocol.QRstringReq.Builder qrBuilder = BRAHMAProtocol.QRstringReq.newBuilder().setQrstring(qrstring);
            BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder()
                    .setType(BRAHMAProtocol.Request.RequestType.QRstringReq)
                    .setQRstringReq(qrBuilder);
            SessionService.sendMessageStatic(msg.build());//送出訊息給server
            Log.d(TAG, "qrstring = " + qrstring);
        }
    }


}
