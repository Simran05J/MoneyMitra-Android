package com.example.moneymitra;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BudgetNotificationHelper {

    public static void showOverBudgetNotification(Context context) {

        // Android 13+ permission check
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return; // Don't crash if permission denied
            }
        }

        Intent intent = new Intent(context, ExpenseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MoneyMitraApplication.CHANNEL_DAILY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Budget Exceeded!")
                .setContentText("You’ve crossed your monthly expense limit.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(2001, builder.build());
    }

}
