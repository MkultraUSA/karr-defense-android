package com.codex.karrdefense;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/** Keeps the field run alive in the foreground so Android does not kill scans. */
public class FieldService extends Service {
    public static final String CH = "karr_field";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && nm != null) {
            NotificationChannel ch = new NotificationChannel(CH, "KARR field run",
                    NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
        Notification.Builder b = (Build.VERSION.SDK_INT >= 26)
                ? new Notification.Builder(this, CH)
                : new Notification.Builder(this);
        b.setContentTitle("KARR field run active")
                .setContentText("Passive discovery running. Takeover restores on exit.")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth);
        startForeground(41, b.build());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
