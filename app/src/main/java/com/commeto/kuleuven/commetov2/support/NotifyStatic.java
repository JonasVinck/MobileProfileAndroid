package com.commeto.kuleuven.commetov2.support;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.commeto.kuleuven.commetov2.R;

/**
 * Created by Jonas on 19/04/2018.
 */

public class NotifyStatic {

    public static void postNotification(Context context, String title, String message){

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        RemoteViews smallView = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        smallView.setTextViewText(R.id.title, title);
        RemoteViews bigView = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        bigView.setTextViewText(R.id.title, title);
        bigView.setTextViewText(R.id.message, message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.preferences))
                .setSmallIcon(R.drawable.logo_svg_clicked)
                .setCustomContentView(smallView)
                .setCustomBigContentView(bigView)
                .setStyle(new NotificationCompat.BigTextStyle());

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
    }
}
