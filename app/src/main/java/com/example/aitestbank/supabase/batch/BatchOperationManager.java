package com.example.aitestbank.supabase.batch;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.model.SupabaseUserProfile;
import com.example.aitestbank.model.SupabaseWrongQuestion;
import com.example.aitestbank.supabase.SupabaseClientManager;
import com.example.aitestbank.supabase.SupabaseClientManager.OperationCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量操作管理器
 * 提供高效的批量数据库操作支持
 */
public class BatchOperationManager {
    private static final String TAG = "BatchOperationManager";
    private static BatchOperationManager instance;
    
    private final SupabaseClientManager supabaseClient;
    private final Handler mainHandler;
    
    private BatchOperationManager() {
        supabaseClient = SupabaseClientManager.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized BatchOperationManager getInstance() {
        if (instance == null) {
            instance = new BatchOperationManager();
        }
        return instance;
    }
    
    /**
     * 批量插入错题
     */
    public void batchInsertWrongQuestions(List<SupabaseWrongQuestion> wrongQuestions, OperationCallback<Integer> callback) {
        new Thread(() -> {
            try {
                if (wrongQuestions == null || wrongQuestions.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(0));
                    return;
                }
                
                int totalInserted = 0;
                List<Exception> errors = new ArrayList<>();
                
                for (SupabaseWrongQuestion wrongQuestion : wrongQuestions) {
                    try {
                        // 使用CountDownLatch等待每个操作完成
                        final boolean[] success = {false};
                        final Exception[] error = {null};
                        
                        Thread.sleep(100); // 避免过于频繁的请求
                        
                        supabaseClient.addWrongQuestion(wrongQuestion, new OperationCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                synchronized (success) {
                                    success[0] = true;
                                    success.notify();
                                }
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                synchronized (error) {
                                    error[0] = e;
                                    success.notify();
                                }
                            }
                        });
                        
                        synchronized (success) {
                            try {
                                success.wait(5000); // 最多等待5秒
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        
                        if (success[0]) {
                            totalInserted++;
                        } else if (error[0] != null) {
                            errors.add(error[0]);
                        }
                        
                    } catch (Exception e) {
                        errors.add(e);
                        Log.e(TAG, "批量插入错题时出错: " + wrongQuestion.getQuestionTitle(), e);
                    }
                }
                
                final int finalCount = totalInserted;
                final List<Exception> finalErrors = errors;
                
                mainHandler.post(() -> {
                    if (finalErrors.isEmpty()) {
                        callback.onSuccess(finalCount);
                    } else {
                        Exception error = new Exception("批量操作部分失败: " + finalErrors.size() + "个错误");
                        for (Exception e : finalErrors) {
                            Log.w(TAG, "批量操作中的错误", e);
                        }
                        callback.onError(error);
                    }
                });
                
                Log.i(TAG, "批量插入错题完成: " + totalInserted + "/" + wrongQuestions.size());
                
            } catch (Exception e) {
                Log.e(TAG, "批量插入错题失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 批量更新错题掌握程度
     */
    public void batchUpdateWrongQuestionsMastery(List<String> questionIds, int masteryLevel, boolean isMastered, OperationCallback<Integer> callback) {
        new Thread(() -> {
            try {
                if (questionIds == null || questionIds.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(0));
                    return;
                }
                
                int totalUpdated = 0;
                List<Exception> errors = new ArrayList<>();
                
                for (String questionId : questionIds) {
                    try {
                        Thread.sleep(100); // 避免过于频繁的请求
                        
                        final boolean[] success = {false};
                        final Exception[] error = {null};
                        
                        supabaseClient.updateWrongQuestionMastery(questionId, masteryLevel, isMastered, new OperationCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                synchronized (success) {
                                    success[0] = true;
                                    success.notify();
                                }
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                synchronized (error) {
                                    error[0] = e;
                                    success.notify();
                                }
                            }
                        });
                        
                        synchronized (success) {
                            try {
                                success.wait(5000); // 最多等待5秒
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        
                        if (success[0]) {
                            totalUpdated++;
                        } else if (error[0] != null) {
                            errors.add(error[0]);
                        }
                        
                    } catch (Exception e) {
                        errors.add(e);
                        Log.e(TAG, "批量更新错题时出错: " + questionId, e);
                    }
                }
                
                final int finalCount = totalUpdated;
                final List<Exception> finalErrors = errors;
                
                mainHandler.post(() -> {
                    if (finalErrors.isEmpty()) {
                        callback.onSuccess(finalCount);
                    } else {
                        Exception error = new Exception("批量更新部分失败: " + finalErrors.size() + "个错误");
                        for (Exception e : finalErrors) {
                            Log.w(TAG, "批量更新中的错误", e);
                        }
                        callback.onError(error);
                    }
                });
                
                Log.i(TAG, "批量更新错题完成: " + totalUpdated + "/" + questionIds.size());
                
            } catch (Exception e) {
                Log.e(TAG, "批量更新错题失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 批量删除错题
     */
    public void batchDeleteWrongQuestions(List<String> questionIds, OperationCallback<Integer> callback) {
        new Thread(() -> {
            try {
                if (questionIds == null || questionIds.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(0));
                    return;
                }
                
                int totalDeleted = 0;
                List<Exception> errors = new ArrayList<>();
                
                for (String questionId : questionIds) {
                    try {
                        Thread.sleep(100); // 避免过于频繁的请求
                        
                        final boolean[] success = {false};
                        final Exception[] error = {null};
                        
                        supabaseClient.deleteWrongQuestion(questionId, new OperationCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                synchronized (success) {
                                    success[0] = true;
                                    success.notify();
                                }
                            }
                            
                            @Override
                            public void onError(Exception e) {
                                synchronized (error) {
                                    error[0] = e;
                                    success.notify();
                                }
                            }
                        });
                        
                        synchronized (success) {
                            try {
                                success.wait(5000); // 最多等待5秒
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        
                        if (success[0]) {
                            totalDeleted++;
                        } else if (error[0] != null) {
                            errors.add(error[0]);
                        }
                        
                    } catch (Exception e) {
                        errors.add(e);
                        Log.e(TAG, "批量删除错题时出错: " + questionId, e);
                    }
                }
                
                final int finalCount = totalDeleted;
                final List<Exception> finalErrors = errors;
                
                mainHandler.post(() -> {
                    if (finalErrors.isEmpty()) {
                        callback.onSuccess(finalCount);
                    } else {
                        Exception error = new Exception("批量删除部分失败: " + finalErrors.size() + "个错误");
                        for (Exception e : finalErrors) {
                            Log.w(TAG, "批量删除中的错误", e);
                        }
                        callback.onError(error);
                    }
                });
                
                Log.i(TAG, "批量删除错题完成: " + totalDeleted + "/" + questionIds.size());
                
            } catch (Exception e) {
                Log.e(TAG, "批量删除错题失败", e);
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
    
    /**
     * 事务操作接口
     */
    public interface TransactionOperation {
        boolean execute() throws Exception;
    }
}