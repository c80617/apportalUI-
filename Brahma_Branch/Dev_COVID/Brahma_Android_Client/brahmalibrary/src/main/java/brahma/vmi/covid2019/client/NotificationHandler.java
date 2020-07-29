/*
Copyright 2013 The MITRE Corporation, All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package brahma.vmi.covid2019.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.wcitui.BrahmaMainActivity;

/**
 * @developer Ian
 * Receives Notification messages from the server to display in the client's notification tray
 */
public class NotificationHandler {
    public static void inspect(BRAHMAProtocol.Response response, Context context, int connectionID) {
        BRAHMAProtocol.Notification notification = response.getNotification();

        // build intent (triggered when notification is selected)
        // Creates an explicit intent for the ConnectionList
        Intent resultIntent = new Intent(context, BrahmaMainActivity.class);
        resultIntent.putExtra("connectionID", connectionID);
        PendingIntent resultPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(BrahmaMainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            resultPendingIntent =  stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else {
            resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // build notification
        Notification.Builder notice = new Notification.Builder(context)
            .setContentTitle(notification.getContentTitle())
            .setContentText(notification.getContentText())
            .setSmallIcon(R.drawable.brahma_app_icon_white) // no way to set a dynamic small icon!
            .setContentIntent(resultPendingIntent);
        // if we have a large icon (optional), set it; otherwise use the small icon (required)
        if (notification.hasLargeIcon()) {
            byte[] data = notification.getLargeIcon().toByteArray();
            if (data != null) {
                Bitmap icon = BitmapFactory.decodeByteArray(data,0, data.length);
                notice.setLargeIcon(icon.copy(Bitmap.Config.ARGB_8888, true));
            }
        }
        else {
            byte[] data = notification.getSmallIcon().toByteArray();
            if (data != null) {
                Bitmap icon = BitmapFactory.decodeByteArray(data,0, data.length);
                notice.setLargeIcon(icon.copy(Bitmap.Config.ARGB_8888, true));
            }
        }
        notice.setAutoCancel(true); // hide it after it's been clicked

        // display the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            notificationManager.notify(1, notice.build());
        else
            notificationManager.notify(1, notice.getNotification());
    }
}
