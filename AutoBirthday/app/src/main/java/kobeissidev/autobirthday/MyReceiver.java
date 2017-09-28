package kobeissidev.autobirthday;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;
import static kobeissidev.autobirthday.MainActivity.runNotification;
import static kobeissidev.autobirthday.MainActivity.runNotificationManager;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        runNotificationManager(context);

        runNotification(context, notificationManager);

        Utility.scheduleJob(context);

    }

}