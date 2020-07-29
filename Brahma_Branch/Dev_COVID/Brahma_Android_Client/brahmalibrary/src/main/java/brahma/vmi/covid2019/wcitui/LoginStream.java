package brahma.vmi.covid2019.wcitui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;

import brahma.vmi.covid2019.apprtc.AppRTCClient;
import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.common.DatabaseHandler;
import brahma.vmi.covid2019.common.SessionInfo;
import brahma.vmi.covid2019.common.StateMachine;
import brahma.vmi.covid2019.net.EasySSLSocketFactory;
import brahma.vmi.covid2019.netspeed.RoleData;
import brahma.vmi.covid2019.wcitui.devicelog.DeviceInfo;

import static brahma.vmi.covid2019.apprtc.AppRTCClient.vm_platform;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.vm_version;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Latitude;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Longitude;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.context;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.resolution;
import static com.google.common.base.Ascii.toLowerCase;

/**
 * Created by tung on 2017/7/14.
 */

public class LoginStream {
    // private String uriAPI = "http://192.168.1.2/httpPostTest/postTest.php";
    private String uriAPI;

    // service and activity objects
    private StateMachine machine;

    private ConnectionInfo connectionInfo;
    private SessionInfo sessionInfo;
    //role 判斷
    String role;

    private DatabaseHandler dbHandler;
    String device = "";//devices name
    String os = "";//device's os version
    String geolocation = "";//user's location
    String network = "";//user's network type
    DeviceInfo deviceInfo = new DeviceInfo();
    String TAG = "LoginStream";

    /** 「要更新版面」的訊息代碼 */
    protected static final int REFRESH_DATA = 0x00000001;


    /** 建立UI Thread使用的Handler，來接收其他Thread來的訊息 */
    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                // 顯示網路上抓取的資料
                case REFRESH_DATA:
                    String result = null;
                    if (msg.obj instanceof String)
                        result = (String) msg.obj;
                    if (result != null){
                        // 印出網路回傳的文字
                        Log.d("login", result);
                    }
                    break;
            }
        }
    };

    public static class Clerk {
        // -1 表示目前沒有產品
        private int product = -1;

        // 這個方法由生產者呼叫
        public synchronized void setProduct(int product) {
            while(this.product != -1) {
                try {
                    // 目前店員沒有空間收產品，請稍候！
                    wait();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            this.product = product;
            //System.out.printf("生產者設定 (%d)%n", this.product);

            // 通知等待區中的一個消費者可以繼續工作了
            notify();
        }

        // 這個方法由消費者呼叫
        public synchronized int getProduct() {
            while(this.product == -1) {
                try {
                    // 缺貨了，請稍候！
                    wait();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int p = this.product;
            System.out.printf("消費者取走 (%d)%n", this.product);
            this.product = -1;

            // 通知等待區中的一個生產者可以繼續工作了
            notify();

            return p;
        }

    }


    public void loginStream(Context context,String user , String pd, boolean standard, ConnectionInfo connectionInfo , Clerk clerk)
    {
        if(standard) {
            uriAPI = "https://"+connectionInfo.getHost()+":"+connectionInfo.getPort()+"/login";
        }else{
            uriAPI = "https://"+connectionInfo.getHost()+":"+connectionInfo.getPort()+"/auth/checkAuth";
        }
        Log.d("loginStream",uriAPI);
        this.connectionInfo = connectionInfo;
        this.dbHandler = new DatabaseHandler(context);//context = BrahmaMainActivity

        try{
            ConnectivityManager gConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo tInfo = gConnMgr.getActiveNetworkInfo();
            if (tInfo != null) {
                network = toLowerCase(tInfo.getTypeName());
            }
            device = Build.BRAND+" "+Build.MODEL;
            os = "Android "+ Build.VERSION.RELEASE;
            geolocation = context.getResources().getConfiguration().locale.getCountry();
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.e("loginStream","ERROR:"+e.getStackTrace());
        }
        Thread t = new Thread(new LoginStream.sendPostRunnable(user, pd, clerk));
        t.start();
    }

    class sendPostRunnable implements Runnable
    {
        String user = null;
        String pd = null;
        private Clerk clerk;

        // 建構子，設定要傳的字串
        public sendPostRunnable(String user, String pd, Clerk clerk)
        {
            this.user = user;
            this.pd = pd;
            this.clerk = clerk;
        }

        @Override
        public void run()
        {
            String result = sendPostDataToInternet(user, pd, clerk);
            mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
        }
    }

    private String sendPostDataToInternet(String user, String pd, Clerk clerk)
    {
        EasySSLSocketFactory socketFactory = new EasySSLSocketFactory();
        JSONObject jsonRequest = new JSONObject();

        try {
            jsonRequest.put("username",user);
            jsonRequest.put("password",pd);
            jsonRequest.put("type","appstreaming");
            jsonRequest.put("device",device);
            jsonRequest.put("os",os);
            jsonRequest.put("resolution",resolution);
            //jsonRequest.put("geolocation",geolocation);
            jsonRequest.put("network",network);
            jsonRequest.put("longitude",Longitude);
            jsonRequest.put("latitude",Latitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("loginStream","jsonObject:"+jsonRequest.toString());

        try
        {
            // set up HttpParams
            HttpParams params2 = new BasicHttpParams();
            HttpProtocolParams.setVersion(params2, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params2, HTTP.UTF_8);

            // set up ConnectionManager
            SchemeRegistry registry = new SchemeRegistry();//定義使用的協議的類型
            registry.register(new Scheme("https", socketFactory, 3000));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params2, registry);
            // create HttpClient
            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params2);
            HttpPost post = new HttpPost(uriAPI);
            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
            post.setHeader("brahma-lang", Locale.getDefault().getLanguage());
            post.addHeader("brahma-authtoken", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6IjBhMDhkMGU4LWFhYmMtNGE3YS1iYjIyLWUxM2U0OWFhM2ZhYyIsImlzcyI6ImR5bGFuZHktZGV2Iiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNTE3Mzc4NDM0fQ.Ac5EUkcwEX6b1N7FJWPd9HMyL9ULxBlQo_8k_-ClmYf3ZKA_E0vVqYfi5dT3TqXZYcEC93IKZZi0R1DrJAaeDxSwoyix7PILmNfYbmZ43YELfqamnDU52xQvMKgmRwTu0I-Yc5Xc6rTU6_FrfRp5EpWzXt_9lZ5G_6BlRJxYXkQ");

            StringEntity entity = null;
            int responseCode = 0;
            HttpResponse response= null;
            try {
                entity = new StringEntity(jsonRequest.toString());
                post.setEntity(entity);
                response = httpclient.execute(post);
                responseCode = response.getStatusLine().getStatusCode();
                Log.d("IanIan","IanIan responseCode!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+responseCode);
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG,"UnsupportedEncodingException");
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                Log.d(TAG,"ClientProtocolException");
                Toast.makeText(context,"連線被拒絕！請檢查網路狀態",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG,"IOException");
                e.printStackTrace();
                clerk.setProduct(5);//失敗
                return "error";
            }

            if (responseCode == 200){
                // get JSON object
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();

                JSONObject jsonResponse = new JSONObject(out.toString());
                Log.d("Login","IanIan msg!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+jsonResponse.toString());
                if(jsonResponse.has("msg")){
                    clerk.setProduct(3);//失敗
                }else {
                    // get session info
                    String token = jsonResponse.getJSONObject("sessionInfo").getString("token");
                    if(jsonResponse.getJSONObject("sessionInfo").has("role")) {
                        role = jsonResponse.getJSONObject("sessionInfo").getString("role");
                    }
                    if(jsonResponse.getJSONObject("sessionInfo").has("brahmaUsername")) {
                        deviceInfo.setUser(jsonResponse.getJSONObject("sessionInfo").getString("brahmaUsername"));
                        AppRTCClient.brahmaUsername = jsonResponse.getJSONObject("sessionInfo").getString("brahmaUsername");
                    }
                    if(jsonResponse.getJSONObject("sessionInfo").has("vm_platform")) {
                        vm_platform = jsonResponse.getJSONObject("sessionInfo").getString("vm_platform");
                        Log.d("YiWen","vm_platform:"+vm_platform);
                    }
                    if (jsonResponse.getJSONObject("sessionInfo").has("vm_version")) {
                        vm_version = jsonResponse.getJSONObject("sessionInfo").getString("vm_version");
                    }
                    if(jsonResponse.getJSONObject("sessionInfo").has("role")) {
                        role = jsonResponse.getJSONObject("sessionInfo").getString("role");
                    }
                    if(jsonResponse.getJSONObject("sessionInfo").has("role")) {
                        AppRTCClient.vm_ip = jsonResponse.getJSONObject("sessionInfo").getString("vm_ip");
                    }
                    long expires = new Date().getTime() + (1000 * jsonResponse.getJSONObject("sessionInfo").getInt("maxLength"));
                    String host = jsonResponse.getJSONObject("server").getString("host");
                    String port = jsonResponse.getJSONObject("server").getString("port");
                    JSONObject webrtc = jsonResponse.getJSONObject("webrtc");
                    sessionInfo = new SessionInfo(token, expires, host, port, webrtc);
                    RoleData roleData = new RoleData();
                    roleData.setRole(role);
                    if (sessionInfo.getSignalingParams() != null) {
                        dbHandler.updateSessionInfo(connectionInfo, sessionInfo);
                        clerk.setProduct(1);
                    }else
                        clerk.setProduct(4);

                }
					/* 取出回應字串 */
                String strResult = "";
                Log.d("Login", "login susses");
                // 回傳回應字串
                return strResult;
            }else if(responseCode == 404){
                clerk.setProduct(2);
                Log.d("Login", "login not susses");
                String strResult = "";
                // 回傳回應字串
                return strResult;
            }else if (responseCode == 400 || responseCode == 401) { // "Unauthorized", code for PASSWORD_CHANGE_FAIL
                clerk.setProduct(3);
                Log.d("Login", "login FFFFFF");
                String strResult = "";
                // 回傳回應字串
                return strResult;
            }


        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


}
