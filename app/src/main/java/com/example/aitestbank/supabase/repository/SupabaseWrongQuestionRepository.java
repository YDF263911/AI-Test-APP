package com.example.aitestbank.supabase.repository;

import android.util.Log;
import com.example.aitestbank.model.SupabaseWrongQuestion;
import com.example.aitestbank.supabase.SupabaseClientManager;
import com.example.aitestbank.supabase.SupabaseClientManager.OperationCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supabase错题数据仓库
 * 提供错题相关的数据库操作，基于SupabaseClientManager的实际方法
 */
public class SupabaseWrongQuestionRepository {
    private static final String TAG = "SupabaseWrongQuestionRepo";
    private static SupabaseWrongQuestionRepository instance;
    
    private final SupabaseClientManager supabaseClient;
    
    private SupabaseWrongQuestionRepository() {
        supabaseClient = SupabaseClientManager.getInstance();
    }
    
    public static synchronized SupabaseWrongQuestionRepository getInstance() {
        if (instance == null) {
            instance = new SupabaseWrongQuestionRepository();
        }
        return instance;
    }
    
    /**
     * 添加错题
     */
    public void addWrongQuestion(SupabaseWrongQuestion wrongQuestion, OperationCallback<String> callback) {
        try {
            supabaseClient.addWrongQuestion(wrongQuestion, new OperationCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    callback.onSuccess(wrongQuestion.getId());
                }
                
                @Override
                public void onError(Exception error) {
                    callback.onError(error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "添加错题失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 获取用户的错题列表
     */
    public void getUserWrongQuestions(String userId, OperationCallback<List<SupabaseWrongQuestion>> callback) {
        try {
            supabaseClient.getUserWrongQuestions(userId, new OperationCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        List<SupabaseWrongQuestion> wrongQuestions = parseWrongQuestions(result);
                        callback.onSuccess(wrongQuestions);
                    } catch (Exception e) {
                        Log.e(TAG, "解析错题数据失败", e);
                        callback.onError(e);
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    callback.onError(error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取错题列表失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 更新错题掌握程度
     */
    public void updateWrongQuestionMastery(String questionId, int masteryLevel, boolean isMastered, OperationCallback<Boolean> callback) {
        try {
            supabaseClient.updateWrongQuestionMastery(questionId, masteryLevel, isMastered, new OperationCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    callback.onSuccess(true);
                }
                
                @Override
                public void onError(Exception error) {
                    callback.onError(error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "更新掌握程度失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 删除错题
     */
    public void deleteWrongQuestion(String questionId, OperationCallback<Boolean> callback) {
        try {
            supabaseClient.deleteWrongQuestion(questionId, new OperationCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    callback.onSuccess(true);
                }
                
                @Override
                public void onError(Exception error) {
                    callback.onError(error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "删除错题失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 获取错题统计信息
     */
    public void getWrongQuestionStatistics(String userId, OperationCallback<Map<String, Integer>> callback) {
        try {
            getUserWrongQuestions(userId, new OperationCallback<List<SupabaseWrongQuestion>>() {
                @Override
                public void onSuccess(List<SupabaseWrongQuestion> wrongQuestions) {
                    Map<String, Integer> statistics = new HashMap<>();
                    statistics.put("total", wrongQuestions.size());
                    statistics.put("mastered", 0);
                    statistics.put("learning", 0);
                    statistics.put("difficult", 0);
                    
                    for (SupabaseWrongQuestion wrongQuestion : wrongQuestions) {
                        if (wrongQuestion.getMastered() != null && wrongQuestion.getMastered()) {
                            statistics.put("mastered", statistics.get("mastered") + 1);
                        } else if (wrongQuestion.getMasteryLevel() != null && wrongQuestion.getMasteryLevel() >= 3) {
                            statistics.put("learning", statistics.get("learning") + 1);
                        } else {
                            statistics.put("difficult", statistics.get("difficult") + 1);
                        }
                    }
                    
                    callback.onSuccess(statistics);
                }
                
                @Override
                public void onError(Exception error) {
                    callback.onError(error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "获取错题统计失败", e);
            callback.onError(e);
        }
    }
    
    /**
     * 解析错题数据
     */
    private List<SupabaseWrongQuestion> parseWrongQuestions(String jsonResult) {
        List<SupabaseWrongQuestion> wrongQuestions = new ArrayList<>();
        
        if (jsonResult == null || jsonResult.trim().isEmpty() || "[]".equals(jsonResult.trim())) {
            return wrongQuestions;
        }
        
        try {
            // 这里需要根据实际的JSON解析库来实现
            // 暂时返回空列表，需要集成Gson或其他JSON库
            Log.d(TAG, "错题JSON数据: " + jsonResult);
            
        } catch (Exception e) {
            Log.e(TAG, "解析错题数据失败", e);
        }
        
        return wrongQuestions;
    }
}