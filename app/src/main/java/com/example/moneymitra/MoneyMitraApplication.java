package com.example.moneymitra;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MoneyMitraApplication extends Application {

    public static final String CHANNEL_DAILY = "daily_reminder";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel dailyChannel = new NotificationChannel(
                    CHANNEL_DAILY,
                    "Daily Expense Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            dailyChannel.setDescription("Reminds you to log expenses");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(dailyChannel);
        }
    }
}
