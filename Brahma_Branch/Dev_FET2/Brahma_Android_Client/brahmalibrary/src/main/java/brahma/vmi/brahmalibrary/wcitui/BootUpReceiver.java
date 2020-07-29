package brahma.vmi.brahmalibrary.wcitui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import brahma.vmi.brahmalibrary.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /* 同一個接收者可以收多個不同行為的廣播
           所以可以判斷收進來的行為為何，再做不同的動作 */
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("notificationAlert","BootUpReceiver");
            /* 收到廣播後開啟目的Service */
            //Intent startServiceIntent = new Intent(context, BootUpService.class);
            //context.startService(startServiceIntent);//android 8.0以前
            //context.startForegroundService(startServiceIntent);//android 8.0

            //call MainActivity and send notification
//            Intent mainIntent = new Intent(context, MainActivity.class);
//            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mainIntent);

//            //only send notification(android 8.0)
//            String idLove  = "Channel Love";
//            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            NotificationChannel channelLove = new NotificationChannel(
//                    idLove,
//                    "Channel Love",
//                    NotificationManager.IMPORTANCE_HIGH);
//            channelLove.setDescription("最重要的人");
//            channelLove.enableLights(true);
//            channelLove.enableVibration(true);
//            mNotificationManager.createNotificationChannel(channelLove);
//            Notification.Builder builder = new Notification.Builder(context)
//                    .setSmallIcon(R.drawable.ic_launcher)
//                    .setContentTitle("My Love")
//                    .setContentText("Hi, my love!")
//                    .setChannelId(idLove);
//            mNotificationManager.notify(1, builder.build());

            //android 8.0之前
            NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            Intent notifyIntent = new Intent(context, BrahmaMainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent appIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);

            Notification notification
                    = new Notification.Builder(context)
                    .setContentIntent(appIntent)
                    .setSmallIcon(R.drawable.fet) // 設置狀態列裡面的圖示（小圖示）　　
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.fet)) // 下拉下拉清單裡面的圖示（大圖示）
                    .setTicker("notification on status bar.") // 設置狀態列的顯示的資訊
                    .setWhen(System.currentTimeMillis())// 設置時間發生時間
                    .setAutoCancel(false) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    .setContentTitle("Notification Title") // 設置下拉清單裡的標題
                    .setContentText("Notification Content")// 設置上下文內容
                    //.setOngoing(true)      //true使notification变为ongoing，用户不能手动清除  // notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;

                    .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
// .setDefaults(Notification.DEFAULT_VIBRATE) //使用默認手機震動提示
// .setDefaults(Notification.DEFAULT_SOUND) //使用默認聲音提示
// .setDefaults(Notification.DEFAULT_LIGHTS) //使用默認閃光提示
// .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND) //使用默認閃光提示 與 默認聲音提示

// .setVibrate(vibrate) //自訂震動長度
// .setSound(uri) //自訂鈴聲
// .setLights(0xff00ff00, 300, 1000) //自訂燈光閃爍 (ledARGB, ledOnMS, ledOffMS)
                    .build();

//把指定ID的通知持久的發送到狀態條上
            mNotificationManager.notify(0, notification);
        }
    }
}
