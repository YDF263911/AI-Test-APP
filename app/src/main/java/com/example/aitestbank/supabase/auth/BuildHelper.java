package com.example.aitestbank.supabase.auth;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.util.Locale;

/**
 * 设备信息辅助类 - 用于生成设备唯一标识符
 */
public class BuildHelper {
    
    /**
     * 获取设备信息字符串（用于生成设备ID）
     */
    public static String getDeviceInfo(Context context) {
        try {
            StringBuilder sb = new StringBuilder();
            
            // 基础设备信息
            sb.append("MODEL:").append(Build.MODEL != null ? Build.MODEL : "");
            sb.append("MANUFACTURER:").append(Build.MANUFACTURER != null ? Build.MANUFACTURER : "");
            sb.append("BRAND:").append(Build.BRAND != null ? Build.BRAND : "");
            sb.append("DEVICE:").append(Build.DEVICE != null ? Build.DEVICE : "");
            sb.append("PRODUCT:").append(Build.PRODUCT != null ? Build.PRODUCT : "");
            sb.append("BOARD:").append(Build.BOARD != null ? Build.BOARD : "");
            sb.append("HARDWARE:").append(Build.HARDWARE != null ? Build.HARDWARE : "");
            
            // Android版本信息
            sb.append("SDK:").append(Build.VERSION.SDK_INT);
            sb.append("RELEASE:").append(Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "");
            
            // 语言设置
            Locale locale = Locale.getDefault();
            sb.append("LOCALE:").append(locale.getLanguage()).append("-").append(locale.getCountry());
            
            return sb.toString();
            
        } catch (Exception e) {
            return "unknown_device";
        }
    }
}