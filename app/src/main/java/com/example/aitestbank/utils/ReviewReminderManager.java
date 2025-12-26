package com.example.aitestbank.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.aitestbank.model.ReviewSchedule;
import com.example.aitestbank.receiver.ReviewReminderReceiver;
import com.example.aitestbank.ui.wrong.WrongQuestionFragment;

import java.util.Calendar;
import java.util.List;

/**
 * 错题复习提醒管理器
 */
public class ReviewReminderManager {
    
    private static final String TAG = "ReviewReminderManager";
    private static final String PREFS_NAME = "ReviewReminderPrefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_DAILY_TIME = "daily_time"; // 格式: HH:mm
    
    private Context context;
    private AlarmManager alarmManager;
    private SharedPreferences prefs;
    
    public ReviewReminderManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 设置每日复习提醒
     */
    public void setDailyReviewReminder(boolean enabled, String time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMINDER_ENABLED, enabled);
        editor.putString(KEY_DAILY_TIME, time);
        editor.apply();
        
        if (enabled) {
            scheduleDailyReminder(time);
        } else {
            cancelDailyReminder();
        }
    }
    
    /**
     * 安排每日提醒
     */
    private void scheduleDailyReminder(String time) {
        try {
            // 解析时间字符串
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // 设置提醒时间
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            // 如果当前时间已经过了设定的时间，就设置为明天
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // 创建PendingIntent
            Intent intent = new Intent(context, ReviewReminderReceiver.class);
            intent.setAction("DAILY_REVIEW_REMINDER");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // 设置重复提醒（每天）
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
            
            Log.d(TAG, "每日复习提醒已设置: " + time);
            
        } catch (Exception e) {
            Log.e(TAG, "设置每日复习提醒失败", e);
        }
    }
    
    /**
     * 取消每日提醒
     */
    private void cancelDailyReminder() {
        Intent intent = new Intent(context, ReviewReminderReceiver.class);
        intent.setAction("DAILY_REVIEW_REMINDER");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        
        Log.d(TAG, "每日复习提醒已取消");
    }
    
    /**
     * 根据艾宾浩斯遗忘曲线安排复习计划
     */
    public ReviewSchedule createReviewSchedule(int wrongCount, long firstWrongTime) {
        ReviewSchedule schedule = new ReviewSchedule();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(firstWrongTime);
        
        // 艾宾浩斯遗忘曲线复习间隔（分钟）：1, 10, 60, 1440, 4320, 10080
        long[] intervals = {1, 10, 60, 1440, 4320, 10080}; // 分钟
        
        schedule.setFirstReviewTime(calendar.getTimeInMillis());
        
        for (int i = 0; i < intervals.length; i++) {
            calendar.add(Calendar.MINUTE, (int) intervals[i]);
            schedule.addReviewTime(calendar.getTimeInMillis());
        }
        
        return schedule;
    }
    
    /**
     * 检查是否有需要复习的错题
     */
    public boolean hasPendingReviews(List<ReviewSchedule> schedules) {
        long currentTime = System.currentTimeMillis();
        
        for (ReviewSchedule schedule : schedules) {
            List<Long> reviewTimes = schedule.getReviewTimes();
            for (Long reviewTime : reviewTimes) {
                if (reviewTime <= currentTime && !schedule.isReviewedAt(reviewTime)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 获取未复习的错题数量
     */
    public int getPendingReviewCount(List<ReviewSchedule> schedules) {
        long currentTime = System.currentTimeMillis();
        int count = 0;
        
        for (ReviewSchedule schedule : schedules) {
            List<Long> reviewTimes = schedule.getReviewTimes();
            for (Long reviewTime : reviewTimes) {
                if (reviewTime <= currentTime && !schedule.isReviewedAt(reviewTime)) {
                    count++;
                    break; // 每个错题只计算一次
                }
            }
        }
        
        return count;
    }
    
    /**
     * 获取提醒设置状态
     */
    public boolean isReminderEnabled() {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }
    
    /**
     * 获取每日提醒时间
     */
    public String getDailyTime() {
        return prefs.getString(KEY_DAILY_TIME, "20:00"); // 默认晚上8点
    }
}