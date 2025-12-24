package com.example.aitestbank.supabase;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 简化版Supabase客户端 - 使用HTTP直接访问
 */
public class SimpleSupabaseClient {
    
    private static final String TAG = "SimpleSupabaseClient";
    private static volatile SimpleSupabaseClient instance;
    
    private OkHttpClient httpClient;
    private Gson gson;
    private String supabaseUrl;
    private String supabaseKey;
    
    private SimpleSupabaseClient() {
        gson = new Gson();
        
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public static SimpleSupabaseClient getInstance() {
        if (instance == null) {
            synchronized (SimpleSupabaseClient.class) {
                if (instance == null) {
                    instance = new SimpleSupabaseClient();
                }
            }
        }
        return instance;
    }
    
    public void initialize(Context context, String url, String key) {
        this.supabaseUrl = url;
        this.supabaseKey = key;
        Log.d(TAG, "Supabase client initialized with URL: " + url);
    }
    
    /**
     * 执行REST查询
     */
    public String query(String tableName, String select, String filter) throws IOException {
        String url = String.format("%s/rest/v1/%s?select=%s", supabaseUrl, tableName, select);
        
        if (filter != null && !filter.isEmpty()) {
            url += "&" + filter;
        }
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Query failed: " + response.code() + " " + response.message());
                throw new IOException("Query failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "Query response: " + responseBody);
            return responseBody;
        }
    }
    
    /**
     * 插入数据
     */
    public String insert(String tableName, Object data) throws IOException {
        String url = String.format("%s/rest/v1/%s", supabaseUrl, tableName);
        String json = gson.toJson(data);
        
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Insert failed: " + response.code() + " " + response.message());
                throw new IOException("Insert failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "Insert response: " + responseBody);
            return responseBody;
        }
    }
    
    /**
     * 更新数据
     */
    public String update(String tableName, Object data, String filter) throws IOException {
        String url = String.format("%s/rest/v1/%s?%s", supabaseUrl, tableName, filter);
        String json = gson.toJson(data);
        
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Update failed: " + response.code() + " " + response.message());
                throw new IOException("Update failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "Update response: " + responseBody);
            return responseBody;
        }
    }
    
    /**
     * 删除数据
     */
    public String delete(String tableName, String filter) throws IOException {
        String url = String.format("%s/rest/v1/%s?%s", supabaseUrl, tableName, filter);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .delete()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Delete failed: " + response.code() + " " + response.message());
                throw new IOException("Delete failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "Delete response: " + responseBody);
            return responseBody;
        }
    }
    
    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            // 测试查询questions表
            String result = query("questions", "count", "limit=1");
            Log.d(TAG, "Connection test successful: " + result);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Connection test failed", e);
            return false;
        }
    }
}