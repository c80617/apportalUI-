package brahma.vmi.covid2019.client;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.net.EasySSLSocketFactory;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.wcitui.BrahmaMainActivity;

import static brahma.vmi.covid2019.apprtc.AppRTCClient.appListObject;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.performanceToken;
import static brahma.vmi.covid2019.apprtc.AppRTCClient.user_id;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalIP;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.normalPort;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Latitude;
import static brahma.vmi.covid2019.wcitui.WelcomeActivity.Longitude;

/**
 * Created by tung on 2017/4/24.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /** 是否开启日志输出,在Debug状态下开启,
     * 在Release状态下关闭以提示程序性能
     * */
    public static final boolean DEBUG = true;
    private static final String TAG = "CrashHandler";
    /** 系统默认的UncaughtException处理类 */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /** CrashHandler实例 */
    private static CrashHandler INSTANCE;
    /** 程序的Context对象 */
//    private Context mContext;
    /** 保证只有一个CrashHandler实例 */
    private CrashHandler() {}
    /** 获取CrashHandler实例 ,单例模式*/
    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     * 获取系统默认的UncaughtException处理器,
     * 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx
     */
    public void init(Context ctx) {
//        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            android.os.Process.killProcess(android.os.Process.myPid());

        }
    }

    /**
     * 自定义错误处理,收集错误信息
     * 发送错误报告等操作均在此完成.
     * 开发者可以根据自己的情况来自定义异常处理逻辑
     * @return
     * true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
//        final String msg = ex.getLocalizedMessage();
        final StackTraceElement[] stack = ex.getStackTrace();
        final String message = ex.getMessage();
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
//                Looper.prepare();
//                Toast.makeText(mContext, "程序出错啦:" + message, Toast.LENGTH_LONG).show();
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String fileName = "crash-" + time  + ".log";
                File file = new File(Environment.getExternalStorageDirectory(), fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file,true);
                    fos.write(message.getBytes());
                    for (int i = 0; i < stack.length; i++) {
                        fos.write(stack[i].toString().getBytes());
                    }
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                }
                //socket
                String result = sendCrashPostDataToInternet();


//                Looper.loop();
            }

        }.start();
        return false;
    }

    private String sendCrashPostDataToInternet() {
        String uriAPI = "https://119.31.180.2:9568/login";
        Log.d(TAG, "sendPostDataToInternet uri:" + uriAPI);
        EasySSLSocketFactory socketFactory;
        socketFactory = new EasySSLSocketFactory();
        JSONObject http_data = new JSONObject();
        try {
            http_data.put("test", "test");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "http_data:" + http_data.toString());

        int returnVal;
        try {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", socketFactory, 9568));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params);
            HttpPost post = new HttpPost(uriAPI);
            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
            post.setHeader("brahma-lang", Locale.getDefault().getLanguage());
            StringEntity entity = null;

            try {
                entity = new StringEntity(http_data.toString());
                post.setEntity(entity);
                HttpResponse response = httpclient.execute(post);
                int responseCode = response.getStatusLine().getStatusCode();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                Log.d(TAG, "responseCode:" + responseCode);
                Log.d(TAG, "response message:" + out.toString());
                JSONObject jsonResponse = new JSONObject(out.toString());
                String msg = jsonResponse.getString("msg");
                Log.d(TAG, "get json message:" + msg);
            } catch (JSONException e) {
            } catch (SSLHandshakeException e) {
            } catch (SSLException e) {
            } catch (IllegalArgumentException e) {
            } catch (IOException e) {
            }
        } catch (Exception e) {

        }
        return null;
    }

}
