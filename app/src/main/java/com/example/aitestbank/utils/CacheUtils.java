package com.example.aitestbank.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 缓存工具类 - 用于数据缓存管理
 */
public class CacheUtils {
    
    private static final String PREF_NAME = "aitestbank_cache";
    private static final String KEY_QUESTIONS_CACHE = "questions_cache";
    private static final String KEY_WRONG_QUESTIONS_CACHE = "wrong_questions_cache";
    private static final String KEY_USER_CACHE = "user_cache";
    private static final String KEY_CACHE_TIMESTAMP = "cache_timestamp";
    
    private static SharedPreferences preferences;
    private static Gson gson;
    
    /**
     * 初始化缓存工具
     */
    public static void init(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * 缓存题目列表
     */
    public static void cacheQuestions(List<?> questions) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        String json = gson.toJson(questions);
        preferences.edit()
            .putString(KEY_QUESTIONS_CACHE, json)
            .putLong(KEY_CACHE_TIMESTAMP + "_questions", System.currentTimeMillis())
            .apply();
    }
    
    /**
     * 获取缓存的题目列表
     */
    public static <T> List<T> getCachedQuestions(Class<T> clazz) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        String json = preferences.getString(KEY_QUESTIONS_CACHE, "");
        if (json.isEmpty()) {
            return null;
        }
        
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * 缓存错题列表
     */
    public static void cacheWrongQuestions(List<?> wrongQuestions) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        String json = gson.toJson(wrongQuestions);
        preferences.edit()
            .putString(KEY_WRONG_QUESTIONS_CACHE, json)
            .putLong(KEY_CACHE_TIMESTAMP + "_wrong_questions", System.currentTimeMillis())
            .apply();
    }
    
    /**
     * 获取缓存的错题列表
     */
    public static <T> List<T> getCachedWrongQuestions(Class<T> clazz) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        String json = preferences.getString(KEY_WRONG_QUESTIONS_CACHE, "");
        if (json.isEmpty()) {
            return null;
        }
        
        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * 缓存用户信息
     */
    public static void cacheUser(Object user) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        String json = gson.toJson(user);
        preferences.edit()
            .putString(KEY_USER_CACHE, json)
            .putLong(KEY_CACHE_TIMESTAMP + "_user", System.currentTimeMillis())
            .apply();
    }
    
    /**
     * 获取缓存的用户信息
     */
    public static <T> T getCachedUser(Class<T> clazz) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        String json = preferences.getString(KEY_USER_CACHE, "");
        if (json.isEmpty()) {
            return null;
        }
        
        return gson.fromJson(json, clazz);
    }
    
    /**
     * 获取缓存时间戳
     */
    public static long getCacheTimestamp(String key) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        return preferences.getLong(KEY_CACHE_TIMESTAMP + "_" + key, 0);
    }
    
    /**
     * 检查缓存是否过期
     */
    public static boolean isCacheExpired(String key, long expireTimeMillis) {
        long timestamp = getCacheTimestamp(key);
        return System.currentTimeMillis() - timestamp > expireTimeMillis;
    }
    
    /**
     * 清除所有缓存
     */
    public static void clearAllCache() {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        preferences.edit().clear().apply();
    }
    
    /**
     * 清除指定缓存
     */
    public static void clearCache(String key) {
        if (preferences == null) {
            throw new IllegalStateException("CacheUtils未初始化，请先调用init()方法");
        }
        
        preferences.edit()
            .remove(key)
            .remove(KEY_CACHE_TIMESTAMP + "_" + key)
            .apply();
    }
    
    /**
     * 获取缓存大小（KB）
     */
    public static int getCacheSize() {
        if (preferences == null) {
            return 0;
        }
        
        int size = 0;
        for (String key : preferences.getAll().keySet()) {
            String value = preferences.getString(key, "");
            size += value.getBytes().length;
        }
        return size / 1024; // 转换为KB
    }
}