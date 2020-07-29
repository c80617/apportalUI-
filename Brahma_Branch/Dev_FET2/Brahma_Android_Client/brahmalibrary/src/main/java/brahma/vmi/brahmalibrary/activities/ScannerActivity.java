package brahma.vmi.brahmalibrary.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.services.SessionService;

/**
 * Created by Yuyu on 2017/12/26.
 */

public class ScannerActivity extends Activity{
    private final static String TAG = "ScannerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        IntentIntegrator intentIntegrator = new IntentIntegrator(ScannerActivity.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);

        Intent scannerIntent = getIntent();

        if(scannerIntent.getStringExtra("ScannerData").equals("ACTION_QRCODE_SCAN")) {
            intentIntegrator.setPrompt("Please move the QRcode to the red line frame.");
            intentIntegrator.setCameraId(0);
            intentIntegrator.setBeepEnabled(false);
            intentIntegrator.setBarcodeImageEnabled(false);
            intentIntegrator.initiateScan();
        }else if(scannerIntent.getStringExtra("ScannerData").equals("ACTION_BARCODE_SCAN")) {
            intentIntegrator.setPrompt("Please move the BARcode to the red line frame.");
            intentIntegrator.setCameraId(0);
            intentIntegrator.setBeepEnabled(false);
            intentIntegrator.setBarcodeImageEnabled(false);
            intentIntegrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //QRcode result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null){
                Toast.makeText(ScannerActivity.this, "Scanner fail", Toast.LENGTH_LONG).show();
            }
            else{
                //Toast.makeText(ScannerActivity.this, result.getContents(), Toast.LENGTH_SHORT).show();
                //send QRcode string to server
                forwardIntent(result.getContents());
            }
        }
        finish();
    }

    private void forwardIntent(String qrstring){
        if(qrstring != null && qrstring.length() > 0) {
            BRAHMAProtocol.QRstringReq.Builder qrBuilder = BRAHMAProtocol.QRstringReq.newBuilder().setQrstring(qrstring);
            BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder()
                    .setType(BRAHMAProtocol.Request.RequestType.QRstringReq)
                    .setQRstringReq(qrBuilder);
            SessionService.sendMessageStatic(msg.build());
            Log.d(TAG, "qrstring = " + qrstring);
        }
    }
}
