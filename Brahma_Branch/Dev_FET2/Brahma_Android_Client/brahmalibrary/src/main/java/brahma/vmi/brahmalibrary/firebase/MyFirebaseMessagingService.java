package brahma.vmi.brahmalibrary.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.apprtc.AppRTCClient;
import brahma.vmi.brahmalibrary.database.ItemBio;
import brahma.vmi.brahmalibrary.database.MyDBHelper;
import brahma.vmi.brahmalibrary.wcitui.WelcomeActivity;
import me.leolin.shortcutbadger.ShortcutBadger;

import static android.app.Notification.DEFAULT_ALL;
import static android.app.Notification.VISIBILITY_PUBLIC;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static String registration_id = "";
    ItemBio itemBio;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("handleIntent", "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d("handleIntent", "Message data payload: " + remoteMessage.getData());
//            Log.d("handleIntent", "Message data payload: " + remoteMessage.getData().get("message"));
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String str = remoteMessage.getData().get("message");
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(str);
                Log.d("handleIntent", "Message Notification Body: " + remoteMessage.getNotification().getBody());
                sendNotification(
                        remoteMessage.getNotification().getTitle(),
                        remoteMessage.getNotification().getBody(),
                        jsonObj.getString("package_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d("handleIntent", "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        registration_id = token;
    }

    private void sendNotification(String title, String body, String package_name) {
        String id = "FCM";
        MyDBHelper dbHelper = new MyDBHelper(this);
        itemBio = new ItemBio(dbHelper.getDatabase(this));

        if (itemBio.getCount_login() != 0) {
            itemBio.update_package(1, package_name);
        }
        Bitmap appICON = BitmapFactory.decodeResource(getResources(), R.drawable.fet);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);

        String group = "notification_group";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("handleIntent", "android 8.0之後");
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Log.d("handleIntent", "mNotificationManager.getActiveNotifications() >>>>>> " + mNotificationManager.getActiveNotifications().length);
            Intent notifyIntent = new Intent().setClass(this, WelcomeActivity.class);
            notifyIntent.setAction("NOTIFICATION_ACTION");
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent appIntent = PendingIntent.getActivity(this, uniqueInt, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationChannel channel = new NotificationChannel(id, "Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("APPortal Notification");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            mNotificationManager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.brahma_app_icon_white)
                    .setColor(getColor(R.color.color_primaryDark))
                    .setLargeIcon(appICON)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(appIntent)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setGroup(group)
                    .setNumber(1)
                    .setShowWhen(true)
                    .setChronometerCountDown(true)
                    .setFullScreenIntent(appIntent, false)
                    .setVisibility(VISIBILITY_PUBLIC)
                    .setDeleteIntent(appIntent)
                    .setChannelId(id);

            Notification notification = builder.build();

            mNotificationManager.notify((int) System.currentTimeMillis(), notification);

            int badgeCount = mNotificationManager.getActiveNotifications().length;
            ShortcutBadger.applyCount(getApplicationContext(), badgeCount); //for 1.1.4+

        } else {
            Log.d("handleIntent", "android 8.0之前");
            //android 8.0之前
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notifyIntent = new Intent().setClass(this, WelcomeActivity.class);
            notifyIntent.setAction("NOTIFICATION_ACTION");
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent appIntent = PendingIntent.getActivity(this, uniqueInt, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(this)
                    .setContentIntent(appIntent)
                    .setSmallIcon(R.mipmap.brahma_app_icon_white) // 設置狀態列裡面的圖示（小圖示）　　
                    .setColor(getColor(R.color.color_primaryDark))
                    .setLargeIcon(appICON) // 下拉下拉清單裡面的圖示（大圖示）
                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
                    .setAutoCancel(true) // 設置通知被使用者點擊後是否清除
                    .setContentTitle(title) // 設置下拉清單裡的標題
                    .setContentText(body)// 設置上下文內容
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setNumber(1)
                    .build();

            assert mNotificationManager != null;
            mNotificationManager.notify((int) System.currentTimeMillis(), notification);
            int badgeCount = mNotificationManager.getActiveNotifications().length;
            ShortcutBadger.applyCount(getApplicationContext(), badgeCount); //for 1.1.4+
        }
    }
}
