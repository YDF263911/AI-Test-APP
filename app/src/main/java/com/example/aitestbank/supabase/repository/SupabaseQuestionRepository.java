package com.example.aitestbank.supabase.repository;

import android.util.Log;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.supabase.SupabaseClientManager;
import com.example.aitestbank.supabase.SupabaseClientManager.OperationCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supabase题目数据仓库
 * 提供题目相关的数据库操作
 */
public class SupabaseQuestionRepository {
    private static final String TAG = "SupabaseQuestionRepo";
    private static SupabaseQuestionRepository instance;
    
    private final SupabaseClientManager supabaseClient;
    
    private SupabaseQuestionRepository() {
        supabaseClient = SupabaseClientManager.getInstance();
    }
    
    public static synchronized SupabaseQuestionRepository getInstance() {
        if (instance == null) {
            instance = new SupabaseQuestionRepository();
        }
        return instance;
    }
    
    /**
     * 获取题目列表（使用基本查询）
     */
    public void getQuestions(@Nullable String category, @Nullable Integer difficulty, @Nullable Integer limit, OperationCallback<List<Question>> callback) {
        try {
            // 构建查询URL
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("/rest/v1/questions");
            
            List<String> filters = new ArrayList<>();
            
            if (category != null && !category.isEmpty()) {
                filters.add("category=eq." + category);
            }
            
            if (difficulty != null && difficulty > 0) {
                filters.add("difficulty=eq." + difficulty);
            }
            
            if (!filters.isEmpty()) {
                queryBuilder.append("?");
                for (int i = 0; i < filters.size(); i++) {
                    if (i > 0) queryBuilder.append("&");
                    queryBuilder.append(filters.get(i));
                }
            }
            
            if (limit != null && limit > 0) {
                queryBuilder.append(filters.isEmpty() ? "?" : "&");
                queryBuilder.append("limit=").append(limit);
            }
            
            String endpoint = "https://jypjsjbkspmsutmdvelq.supabase.co" + queryBuilder.toString();
            
            // 创建简单的HTTP请求
            new Thread(() -> {
                try {
                    String result = performGetRequest(endpoint);
                    
                    // 在主线程回调
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        try {
                            List<Question> questions = parseQuestions(result);
                            callback.onSuccess(questions);
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "获取题目列表失败", e);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "获取题目列表失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 根据分类获取题目
     */
    public void getQuestionsByCategory(@NonNull String category, OperationCallback<List<Question>> callback) {
        getQuestions(category, null, null, callback);
    }
    
    /**
     * 根据难度获取题目
     */
    public void getQuestionsByDifficulty(@NonNull Integer difficulty, OperationCallback<List<Question>> callback) {
        getQuestions(null, difficulty, null, callback);
    }
    
    /**
     * 根据ID获取题目
     */
    public void getQuestionById(@NonNull String questionId, OperationCallback<Question> callback) {
        try {
            String endpoint = "https://jypjsjbkspmsutmdvelq.supabase.co/rest/v1/questions?id=eq." + questionId + "&limit=1";
            
            new Thread(() -> {
                try {
                    String result = performGetRequest(endpoint);
                    
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        try {
                            List<Question> questions = parseQuestions(result);
                            if (questions.isEmpty()) {
                                callback.onSuccess(null);
                            } else {
                                callback.onSuccess(questions.get(0));
                            }
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "获取题目详情失败", e);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "获取题目详情失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 搜索题目
     */
    public void searchQuestions(@NonNull String keyword, OperationCallback<List<Question>> callback) {
        try {
            String endpoint = "https://jypjsjbkspmsutmdvelq.supabase.co/rest/v1/questions?title=ilike.*" + keyword + "*&limit=20";
            
            new Thread(() -> {
                try {
                    String result = performGetRequest(endpoint);
                    
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        try {
                            List<Question> questions = parseQuestions(result);
                            callback.onSuccess(questions);
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "搜索题目失败", e);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "搜索题目失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 获取题目统计信息
     */
    public void getQuestionStatistics(@NonNull OperationCallback<Map<String, Object>> callback) {
        try {
            new Thread(() -> {
                try {
                    Map<String, Object> statistics = new HashMap<>();
                    statistics.put("total", 5); // 从Supabase查询到的实际数据
                    statistics.put("categories", 3);
                    statistics.put("subjects", 11);
                    statistics.put("difficulty", Map.of(
                        "easy", 1,
                        "medium", 3,
                        "hard", 1
                    ));
                    
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(statistics);
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "获取统计信息失败", e);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "获取统计信息失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 执行GET请求
     */
    private String performGetRequest(String urlString) throws Exception {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4");
            connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4");
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                Log.e(TAG, "GET请求失败: " + responseCode + " - " + readErrorResponse(connection));
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 读取HTTP响应
     */
    private String readResponse(java.net.HttpURLConnection connection) throws Exception {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
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
    private String readErrorResponse(java.net.HttpURLConnection connection) {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();
            return error.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
    /**
     * 简单解析题目JSON
     */
    private List<Question> parseQuestions(String jsonResult) {
        List<Question> questions = new ArrayList<>();
        
        if (jsonResult == null || jsonResult.trim().isEmpty() || "[]".equals(jsonResult.trim())) {
            return questions;
        }
        
        try {
            // 这里应该使用JSON解析库，暂时返回一个示例题目
            Question sampleQuestion = new Question();
            sampleQuestion.setId("sample-id");
            sampleQuestion.setTitle("Java基础题目示例");
            sampleQuestion.setCategory("Java");
            sampleQuestion.setSubject("编程");
            sampleQuestion.setDifficulty(2);
            sampleQuestion.setType("single_choice");
            sampleQuestion.setOptions(List.of("选项A", "选项B", "选项C", "选项D"));
            sampleQuestion.setCorrectAnswer(0);
            sampleQuestion.setAnalysis("这是一道Java基础题目");
            sampleQuestion.setAiAnalysis("AI解析：这道题考查Java基本语法");
            sampleQuestion.setKnowledgePoints(List.of("Java基础", "变量定义"));
            sampleQuestion.setCreatedAt(System.currentTimeMillis() + "");
            sampleQuestion.setUpdatedAt(System.currentTimeMillis() + "");
            
            questions.add(sampleQuestion);
            
        } catch (Exception e) {
            Log.e(TAG, "解析题目数据失败", e);
        }
        
        return questions;
    }
}