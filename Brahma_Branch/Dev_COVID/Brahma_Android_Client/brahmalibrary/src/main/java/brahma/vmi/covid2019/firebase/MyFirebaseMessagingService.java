package brahma.vmi.covid2019.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.apprtc.AppRTCClient;
import brahma.vmi.covid2019.database.ItemBio;
import brahma.vmi.covid2019.wcitui.WelcomeActivity;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static String registration_id = "";
    ItemBio itemBio;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
    }

    @Override
    public void handleIntent(Intent intent) {
        Log.d("handleIntent", "handleIntent:" + intent.getAction());


        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                Log.d("handleIntent", "Key: " + key + " Value: " + value);
            }
            //app is not running
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            AppRTCClient.deviceID = androidId;
            registration_id = FirebaseInstanceId.getInstance().getToken();
            //if(!getStatus()){
            if (intent.getExtras().get("message") != null) {
                String str = intent.getExtras().get("message").toString();
                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(str);
                    if (intent.getExtras().get("gcm.notification.title") != null) {
                        sendNotification(
                                intent.getExtras().get("gcm.notification.title").toString(),
                                intent.getExtras().get("gcm.notification.body").toString(),
                                jsonObj.getString("package_name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //}
        }
    }

    private void sendNotification(String title, String body, String package_name) {
        String id = "FCM";
        String packageName = package_name;
        itemBio = new ItemBio(getBaseContext());

        if (itemBio.getCount_login() != 0) {
            itemBio.update_package(1,packageName);
        }
//        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
//        String username = sharedPreferences.getString("usernameView", "");
//        String password = sharedPreferences.getString("password", "");
//        String host = sharedPreferences.getString("hostView", "");
//        String port = sharedPreferences.getString("portView", "");

        Bitmap appICON = BitmapFactory.decodeResource(getResources(), R.drawable.brahma_app_icon_white);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);

        String group = "notification_group";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("handleIntent", "android 8.0之後");
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Log.d("handleIntent", "mNotificationManager.getActiveNotifications() >>>>>> " + mNotificationManager.getActiveNotifications().length);
            Intent notifyIntent = new Intent().setClass(this, WelcomeActivity.class);
            notifyIntent.setAction("NOTIFICATION_ACTION");
//            notifyIntent.putExtra("package_name", package_name);
//            notifyIntent.putExtra("vm_ip", vm_ip);
//            notifyIntent.putExtra("username", username);
//            notifyIntent.putExtra("password", password);
//            notifyIntent.putExtra("host", host);
//            notifyIntent.putExtra("port", port);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent appIntent = PendingIntent.getActivity(this, uniqueInt, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationChannel channel = new NotificationChannel(id, "Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("APPortal Notification");
            channel.enableLights(true);
            channel.enableVibration(true);
            mNotificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.brahma_app_icon_white)
                    .setLargeIcon(appICON)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(appIntent)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setGroup(group)
                    .setNumber(1)
                    .setChannelId(id);

            Notification.Builder builder2 = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.brahma_app_icon_white)
                    .setContentTitle("APPortal")
                    .setContentText("You have new notification.")
                    .setLargeIcon(appICON)
                    .setContentIntent(appIntent)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setGroup(group)
                    .setGroupSummary(true)
                    .setNumber(1)
                    .setChannelId(id);

            mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
            mNotificationManager.notify(0, builder2.build());

            int badgeCount = mNotificationManager.getActiveNotifications().length;
            ShortcutBadger.applyCount(getApplicationContext(), badgeCount); //for 1.1.4+

        } else {
            Log.d("handleIntent", "android 8.0之前");
            //android 8.0之前
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notifyIntent = new Intent().setClass(this, WelcomeActivity.class);
            notifyIntent.setAction("NOTIFICATION_ACTION");
//            notifyIntent.putExtra("package_name", package_name);
//            notifyIntent.putExtra("vm_ip", vm_ip);
//            notifyIntent.putExtra("username", username);
//            notifyIntent.putExtra("password", password);
//            notifyIntent.putExtra("host", host);
//            notifyIntent.putExtra("port", port);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent appIntent = PendingIntent.getActivity(this, uniqueInt, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(this)
                    .setContentIntent(appIntent)
                    .setSmallIcon(R.drawable.brahma_app_icon_white) // 設置狀態列裡面的圖示（小圖示）　　
                    .setLargeIcon(appICON) // 下拉下拉清單裡面的圖示（大圖示）
                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
                    .setAutoCancel(true) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    .setContentTitle(title) // 設置下拉清單裡的標題
                    .setContentText(body)// 設置上下文內容
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setNumber(1)
                    .build();

            Notification notification2 = new Notification.Builder(this)
                    .setContentIntent(appIntent)
                    .setSmallIcon(R.drawable.brahma_app_icon_white) // 設置狀態列裡面的圖示（小圖示）　　
                    .setLargeIcon(appICON) // 下拉下拉清單裡面的圖示（大圖示）
                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
                    .setAutoCancel(true) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    .setContentTitle("APPortal")
                    .setContentText("You have new notification.")
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setGroup(group)
                    .setGroupSummary(true)
                    .setNumber(1)
                    .build();

            mNotificationManager.notify((int) System.currentTimeMillis(), notification);
            mNotificationManager.notify(0, notification2);

            int badgeCount = mNotificationManager.getActiveNotifications().length;
            ShortcutBadger.applyCount(getApplicationContext(), badgeCount); //for 1.1.4+
        }
    }


}
