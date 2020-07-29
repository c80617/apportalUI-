package brahma.vmi.covid2019.client;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.net.EasySSLSocketFactory;

import static brahma.vmi.covid2019.apprtc.AppRTCClient.performanceToken;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.user_id;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalPort;

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

    int avgFPS = 0;
    int avgLatency = 0;
    int avgBandwidth = 0;

    int totalFPS = 0;
    int totalLatency = 0;
    int totalBandwidth = 0;


    /**
     * 建立UI Thread使用的Handler，來接收其他Thread來的訊息
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 顯示網路上抓取的資料
                case REFRESH_DATA:
                    String result = null;
                    if (msg.obj instanceof String)
                        result = (String) msg.obj;
                    if (result != null) {
                        // 印出網路回傳的文字
                        //Log.d("REBOOT", result);
                    }
                    break;
            }
        }
    };


    public void PingSend(String ping, ConnectionInfo connectionInfo) {

        SimpleDateFormat sdf = new SimpleDateFormat("ss");
        String date = sdf.format(new java.util.Date());
        int ss = Integer.parseInt(date);
        //Log.d("PingSend", "date ss："+date);
        if (ss % 5 == 0) {
            new PingSendTask().execute(ping);
//                String result = sendPostDataToInternet(strTxt);

        }

//        Log.d("PingSend", uriAPI);

//        Thread t = new Thread(new PingLatency.sendPostRunnable(ping));
//        t.start();
    }

    //    private String sendPostDataToInternet(String strTxt) {
//
//        JSONObject json_request = new JSONObject();
//        try {
//            json_request.put("user_id", user_id);//ing
//            json_request.put("latency", Integer.parseInt(strTxt));//ing
//            json_request.put("bandwidth", bandwidth);//float
//            json_request.put("fps", fps);//int
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
////        Log.d("PingSend", "json_request：" + json_request.toString());
//
//        try {
//            // set up HttpParams
//            HttpParams params2 = new BasicHttpParams();
//            HttpProtocolParams.setVersion(params2, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(params2, HTTP.UTF_8);
//            EasySSLSocketFactory socketFactory;
//            socketFactory = new EasySSLSocketFactory();
//            // set up ConnectionManager
//            SchemeRegistry registry = new SchemeRegistry();//定義使用的協議的類型
////            registry.register(new Scheme(proto, useSSL ? sslConfig.getSocketFactory() : PlainSocketFactory.getSocketFactory(), 1234));
//            registry.register(new Scheme("https", socketFactory, Integer.valueOf(normalPort)));
//            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params2, registry);
//            // create HttpClient
//            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params2);
//            HttpPost post = new HttpPost(uriAPI);
//            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
//            post.setHeader("brahma-lang", Locale.getDefault().getLanguage());
//            post.addHeader("brahma-authtoken", performanceToken);
//            StringEntity entity = null;
//            int responseCode = 0;
//            HttpResponse response = null;
//            try {
//                entity = new StringEntity(json_request.toString());
//                post.setEntity(entity);
//                response = httpclient.execute(post);
//                responseCode = response.getStatusLine().getStatusCode();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            if (avgBandwidth == 0 && avgFPS == 0 && avgLatency == 0) {
//                avgBandwidth = bandwidth;
//                avgFPS = fps;
//                avgLatency = latency;
//
//            } else {
//                avgBandwidth = (avgBandwidth + bandwidth) / 2;
//                avgFPS = (avgFPS + fps) / 2;
//                avgLatency = (avgLatency + Integer.parseInt(strTxt)) / 2;
//            }
////            Log.d("AVERAGE_TEST", "(avgBandwidth:" + avgBandwidth+" , avgFPS:"+avgFPS+" ,avgLatency:"+avgLatency+")");
//
//            if (responseCode == 200) {
//                String strResult = EntityUtils.toString(response.getEntity());
////                Log.d("PingSend", "strResult:" + strResult);
//                return strResult;
//            } else {
////                Log.d("PingSend", "responseCode:" + responseCode);
////                Log.d("PingSend", "response:" + response.getStatusLine());
//                return null;
//            }
//        } catch (Exception e) {
////            Log.d("PingSend", "Send ping not susses");
//            e.printStackTrace();
//        }
//        return null;
//    }

//    class sendPostRunnable implements Runnable {
//        String strTxt = null;
//
//        public sendPostRunnable(String strTxt) {
//            this.strTxt = strTxt;
//        }
//
//        @Override
//        public void run() {
//
//            SimpleDateFormat sdf = new SimpleDateFormat("ss");
//            String date = sdf.format(new java.util.Date());
//            int ss = Integer.parseInt(date);
//            //Log.d("PingSend", "date ss："+date);
//            if (ss % 5 == 0) {
//                String result = sendPostDataToInternet(strTxt);
//                mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
//            }
//        }
//    }

    class PingSendTask extends AsyncTask<String, String, String> {
        String uriAPI;
        JSONObject json_request = null;

        //AsyncTask 執行前的準備工作，例如畫面上顯示進度表
        @Override
        protected void onPreExecute() {
            uriAPI = "https://" + normalIP + ":" + normalPort + "/clients/performance";
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

            try {
                // set up HttpParams
                HttpParams params2 = new BasicHttpParams();
                HttpProtocolParams.setVersion(params2, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params2, HTTP.UTF_8);
                EasySSLSocketFactory socketFactory;
                socketFactory = new EasySSLSocketFactory();
                // set up ConnectionManager
                SchemeRegistry registry = new SchemeRegistry();//定義使用的協議的類型
//            registry.register(new Scheme(proto, useSSL ? sslConfig.getSocketFactory() : PlainSocketFactory.getSocketFactory(), 1234));
                registry.register(new Scheme("https", socketFactory, Integer.valueOf(normalPort)));
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params2, registry);
                // create HttpClient
                DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params2);
                HttpPost post = new HttpPost(uriAPI);
                post.setHeader(HTTP.CONTENT_TYPE, "application/json");
                post.setHeader("brahma-lang", Locale.getDefault().getLanguage());
                post.addHeader("brahma-authtoken", performanceToken);
                StringEntity entity = null;
                int responseCode = 0;
                HttpResponse response = null;
                try {
                    entity = new StringEntity(json_request.toString());
                    post.setEntity(entity);
                    response = httpclient.execute(post);
                    responseCode = response.getStatusLine().getStatusCode();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (avgBandwidth == 0 && avgFPS == 0 && avgLatency == 0) {
                    avgBandwidth = bandwidth;
                    avgFPS = fps;
                    avgLatency = latency;

                } else {
                    avgBandwidth = (avgBandwidth + bandwidth) / 2;
                    avgFPS = (avgFPS + fps) / 2;
                    avgLatency = (avgLatency + Integer.parseInt(ping[0])) / 2;
                }
//                Log.d("AVERAGE_TEST", "(avgBandwidth:" + avgBandwidth + " , avgFPS:" + avgFPS + " ,avgLatency:" + avgLatency + ")");

                if (responseCode == 200) {
                    String strResult = EntityUtils.toString(response.getEntity());
//                    Log.d("PingSend", "strResult:" + strResult);
                    return strResult;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
        }
    }
}
