package com.example.aitestbank.supabase;

import android.content.Context;
import android.util.Log;

import com.example.aitestbank.supabase.auth.DeviceIdManager;
import com.example.aitestbank.ui.adapter.WrongQuestionAdapter;
import com.example.aitestbank.utils.OperationCallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private Context context;
    
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
        this.context = context.getApplicationContext();
        this.supabaseUrl = url;
        this.supabaseKey = key;
        Log.d(TAG, "Supabase client initialized with URL: " + url + ", deviceId: " + DeviceIdManager.getShortDeviceId(context));
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
        
        // 处理JSONObject类型，确保只发送纯JSON数据
        String json;
        if (data instanceof org.json.JSONObject) {
            json = data.toString(); // JSONObject的toString()方法返回纯JSON
        } else {
            json = gson.toJson(data);
        }
        
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        
        // 获取设备ID用于RLS策略
        String deviceId = getDeviceId();
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation");
        
        // 设置会话变量用于RLS策略
        if (deviceId != null && !deviceId.isEmpty()) {
            requestBuilder.addHeader("X-Client-Info", "device_id=" + deviceId);
        }
        
        Request request = requestBuilder.post(body).build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body().string();
                Log.e(TAG, "Insert failed: " + response.code() + " " + response.message() + " - " + errorBody);
                throw new IOException("Insert failed: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "Insert response: " + responseBody);
            return responseBody;
        }
    }
    
    /**
     * 获取设备ID
     */
    private String getDeviceId() {
        try {
            // 使用DeviceIdManager获取真实的设备ID
            return DeviceIdManager.getDeviceId(context);
        } catch (Exception e) {
            Log.e(TAG, "获取设备ID失败，使用降级方案", e);
            // 降级方案：生成临时UUID
            return "fallback_" + java.util.UUID.randomUUID().toString();
        }
    }
    
    /**
     * 更新数据
     */
    public String update(String tableName, Object data, String filter) throws IOException {
        String url = String.format("%s/rest/v1/%s?%s", supabaseUrl, tableName, filter);
        
        // 处理JSONObject类型，确保只发送纯JSON数据
        String json;
        if (data instanceof org.json.JSONObject) {
            json = data.toString(); // JSONObject的toString()方法返回纯JSON
        } else {
            json = gson.toJson(data);
        }
        
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
                String errorBody = response.body().string();
                Log.e(TAG, "Update failed: " + response.code() + " " + response.message() + " - " + errorBody);
                throw new IOException("Update failed: " + response.code() + " - " + errorBody);
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
    
    /**
     * 保存答题记录到answer_records表
     */
    public void saveAnswerRecord(String questionId, Integer userAnswer, boolean isCorrect, int answerTime, String sessionId, OperationCallback<String> callback) {
        new Thread(() -> {
            try {
                // 获取当前用户ID
                com.example.aitestbank.supabase.auth.AuthManager authManager =
                    com.example.aitestbank.supabase.auth.AuthManager.getInstance(context);
                String userId = authManager.getCurrentUserId();
                
                // 构建答题记录数据
                JsonObject answerRecord = new JsonObject();
                answerRecord.addProperty("id", java.util.UUID.randomUUID().toString());
                answerRecord.addProperty("user_id", userId); // 添加用户ID字段
                answerRecord.addProperty("question_id", questionId);
                answerRecord.addProperty("user_answer", userAnswer);
                answerRecord.addProperty("is_correct", isCorrect);
                answerRecord.addProperty("answer_time", answerTime);
                answerRecord.addProperty("session_id", sessionId);
                
                // 插入到answer_records表
                String result = insert("answer_records", answerRecord);
                Log.d(TAG, "答题记录保存成功 (userId=" + userId + "): " + result);
                
                if (callback != null) {
                    callback.onSuccess(result);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "保存答题记录失败", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }

    /**
     * 更新用户统计数据
     */
    public void updateUserStatistics(int totalQuestions, int correctCount, double accuracyRate, OperationCallback<String> callback) {
        new Thread(() -> {
            try {
                // 获取当前用户ID
                com.example.aitestbank.supabase.auth.AuthManager authManager = 
                    com.example.aitestbank.supabase.auth.AuthManager.getInstance(context);
                String currentUserId = authManager.getCurrentUserId();
                
                // 更新user_profiles表的统计数据
                JsonObject statsData = new JsonObject();
                statsData.addProperty("total_questions", totalQuestions);
                statsData.addProperty("correct_questions", correctCount);
                
                // 更新last_study_date为今天
                String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                statsData.addProperty("last_study_date", today);
                
                // 查询当前的study_days和last_study_date
                try {
                    String userProfileResult = query("user_profiles", "study_days,last_study_date", "id=eq." + currentUserId + "&limit=1");
                    org.json.JSONArray jsonArray = new org.json.JSONArray(userProfileResult);
                    
                    if (jsonArray.length() > 0) {
                        org.json.JSONObject userProfile = jsonArray.getJSONObject(0);
                        String lastStudyDate = userProfile.optString("last_study_date", "");
                        int currentStudyDays = userProfile.optInt("study_days", 0);
                        
                        // 如果上次学习日期不是今天，增加学习天数
                        if (!today.equals(lastStudyDate)) {
                            statsData.addProperty("study_days", currentStudyDays + 1);
                            Log.d(TAG, "学习天数+1: " + (currentStudyDays + 1));
                        } else {
                            statsData.addProperty("study_days", currentStudyDays);
                            Log.d(TAG, "今天已学习，学习天数不变: " + currentStudyDays);
                        }
                    } else {
                        statsData.addProperty("study_days", 1);
                        Log.d(TAG, "首次学习，学习天数设为1");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "查询用户资料失败，设置默认学习天数为1", e);
                    statsData.addProperty("study_days", 1);
                }
                
                // 更新数据库
                String result = update("user_profiles", statsData, "id=eq." + currentUserId);
                Log.d(TAG, "用户统计数据更新成功: 总题数=" + totalQuestions + 
                      ", 正确数=" + correctCount + ", 正确率=" + accuracyRate + "%");
                
                if (callback != null) {
                    callback.onSuccess(result);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "更新用户统计数据失败", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
    
    /**
     * 批量更新错题数据
     */
    public void batchUpdateWrongQuestions(List<JsonObject> wrongQuestionUpdates, OperationCallback<String> callback) {
        new Thread(() -> {
            try {
                // 批量更新错题记录
                for (JsonObject updateData : wrongQuestionUpdates) {
                    String wrongQuestionId = updateData.get("id").getAsString();
                    String result = update("wrong_questions", updateData, "id=eq." + wrongQuestionId);
                    Log.d(TAG, "更新错题记录结果: " + result);
                }
                
                String finalResult = "批量更新成功，共更新 " + wrongQuestionUpdates.size() + " 条记录";
                
                if (callback != null) {
                    callback.onSuccess(finalResult);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "批量更新错题数据失败", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
    
    /**
     * 同步本地错题数据到云端
     */
    public void syncWrongQuestionsToCloud(List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestions, OperationCallback<String> callback) {
        new Thread(() -> {
            try {
                List<JsonObject> updates = new ArrayList<>();
                
                for (WrongQuestionAdapter.WrongQuestionItem wrongQuestion : wrongQuestions) {
                    JsonObject updateData = new JsonObject();
                    updateData.addProperty("id", wrongQuestion.getId());
                    updateData.addProperty("is_mastered", wrongQuestion.isMastered());
                    // 使用正确的字段名：review_count 而不是 wrong_count
                    updateData.addProperty("review_count", wrongQuestion.getWrongCount());
                    // 使用正确的日期格式
                    String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis()));
                    updateData.addProperty("last_review_date", currentDate);
                    
                    updates.add(updateData);
                }
                
                if (!updates.isEmpty()) {
                    batchUpdateWrongQuestions(updates, callback);
                } else {
                    if (callback != null) {
                        callback.onSuccess("没有需要同步的数据");
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "同步错题数据到云端失败", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
}