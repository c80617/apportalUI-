//package brahma.vmi.brahmalibrary.services;
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationChannelGroup;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.Icon;
//import android.os.Binder;
//import android.os.Build;
//import android.os.IBinder;
//import android.util.Base64;
//import android.util.Log;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpVersion;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.conn.ClientConnectionManager;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.scheme.SchemeRegistry;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.params.HttpParams;
//import org.apache.http.params.HttpProtocolParams;
//import org.apache.http.protocol.HTTP;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import brahma.vmi.brahmalibrary.R;
//import brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity;
//
//import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.context;
//
//
//public class NotificatinService extends Service {
//    public static final String TAG = "notificaitonAlert";
//    private boolean running = false;
//    Timer timer;
//    TimerTask timerTask;
//    String username;
//    String host;
//    String port;
//
//    private NotificatinService.MyBinder mBinder = new NotificatinService.MyBinder();
//
//    @Override
//    public void onCreate() {
//        //當app退到背景的時候才開始執行
//        super .onCreate();
//        Log.d(TAG, "onCreate() executed");
////        if (Build.VERSION.SDK_INT >= 26) {
//
//        startForeground(1, new Notification.Builder(this).build());
//
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand() executed");
//        //service's intent
//        username = intent.getStringExtra("notificationUsername");
//        host = intent.getStringExtra("notificationHost");
//        port = intent.getStringExtra("notificationPort");
//
//        Log.d(TAG, "onStartCommand() executed, notificationUsername:"+username+", notificationHost:"+host+", notificationPort:"+port);
//        startTimer();
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stoptimertask();
//        Log.d(TAG, "onDestroy() executed");
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//    class MyBinder extends Binder {
//
//    }
//    private void startTimer() {
//        timer = new Timer();
//        initializeTimerTask();
//        timer.schedule(timerTask,5000,60000);
//    }
//
//    public void initializeTimerTask() {
//        timerTask = new TimerTask() {
//            public void run() {
//                if(username != null && host != null && port !=null)
//                    sendPostDataToInternet(username,host,port);//example
//                else
//                    Log.d(TAG, "something is null!");
//                //Log.d(TAG, "timer task:"+String.valueOf(System.currentTimeMillis()));
//
//            }
//        };
//    }
//
//    public void stoptimertask() {
//        if (timer != null) {
//            timer.cancel();
//            timer = null;
//        }
//    }
//    private void sendPostDataToInternet(String user, String host,String port)
//    {
//        //取得時間
//        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
//        String notificationTime=format.format(new Date());
//        //Log.d("notificaitonAlert", "time:"+notificationTime);
//
//        String encode = notificationTime;
//        //String encode = "2018/5/7 12:00";
//        String uriAPI = "https://"+host+":"+port+"/mobile/notification?username="+user+"&from=";
//        String urlEncode = null;
//        try {
//            urlEncode = URLEncoder.encode(encode, "UTF-8");
//            //Log.d("notificaitonAlert","urlEncode="+urlEncode);
//
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        uriAPI +=urlEncode;
//        Log.d("notificaitonAlert","uriAPI="+uriAPI);
//        //需要先判斷user是否為特殊角色,可以取得notification alert
//        //取得socketFactory
//        EasySSLSocketFactory socketFactory = new EasySSLSocketFactory();
//
//        try{
//            HttpParams params2 = new BasicHttpParams();
//            HttpProtocolParams.setVersion(params2, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(params2, HTTP.UTF_8);
//
//            SchemeRegistry registry = new SchemeRegistry();//定義使用的協議的類型
//            registry.register(new Scheme("https", socketFactory, 3000));
//            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params2, registry);
//
//            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params2);
//
//            HttpUriRequest get = new HttpGet(uriAPI);
//            get.setHeader(HTTP.CONTENT_TYPE, "application/json");
//            get.setHeader("brahma-lang", Locale.getDefault().getLanguage());
//
//            get.addHeader("brahma-authtoken", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6IjBhMDhkMGU4LWFhYmMtNGE3YS1iYjIyLWUxM2U0OWFhM2ZhYyIsImlzcyI6ImR5bGFuZHktZGV2Iiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNTE3Mzc4NDM0fQ.Ac5EUkcwEX6b1N7FJWPd9HMyL9ULxBlQo_8k_-ClmYf3ZKA_E0vVqYfi5dT3TqXZYcEC93IKZZi0R1DrJAaeDxSwoyix7PILmNfYbmZ43YELfqamnDU52xQvMKgmRwTu0I-Yc5Xc6rTU6_FrfRp5EpWzXt_9lZ5G_6BlRJxYXkQ");
//
//            int responseCode = 0;
//            HttpResponse response= null;
//            try {
//                response = httpclient.execute(get);
//                responseCode = response.getStatusLine().getStatusCode();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            /* 若狀態碼為200 ok */
//            if (responseCode == 200){
//                // get JSON object
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                response.getEntity().writeTo(out);
//                out.close();
//                Log.d("notificaitonAlert","response:"+out.toString());
//
//                //將取得的notification info保存,並且放入notification alert中顯示
//                //loop執行應該取得的info
//                JSONObject jsonResponse = new JSONObject(out.toString());
//                JSONArray array = jsonResponse.getJSONArray("data");
//                for(int i=0;i < array.length();i++){
//                    JSONObject jsonObject = array.getJSONObject(i);
//                    String vm_ip = jsonObject.getString("vm_ip");
//                    String title = jsonObject.getString("title");
//                    String text = jsonObject.getString("text");
//                    String username = jsonObject.getString("username");
//                    String packagename = jsonObject.getString("package_name");
//                    String appName = jsonObject.getString("app_name");
//                    String appIcon = jsonObject.getString("app_icon");
//                    String time = jsonObject.getString("time");
//                    String timestamp = jsonObject.getString("timestamp");
//                    Log.d("notificaitonAlert","======第" + i + "筆======");
////                    Log.d("notificaitonAlert", "vm_ip:" + vm_ip +
////                            "\n,title:" + title +
////                            "\n, text:" + text +
////                            "\n, packagename:" + packagename +
////                            "\n, appName:" + appName +
////                            "\n, time:" + time +
////                            "\n, timestamp:" + timestamp +
////                            "\n, appIcon:" + appIcon );
//                    setNotification(title,text,username,packagename,appName,appIcon,time,timestamp);
//                }
//            }else if(responseCode == 404){
//                //error
//            }else if (responseCode == 400 || responseCode == 401) { // "Unauthorized", code for PASSWORD_CHANGE_FAIL
//                //error
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void setNotification(String nTitle,String nContent,String username,String packageName,String appName,String appIcon,String time,String timestamp) {
//
//        Log.d("notificaitonAlert", "username:" + username +
//                "\n, title:" + nTitle +
//                "\n, text:" + nContent +
//                "\n, packagename:" + packageName +
//                "\n, appName:" + appName +
//                "\n, time:" + time +
//                "\n, timestamp:" + timestamp +
//                "\n, appIcon:" + appIcon );
//
//            //毫秒转换为日期
//            long now = System.currentTimeMillis();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(now);
//            //System.out.println(now + " = " + nowTime);
//
//            Bitmap smallBitmap;
//            Bitmap largeBitmap;
//            if(appIcon.length() > 1){
//                //base64
//                appIcon = appIcon.replaceAll("jpeg", "png");
//                String[] icon = appIcon.replaceAll("\\[data:image/png;base64,", "").replaceAll("\\]", "").split("data:image/png;base64,");
//                byte[] decode = Base64.decode(icon[1],Base64.DEFAULT);
//                smallBitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
//                largeBitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
//
//            }else{
//                smallBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brahma_app_icon_white);
//                largeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brahma_app_icon_white);
//            }
//            if(appName.length() > 1){
//                nContent = appName +" : "+nContent;
//            }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //String id  = "Channel";
//            String id  = packageName;
//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent notifyIntent = new Intent(this, BrahmaMainActivity.class);
//            notifyIntent.setAction("NOTIFICATION_ACTION");
//            notifyIntent.putExtra("openPackage",packageName);
//            notifyIntent.putExtra("openUsername",username);
//            notifyIntent.putExtra("openAPP",appName);
//            notifyIntent.putExtra("openHost",host);
//            notifyIntent.putExtra("openPort",port);
//            notifyIntent.putExtra("timestamp",timestamp);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            //notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent appIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            //PendingIntent appIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
//
//            NotificationChannel channel = new NotificationChannel(id,"Channel",NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("APPortal Notification");
//            channel.enableLights(true);
//            channel.enableVibration(true);
//            mNotificationManager.createNotificationChannel(channel);
//            mNotificationManager.createNotificationChannelGroup(new NotificationChannelGroup(timestamp, packageName));
//            Notification.Builder builder =
//                    new Notification.Builder(this)
//                            .setSmallIcon(Icon.createWithBitmap(smallBitmap))
//                            .setLargeIcon(Icon.createWithBitmap(largeBitmap))
//                            .setContentTitle(nTitle)
//                            .setContentText(packageName)
//                            .setContentIntent(appIntent)
//                            .setGroup(packageName)
//                            .setWhen(System.currentTimeMillis())
//                            .setAutoCancel(true)
//                            .setChannelId(id);
//            mNotificationManager.notify((int)System.currentTimeMillis(), builder.build());
//        }else {
//            //android 8.0之前
//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            Intent notifyIntent = new Intent(this, BrahmaMainActivity.class);
//            notifyIntent.setAction("NOTIFICATION_ACTION");
//            notifyIntent.putExtra("openPackage",packageName);
//            notifyIntent.putExtra("openUsername",username);
//            notifyIntent.putExtra("openAPP",appName);
//            notifyIntent.putExtra("openHost",host);
//            notifyIntent.putExtra("openPort",port);
//            notifyIntent.putExtra("timestamp",timestamp);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent appIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
//
//            Notification notification
//                    = new Notification.Builder(this)
//                    .setContentIntent(appIntent)
//                    .setSmallIcon(R.drawable.brahma_app_icon_white) // 設置狀態列裡面的圖示（小圖示）　　
//                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.brahma_app_icon_white)) // 下拉下拉清單裡面的圖示（大圖示）
//                    .setTicker("notification on status bar.") // 設置狀態列的顯示的資訊
//                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
//                    .setAutoCancel(false) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
//                    .setContentTitle(nTitle) // 設置下拉清單裡的標題
//                    .setContentText(nContent)// 設置上下文內容
//                    //.setOngoing(true)      //true使notification变为ongoing，用户不能手动清除  // notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
//                    .setWhen(System.currentTimeMillis())
//                    .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
//                    .setPriority(Notification.PRIORITY_DEFAULT)
//                    .build();
//
//            mNotificationManager.notify((int)System.currentTimeMillis(), notification);
//        }
//    }
//}
