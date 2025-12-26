package com.example.aitestbank.supabase.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 设备ID管理器 - 为每个设备安装生成唯一标识符
 * 用于Supabase RLS策略的设备识别
 */
public class DeviceIdManager {
    private static final String TAG = "DeviceIdManager";
    private static final String PREFS_NAME = "device_id_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    
    /**
     * 获取设备唯一标识符
     * 优先使用Android ID，如果没有则生成UUID并持久化
     */
    public static String getDeviceId(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String deviceId = prefs.getString(KEY_DEVICE_ID, null);
            
            if (deviceId == null) {
                // 生成新的设备ID
                deviceId = generateDeviceId(context);
                prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
                Log.i(TAG, "生成新的设备ID: " + deviceId);
            } else {
                Log.d(TAG, "使用已存在的设备ID: " + deviceId);
            }
            
            return deviceId;
            
        } catch (Exception e) {
            Log.e(TAG, "获取设备ID失败", e);
            // 降级方案：生成临时UUID
            return "temp_" + UUID.randomUUID().toString();
        }
    }
    
    /**
     * 生成设备唯一标识符
     */
    private static String generateDeviceId(Context context) {
        try {
            // 尝试使用Android ID
            String androidId = Settings.Secure.getString(
                context.getContentResolver(), 
                Settings.Secure.ANDROID_ID
            );
            
            if (androidId != null && !androidId.isEmpty() && !"9774d56d682e549c".equals(androidId)) {
                // 对Android ID进行哈希处理以保护隐私
                return hashDeviceId(androidId);
            }
            
            // 如果Android ID不可用，使用设备信息组合生成
            String deviceInfo = BuildHelper.getDeviceInfo(context);
            if (deviceInfo != null && !deviceInfo.isEmpty()) {
                return hashDeviceId(deviceInfo);
            }
            
            // 最终降级方案：生成随机UUID
            return UUID.randomUUID().toString();
            
        } catch (Exception e) {
            Log.w(TAG, "生成设备ID异常，使用UUID", e);
            return UUID.randomUUID().toString();
        }
    }
    
    /**
     * 对设备ID进行哈希处理以保护隐私
     */
    private static String hashDeviceId(String deviceId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(deviceId.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32); // 取前32位
            
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "SHA-256不可用，使用原始ID", e);
            return deviceId;
        }
    }
    
    /**
     * 清除设备ID（用于测试或重置）
     */
    public static void clearDeviceId(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_DEVICE_ID).apply();
            Log.i(TAG, "设备ID已清除");
        } catch (Exception e) {
            Log.e(TAG, "清除设备ID失败", e);
        }
    }
    
    /**
     * 获取设备ID的简短版本（用于调试）
     */
    public static String getShortDeviceId(Context context) {
        String deviceId = getDeviceId(context);
        if (deviceId.length() > 8) {
            return deviceId.substring(0, 8) + "...";
        }
        return deviceId;
    }
}