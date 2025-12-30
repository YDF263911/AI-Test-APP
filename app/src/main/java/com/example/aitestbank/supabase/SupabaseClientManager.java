package com.example.aitestbank.supabase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.aitestbank.model.SupabaseUserProfile;
import com.example.aitestbank.model.SupabaseWrongQuestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Supabase客户端管理器 - 扩展版本支持错题本API
 * 基于MCP工具查询的实际表结构实现
 */
public class SupabaseClientManager {
    
    private static final String TAG = "SupabaseClientManager";
    
    // Supabase配置 - 使用MCP查询到的有效API密钥
    private static final String SUPABASE_URL = "https://jypjsjbkspmsutmdvelq.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4";
    
    private static volatile SupabaseClientManager instance;
    private Context appContext;
    private Handler mainHandler;
    
    private SupabaseClientManager() {
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static SupabaseClientManager getInstance() {
        if (instance == null) {
            instance = new SupabaseClientManager();
        }
        return instance;
    }
    
    /**
     * 初始化Supabase配置
     */
    public void initialize(Context context) {
        this.appContext = context.getApplicationContext();
        Log.i(TAG, "Supabase客户端初始化完成 - URL: " + SUPABASE_URL);
    }
    
    // ==================== 错题本相关API（基于实际表结构）====================
    
    /**
     * 添加错题记录 - 对应wrong_questions表的实际字段
     */
    public void addWrongQuestion(@NonNull SupabaseWrongQuestion wrongQuestion, @NonNull OperationCallback<Void> callback) {
        Log.d(TAG, "开始添加错题记录: " + wrongQuestion.getQuestionTitle());
        
        new Thread(() -> {
            try {
                String jsonData = buildWrongQuestionJson(wrongQuestion);
                String endpoint = SUPABASE_URL + "/rest/v1/wrong_questions";
                
                String result = performPostRequest(endpoint, jsonData);
                
                mainHandler.post(() -> {
                    if (result != null && !result.isEmpty()) {
                        Log.i(TAG, "错题记录添加成功: " + wrongQuestion.getQuestionTitle());
                        callback.onSuccess(null);
                    } else {
                        Log.e(TAG, "错题记录添加失败: 服务器返回空结果");
                        callback.onError(new Exception("Server returned empty result"));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "错题记录添加异常", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 获取用户的错题列表 - 按掌握程度排序
     */
    public void getUserWrongQuestions(@NonNull String userId, @NonNull OperationCallback<String> callback) {
        Log.d(TAG, "开始获取用户错题列表: " + userId);
        
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/wrong_questions" +
                    "?user_id=eq." + userId +
                    "&order=mastery_level.desc,next_review_date.asc" +
                    "&select=*,questions(*)";
                
                String result = performGetRequest(endpoint);
                
                mainHandler.post(() -> {
                    if (result != null) {
                        Log.i(TAG, "获取用户错题列表成功");
                        callback.onSuccess(result);
                    } else {
                        callback.onError(new Exception("Failed to fetch wrong questions"));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "获取用户错题列表失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 更新错题掌握程度 - 对应mastery_level字段
     */
    public void updateWrongQuestionMastery(@NonNull String questionId, int masteryLevel, boolean isMastered, @NonNull OperationCallback<Void> callback) {
        Log.d(TAG, "开始更新错题掌握程度: " + questionId + ", level: " + masteryLevel);
        
        new Thread(() -> {
            try {
                // 约束在1-5范围内，与数据库CHECK约束保持一致
                int constrainedLevel = Math.max(1, Math.min(5, masteryLevel));
                
                String updateData = String.format(
                    "{\"mastery_level\": %d, \"is_mastered\": %b, \"last_review_date\": \"%s\", \"updated_at\": \"%s\"}",
                    constrainedLevel, isMastered, formatDate(new Date()), formatTimestamp(new Date())
                );
                
                String endpoint = SUPABASE_URL + "/rest/v1/wrong_questions?id=eq." + questionId;
                
                String result = performPatchRequest(endpoint, updateData);
                
                mainHandler.post(() -> {
                    if (result != null) {
                        Log.i(TAG, "错题掌握程度更新成功");
                        callback.onSuccess(null);
                    } else {
                        callback.onError(new Exception("Update operation failed"));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "更新错题掌握程度失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 删除错题记录
     */
    public void deleteWrongQuestion(@NonNull String questionId, @NonNull OperationCallback<Void> callback) {
        Log.d(TAG, "开始删除错题记录: " + questionId);
        
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/wrong_questions?id=eq." + questionId;
                
                String result = performDeleteRequest(endpoint);
                
                mainHandler.post(() -> {
                    if (result != null) {
                        Log.i(TAG, "错题记录删除成功");
                        callback.onSuccess(null);
                    } else {
                        callback.onError(new Exception("Delete operation failed"));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "删除错题记录失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    // ==================== 用户档案相关API（基于user_profiles表）====================
    
    /**
     * 获取或创建用户档案 - 对应user_profiles表
     */
    public void getUserProfile(@NonNull String deviceId, @NonNull OperationCallback<String> callback) {
        Log.d(TAG, "开始获取用户档案: " + deviceId);
        
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/user_profiles?device_id=eq." + deviceId;
                
                String result = performGetRequest(endpoint);
                
                if (result == null || result.equals("[]")) {
                    // 用户不存在，创建新用户档案
                    createDefaultUserProfile(deviceId, callback);
                } else {
                    mainHandler.post(() -> {
                        Log.i(TAG, "获取用户档案成功");
                        callback.onSuccess(result);
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "获取用户档案失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 按用户ID获取用户档案 - 用于登录用户
     */
    public void getUserProfileById(@NonNull String userId, @NonNull OperationCallback<String> callback) {
        Log.d(TAG, "开始获取用户档案(按ID): " + userId);
        
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/user_profiles?id=eq." + userId;
                
                String result = performGetRequest(endpoint);
                
                mainHandler.post(() -> {
                    if (result != null && !result.equals("[]")) {
                        Log.i(TAG, "获取用户档案成功(按ID)");
                        callback.onSuccess(result);
                    } else {
                        Log.w(TAG, "用户档案不存在(按ID): " + userId);
                        callback.onError(new Exception("User profile not found"));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "获取用户档案失败(按ID)", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 更新用户学习统计 - 对应user_profiles表中的统计字段
     */
    public void updateUserStudyStats(@NonNull String deviceId, long totalQuestions, long correctQuestions, int studyDays, @NonNull OperationCallback<Void> callback) {
        Log.d(TAG, "开始更新用户学习统计: " + deviceId);
        
        new Thread(() -> {
            try {
                String updateData = String.format(
                    "{\"total_questions\": %d, \"correct_questions\": %d, \"study_days\": %d, \"last_study_date\": \"%s\", \"updated_at\": \"%s\"}",
                    totalQuestions, correctQuestions, studyDays, formatDate(new Date()), formatTimestamp(new Date())
                );
                
                String endpoint = SUPABASE_URL + "/rest/v1/user_profiles?device_id=eq." + deviceId;
                
                String result = performPatchRequest(endpoint, updateData);
                
                mainHandler.post(() -> {
                    if (result != null) {
                        Log.i(TAG, "用户学习统计更新成功");
                        callback.onSuccess(null);
                    } else {
                        callback.onError(new Exception("Update operation failed"));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "更新用户学习统计失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 构建错题记录的JSON数据
     */
    private String buildWrongQuestionJson(SupabaseWrongQuestion wrongQuestion) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\": \"").append(wrongQuestion.getId() != null ? wrongQuestion.getId() : UUID.randomUUID().toString()).append("\",");
        json.append("\"user_id\": \"").append(wrongQuestion.getUserId()).append("\",");
        json.append("\"question_id\": \"").append(wrongQuestion.getQuestionId()).append("\",");
        json.append("\"question_title\": \"").append(escapeJson(wrongQuestion.getQuestionTitle())).append("\",");
        json.append("\"correct_answer\": ").append(wrongQuestion.getCorrectAnswer()).append(",");
        json.append("\"user_answer\": ").append(wrongQuestion.getUserAnswer()).append(",");
        json.append("\"review_count\": ").append(wrongQuestion.getReviewCount() != null ? wrongQuestion.getReviewCount() : 1).append(",");
        json.append("\"mastery_level\": ").append(wrongQuestion.getMasteryLevel() != null ? wrongQuestion.getMasteryLevel() : 1).append(",");
        json.append("\"is_mastered\": ").append(wrongQuestion.getMastered() != null ? wrongQuestion.getMastered() : false);
        
        if (wrongQuestion.getOptions() != null && !wrongQuestion.getOptions().isEmpty()) {
            json.append(",\"options\": [");
            for (int i = 0; i < wrongQuestion.getOptions().size(); i++) {
                if (i > 0) json.append(",");
                json.append("\"").append(escapeJson(wrongQuestion.getOptions().get(i))).append("\"");
            }
            json.append("]");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 创建默认用户档案
     */
    private void createDefaultUserProfile(String deviceId, OperationCallback<String> callback) {
        try {
            String defaultProfile = String.format(
                "{\"id\": \"%s\", \"device_id\": \"%s\", \"username\": \"用户_%s\", \"display_name\": \"AI刷题用户\", \"daily_goal\": 20, \"total_questions\": 0, \"correct_questions\": 0, \"study_days\": 0, \"study_streak\": 0, \"is_premium\": false}",
                UUID.randomUUID().toString(), deviceId, deviceId.substring(0, Math.min(8, deviceId.length()))
            );
            
            String endpoint = SUPABASE_URL + "/rest/v1/user_profiles";
            String result = performPostRequest(endpoint, defaultProfile);
            
            mainHandler.post(() -> {
                if (result != null) {
                    Log.i(TAG, "默认用户档案创建成功");
                    callback.onSuccess(result);
                } else {
                    callback.onError(new Exception("Failed to create default user profile"));
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "创建默认用户档案失败", e);
            mainHandler.post(() -> callback.onError(e));
        }
    }
    
    /**
     * 执行GET请求
     */
    private String performGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                Log.e(TAG, "GET请求失败: " + responseCode);
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 执行POST请求
     */
    private String performPostRequest(String urlString, String jsonData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Prefer", "return=minimal");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                Log.e(TAG, "POST请求失败: " + responseCode + " - " + readErrorResponse(connection));
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 执行PATCH请求
     */
    private String performPatchRequest(String urlString, String jsonData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Prefer", "return=minimal");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return "success";
            } else {
                Log.e(TAG, "PATCH请求失败: " + responseCode + " - " + readErrorResponse(connection));
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 执行DELETE请求
     */
    private String performDeleteRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
            connection.setRequestProperty("Prefer", "return=minimal");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return "success";
            } else {
                Log.e(TAG, "DELETE请求失败: " + responseCode + " - " + readErrorResponse(connection));
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 读取HTTP响应
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
    
    /**
     * 读取HTTP错误响应
     */
    private String readErrorResponse(HttpURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();
            
            return error.toString();
        } catch (Exception e) {
            return "Unknown error";
        }
    }
    
    /**
     * 格式化日期为YYYY-MM-DD
     */
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 格式化时间戳
     */
    private String formatTimestamp(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * 通用操作回调接口
     */
    public interface OperationCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}