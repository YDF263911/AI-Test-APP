package com.example.aitestbank.supabase.auth;

import android.content.Context;
import android.provider.Settings;
import java.util.UUID;

/**
 * 设备ID管理器 - 用于无认证版本的用户识别
 */
public class DeviceIdManager {
    
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_USER_ID = "user_id";
    
    private static String cachedDeviceId;
    private static String cachedUserId;
    
    /**
     * 获取设备唯一ID
     */
    public static String getDeviceId(Context context) {
        if (cachedDeviceId == null) {
            var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            cachedDeviceId = prefs.getString(KEY_DEVICE_ID, null);
            
            if (cachedDeviceId == null) {
                // 生成新的设备ID
                cachedDeviceId = generateDeviceId();
                prefs.edit().putString(KEY_DEVICE_ID, cachedDeviceId).apply();
            }
        }
        return cachedDeviceId;
    }
    
    /**
     * 获取用户ID（基于设备ID生成的UUID）
     */
    public static String getUserId(Context context) {
        if (cachedUserId == null) {
            var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            cachedUserId = prefs.getString(KEY_USER_ID, null);
            
            if (cachedUserId == null) {
                // 生成新的用户ID
                cachedUserId = UUID.randomUUID().toString();
                prefs.edit().putString(KEY_USER_ID, cachedUserId).apply();
            }
        }
        return cachedUserId;
    }
    
    /**
     * 生成设备ID
     */
    private static String generateDeviceId() {
        // 使用Android ID + 时间戳 + 随机数组合
        String androidId = Settings.Secure.ANDROID_ID;
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        
        return "device_" + androidId + "_" + timestamp + "_" + random;
    }
    
    /**
     * 重置用户ID（用于数据重置）
     */
    public static void resetUserId(Context context) {
        var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_ID).apply();
        cachedUserId = null;
    }
    
    /**
     * 检查是否为新用户
     */
    public static boolean isNewUser(Context context) {
        var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.contains(KEY_USER_ID);
    }
}