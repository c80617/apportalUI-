package brahma.vmi.covid2019.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.services.SessionService;

/**
 * Created by Yuyu on 2017/12/26.
 */

public class BarActivity extends Activity{
    private final static String TAG = "BarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        IntentIntegrator intentIntegrator = new IntentIntegrator(BarActivity.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
        intentIntegrator.setPrompt("Scan");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //onActivityResult:當子介面的任務完成之後,返回父介面
        //Barcode result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null){
                //Toast.makeText(BarActivity.this, "Scanner fail~~", Toast.LENGTH_SHORT).show();
                //回傳null訊息
                Log.d("YiWen -> Barcode","Scanner failed");
                forwardIntent("cancel");//回傳資訊給server
                finish();
            }
            else{
                //Toast.makeText(QrActivity.this, result.getContents(), Toast.LENGTH_SHORT).show();
                //send QRcode string to server
                forwardIntent(result.getContents());//回傳資訊給server
            }
        }
        finish();
    }

    //add by YiWen 20180313
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void forwardIntent(String barstring){
        Log.d("YiWen -> Barcode","forwardIntent內容");
        if(barstring != null && barstring.length() > 0) {
            BRAHMAProtocol.QRstringReq.Builder qrBuilder = BRAHMAProtocol.QRstringReq.newBuilder().setQrstring(barstring);
            BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder()
                    .setType(BRAHMAProtocol.Request.RequestType.QRstringReq)
                    .setQRstringReq(qrBuilder);
            SessionService.sendMessageStatic(msg.build());//送出訊息給server
            Log.d(TAG, "barstring = " + barstring);
        }
    }
}
