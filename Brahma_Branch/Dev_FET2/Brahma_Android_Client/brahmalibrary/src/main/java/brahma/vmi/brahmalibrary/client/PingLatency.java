package brahma.vmi.brahmalibrary.client;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.common.ConnectionInfo;
import brahma.vmi.brahmalibrary.common.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.performanceToken;
import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.user_id;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.normalPort;

/**
 * Created by tung on 2017/7/14.
 */

public class PingLatency {

    /**
     * 「要更新版面」的訊息代碼
     */
    protected static final int REFRESH_DATA = 0x00000001;
    public static int fps = 0;
    public static int latency = 0;
    public static int bandwidth = 0;
    public static String TAG = "PingLatency";
    int avgFPS = 0;
    int avgLatency = 0;
    int avgBandwidth = 0;
    /**
     * 建立UI Thread使用的Handler，來接收其他Thread來的訊息
     */
//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                // 顯示網路上抓取的資料
//                case REFRESH_DATA:
//                    String result = null;
//                    if (msg.obj instanceof String)
//                        result = (String) msg.obj;
//                    if (result != null) {
//                        // 印出網路回傳的文字
//                        //Log.d("REBOOT", result);
//                    }
//                    break;
//            }
//        }
//    };
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;

    public void PingSend(String ping, ConnectionInfo connectionInfo) {

        SimpleDateFormat sdf = new SimpleDateFormat("ss");
        String date = sdf.format(new java.util.Date());
        int ss = Integer.parseInt(date);
        if (ss % 5 == 0) {
            new PingSendTask().execute(ping);

        }
    }

    private void initSSLSocketFactory() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        KeyStore localTrustStore = null;
        try {
            localTrustStore = KeyStore.getInstance("BKS");
            assert application != null;
            InputStream in = application.getResources().openRawResource(R.raw.mykeystore);
            try {
                localTrustStore.load(in, Constants.TRUSTSTORE.toCharArray());
                in.close();
            } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(localTrustStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    class PingSendTask extends AsyncTask<String, String, String> {
        String uriAPI;
        JSONObject json_request = null;

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        @Override
        protected void onPreExecute() {
            uriAPI = "https://" + normalIP + ":" + normalPort + "/clients/performance";
            initSSLSocketFactory();
        }

        @Override
        protected String doInBackground(String... ping) {
            json_request = new JSONObject();
            try {
                json_request.put("user_id", user_id);
                json_request.put("latency", Integer.parseInt(ping[0]));
                json_request.put("bandwidth", bandwidth);
                json_request.put("fps", fps);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return s.equals(sslSession.getPeerHost());
                        }
                    })
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , json_request.toString());

            if (!performanceToken.isEmpty()) {
                Request request = new Request.Builder()
                        .url(uriAPI)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("brahma-lang", Locale.getDefault().getLanguage())
                        .addHeader("brahma-authtoken", performanceToken)
                        .post(requestBody)
                        .build();

                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "[PingSendTask] error:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        assert response.body() != null;
                        String jsonData = response.body().string();
                        int responseCode = response.code();
                        Log.i(TAG, "[PingSendTask] response body :" + jsonData);
                        Log.i(TAG, "[PingSendTask] response code :" + responseCode);

                        if (avgBandwidth == 0 && avgFPS == 0 && avgLatency == 0) {
                            avgBandwidth = bandwidth;
                            avgFPS = fps;
                            avgLatency = latency;

                        } else {
                            avgBandwidth = (avgBandwidth + bandwidth) / 2;
                            avgFPS = (avgFPS + fps) / 2;
                            avgLatency = (avgLatency + Integer.parseInt(ping[0])) / 2;
                        }
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
//            mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
        }
    }
}
