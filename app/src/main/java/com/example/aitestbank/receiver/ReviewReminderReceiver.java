package com.example.aitestbank.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.aitestbank.MainActivity;
import com.example.aitestbank.R;

/**
 * 复习提醒广播接收器
 */
public class ReviewReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ReviewReminderReceiver";
    private static final String CHANNEL_ID = "review_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "收到复习提醒广播");
        
        String action = intent.getAction();
        if ("DAILY_REVIEW_REMINDER".equals(action)) {
            showReviewNotification(context);
        }
    }
    
    /**
     * 显示复习提醒通知
     */
    private void showReviewNotification(Context context) {
        // 创建通知渠道（Android 8.0+）
        createNotificationChannel(context);
        
        // 创建跳转到错题本的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "wrong_question");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("AI题库 - 复习提醒")
            .setContentText("今天有错题需要复习，点击查看详情")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("根据艾宾浩斯遗忘曲线，今天有需要复习的错题。及时复习可以巩固记忆，提高学习效果。"))
            .build();
        
        // 显示通知
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        
        Log.d(TAG, "复习提醒通知已发送");
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "复习提醒";
            String description = "错题复习提醒通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}