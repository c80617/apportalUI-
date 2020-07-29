package brahma.vmi.brahmalibrary.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.services.SessionService;

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
