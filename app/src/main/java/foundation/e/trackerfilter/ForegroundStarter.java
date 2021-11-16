package foundation.e.trackerfilter;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;

import dnsfilter.android.R;

public class ForegroundStarter {
    private static final String NOTIFICATION_CHANNEL_ID = "blocker_service";
    public static void startForeground(Service service){
        NotificationManager mNotificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW));
            Notification notification = new Notification.Builder(service, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(service.getString(R.string.app_name)).build();
            service.startForeground(1337, notification);
        }
    }
}
