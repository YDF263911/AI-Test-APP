package com.example.aitestbank.supabase.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Supabase缓存管理器
 * 提供智能的缓存策略，支持过期检查和内存缓存
 */
public class SupabaseCacheManager {
    private static final String TAG = "SupabaseCacheManager";
    private static final String PREFS_NAME = "supabase_cache";
    private static final String CACHE_PREFIX = "cache_";
    private static final String TIMESTAMP_PREFIX = "timestamp_";
    private static final long DEFAULT_CACHE_DURATION = 5 * 60 * 1000; // 5分钟
    
    private static SupabaseCacheManager instance;
    private final SharedPreferences preferences;
    private final Map<String, CacheEntry> memoryCache;
    
    private SupabaseCacheManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        memoryCache = new HashMap<>();
    }
    
    public static synchronized SupabaseCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseCacheManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 缓存数据
     */
    public void putData(String key, String data) {
        putData(key, data, DEFAULT_CACHE_DURATION);
    }
    
    /**
     * 缓存数据（带自定义过期时间）
     */
    public void putData(String key, String data, long durationMillis) {
        try {
            long currentTime = System.currentTimeMillis();
            long expireTime = currentTime + durationMillis;
            
            // 存储到内存缓存
            CacheEntry entry = new CacheEntry(data, expireTime);
            memoryCache.put(key, entry);
            
            // 存储到SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(CACHE_PREFIX + key, data);
            editor.putLong(TIMESTAMP_PREFIX + key, expireTime);
            editor.apply();
            
            Log.d(TAG, "缓存数据: " + key + ", 过期时间: " + new java.util.Date(expireTime));
            
        } catch (Exception e) {
            Log.e(TAG, "缓存数据失败: " + key, e);
        }
    }
    
    /**
     * 获取缓存数据
     */
    public String getData(String key) {
        try {
            // 先检查内存缓存
            CacheEntry memoryEntry = memoryCache.get(key);
            if (memoryEntry != null && !memoryEntry.isExpired()) {
                Log.d(TAG, "从内存缓存获取数据: " + key);
                return memoryEntry.data;
            }
            
            // 检查持久化缓存
            String data = preferences.getString(CACHE_PREFIX + key, null);
            long expireTime = preferences.getLong(TIMESTAMP_PREFIX + key, 0);
            
            if (data != null && expireTime > System.currentTimeMillis()) {
                // 重新加载到内存缓存
                CacheEntry entry = new CacheEntry(data, expireTime);
                memoryCache.put(key, entry);
                Log.d(TAG, "从持久化缓存获取数据: " + key);
                return data;
            }
            
            // 数据过期或不存在
            Log.d(TAG, "缓存数据已过期或不存在: " + key);
            removeData(key);
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "获取缓存数据失败: " + key, e);
            return null;
        }
    }
    
    /**
     * 检查缓存是否存在且未过期
     */
    public boolean hasValidData(String key) {
        return getData(key) != null;
    }
    
    /**
     * 移除缓存数据
     */
    public void removeData(String key) {
        try {
            memoryCache.remove(key);
            
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(CACHE_PREFIX + key);
            editor.remove(TIMESTAMP_PREFIX + key);
            editor.apply();
            
            Log.d(TAG, "移除缓存数据: " + key);
            
        } catch (Exception e) {
            Log.e(TAG, "移除缓存数据失败: " + key, e);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        try {
            memoryCache.clear();
            
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            
            Log.d(TAG, "清空所有缓存");
            
        } catch (Exception e) {
            Log.e(TAG, "清空缓存失败", e);
        }
    }
    
    /**
     * 清理过期的缓存数据
     */
    public void cleanupExpiredCache() {
        try {
            long currentTime = System.currentTimeMillis();
            SharedPreferences.Editor editor = preferences.edit();
            boolean hasChanges = false;
            
            // 清理内存缓存
            memoryCache.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    Log.d(TAG, "清理过期内存缓存: " + entry.getKey());
                    return true;
                }
                return false;
            });
            
            // 清理持久化缓存
            Map<String, ?> allEntries = preferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith(TIMESTAMP_PREFIX)) {
                    long expireTime = (Long) entry.getValue();
                    if (expireTime <= currentTime) {
                        String cacheKey = entry.getKey().substring(TIMESTAMP_PREFIX.length());
                        editor.remove(CACHE_PREFIX + cacheKey);
                        editor.remove(TIMESTAMP_PREFIX + cacheKey);
                        hasChanges = true;
                        Log.d(TAG, "清理过期持久化缓存: " + cacheKey);
                    }
                }
            }
            
            if (hasChanges) {
                editor.apply();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "清理过期缓存失败", e);
        }
    }
    
    /**
     * 获取缓存大小（KB）
     */
    public int getCacheSize() {
        try {
            int size = 0;
            
            // 计算内存缓存大小
            for (CacheEntry entry : memoryCache.values()) {
                size += entry.data.getBytes().length;
            }
            
            // 计算持久化缓存大小
            Map<String, ?> allEntries = preferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith(CACHE_PREFIX)) {
                    size += ((String) entry.getValue()).getBytes().length;
                }
            }
            
            return size / 1024; // 转换为KB
            
        } catch (Exception e) {
            Log.e(TAG, "计算缓存大小失败", e);
            return 0;
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getCacheStatistics() {
        try {
            int memoryCount = memoryCache.size();
            int persistentCount = 0;
            int expiredCount = 0;
            long currentTime = System.currentTimeMillis();
            
            Map<String, ?> allEntries = preferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().startsWith(CACHE_PREFIX)) {
                    persistentCount++;
                    
                    String timestampKey = TIMESTAMP_PREFIX + entry.getKey().substring(CACHE_PREFIX.length());
                    Long expireTime = (Long) allEntries.get(timestampKey);
                    if (expireTime != null && expireTime <= currentTime) {
                        expiredCount++;
                    }
                }
            }
            
            return new CacheStatistics(memoryCount, persistentCount, expiredCount, getCacheSize());
            
        } catch (Exception e) {
            Log.e(TAG, "获取缓存统计失败", e);
            return new CacheStatistics(0, 0, 0, 0);
        }
    }
    
    /**
     * 预热缓存
     */
    public void warmupCache() {
        try {
            // 启动一个线程来清理过期缓存
            new Thread(() -> {
                cleanupExpiredCache();
                Log.d(TAG, "缓存预热完成");
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "缓存预热失败", e);
        }
    }
    
    /**
     * 缓存条目类
     */
    private static class CacheEntry {
        final String data;
        final long expireTime;
        
        CacheEntry(String data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        public final int memoryCount;
        public final int persistentCount;
        public final int expiredCount;
        public final int sizeKB;
        
        CacheStatistics(int memoryCount, int persistentCount, int expiredCount, int sizeKB) {
            this.memoryCount = memoryCount;
            this.persistentCount = persistentCount;
            this.expiredCount = expiredCount;
            this.sizeKB = sizeKB;
        }
        
        @Override
        public String toString() {
            return String.format("缓存统计 - 内存: %d, 持久化: %d, 过期: %d, 大小: %dKB", 
                               memoryCount, persistentCount, expiredCount, sizeKB);
        }
    }
}