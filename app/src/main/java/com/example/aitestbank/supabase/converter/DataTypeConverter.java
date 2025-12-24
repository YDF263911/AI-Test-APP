package com.example.aitestbank.supabase.converter;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 数据类型转换器
 * 用于处理各种数据类型的转换和验证
 */
public class DataTypeConverter {
    private static final String TAG = "DataTypeConverter";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    
    // 日期格式
    private static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    };
    
    /**
     * 安全转换为整数
     */
    public static int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            
            if (value instanceof String) {
                String strValue = ((String) value).trim();
                if (NUMBER_PATTERN.matcher(strValue).matches()) {
                    return Double.parseDouble(strValue) < Integer.MAX_VALUE ? 
                           (int) Double.parseDouble(strValue) : defaultValue;
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "整数转换失败: " + value, e);
        }
        
        return defaultValue;
    }
    
    /**
     * 安全转换为长整数
     */
    public static long toLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            
            if (value instanceof String) {
                String strValue = ((String) value).trim();
                if (NUMBER_PATTERN.matcher(strValue).matches()) {
                    return Double.parseDouble(strValue) < Long.MAX_VALUE ? 
                           (long) Double.parseDouble(strValue) : defaultValue;
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "长整数转换失败: " + value, e);
        }
        
        return defaultValue;
    }
    
    /**
     * 安全转换为双精度浮点数
     */
    public static double toDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            
            if (value instanceof String) {
                String strValue = ((String) value).trim();
                if (NUMBER_PATTERN.matcher(strValue).matches()) {
                    return Double.parseDouble(strValue);
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "双精度浮点数转换失败: " + value, e);
        }
        
        return defaultValue;
    }
    
    /**
     * 安全转换为布尔值
     */
    public static boolean toBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            
            if (value instanceof String) {
                String strValue = ((String) value).trim().toLowerCase();
                return "true".equals(strValue) || "1".equals(strValue) || "yes".equals(strValue);
            }
            
        } catch (Exception e) {
            Log.w(TAG, "布尔值转换失败: " + value, e);
        }
        
        return defaultValue;
    }
    
    /**
     * 安全转换为字符串
     */
    public static String toString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return value.toString();
        } catch (Exception e) {
            Log.w(TAG, "字符串转换失败: " + value, e);
            return defaultValue;
        }
    }
    
    /**
     * 安全转换为字符串列表
     */
    public static List<String> toStringList(Object value, List<String> defaultValue) {
        if (value == null) {
            return defaultValue != null ? defaultValue : new ArrayList<>();
        }
        
        try {
            if (value instanceof List) {
                List<String> stringList = new ArrayList<>();
                for (Object item : (List<?>) value) {
                    stringList.add(toString(item, ""));
                }
                return stringList;
            }
            
            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.startsWith("[") && strValue.endsWith("]")) {
                    // JSON数组格式
                    strValue = strValue.substring(1, strValue.length() - 1);
                    String[] items = strValue.split(",");
                    List<String> stringList = new ArrayList<>();
                    for (String item : items) {
                        stringList.add(item.trim().replaceAll("^\"|\"$", ""));
                    }
                    return stringList;
                } else if (strValue.contains(",")) {
                    // 逗号分隔格式
                    return Arrays.asList(strValue.split(","));
                } else {
                    // 单个字符串
                    return Arrays.asList(strValue);
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "字符串列表转换失败: " + value, e);
        }
        
        return defaultValue != null ? defaultValue : new ArrayList<>();
    }
    
    /**
     * 安全转换为时间戳（毫秒）
     */
    public static long toTimestamp(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            
            if (value instanceof String) {
                String strValue = ((String) value).trim();
                
                // 先尝试解析为数字
                if (NUMBER_PATTERN.matcher(strValue).matches()) {
                    long timestamp = (long) Double.parseDouble(strValue);
                    // 如果时间戳小于10位数字，可能是秒级时间戳
                    if (timestamp < 10000000000L) {
                        timestamp *= 1000;
                    }
                    return timestamp;
                }
                
                // 尝试解析为日期字符串
                for (SimpleDateFormat format : DATE_FORMATS) {
                    try {
                        Date date = format.parse(strValue);
                        return date.getTime();
                    } catch (ParseException ignored) {
                        // 继续尝试下一个格式
                    }
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "时间戳转换失败: " + value, e);
        }
        
        return defaultValue;
    }
    
    /**
     * 安全转换日期字符串为标准格式
     */
    public static String toDateString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        try {
            long timestamp = toTimestamp(value, 0);
            if (timestamp > 0) {
                Date date = new Date(timestamp);
                return DATE_FORMATS[0].format(date);
            }
        } catch (Exception e) {
            Log.w(TAG, "日期字符串转换失败: " + value, e);
        }
        
        return toString(value, defaultValue);
    }
    
    /**
     * 验证字符串是否为有效的数字
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return NUMBER_PATTERN.matcher(str.trim()).matches();
    }
    
    /**
     * 验证字符串是否为有效的邮箱
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email.trim()).matches();
    }
    
    /**
     * 验证字符串是否为有效的手机号（中国）
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        String phoneRegex = "^1[3-9]\\d{9}$";
        return Pattern.compile(phoneRegex).matcher(phone.trim()).matches();
    }
    
    /**
     * 清理和标准化字符串
     */
    public static String cleanString(String str) {
        if (str == null) {
            return "";
        }
        
        return str.trim()
                .replaceAll("\\s+", " ") // 多个空格替换为单个空格
                .replaceAll("[\\r\\n]+", " "); // 换行符替换为空格
    }
    
    /**
     * 截断字符串到指定长度
     */
    public static String truncateString(String str, int maxLength, String suffix) {
        if (str == null) {
            return "";
        }
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength - suffix.length()) + suffix;
    }
    
    /**
     * 将对象转换为JSON字符串（简单实现）
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        // 对于其他对象，返回其字符串表示
        return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
    }
}