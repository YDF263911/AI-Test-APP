package com.example.aitestbank.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Body;

import com.example.aitestbank.model.Question;
import com.example.aitestbank.model.SupabaseWrongQuestion;

import java.util.List;
import java.util.Map;

/**
 * API服务接口 - 定义后端API调用
 */
public interface ApiService {
    
    /**
     * 获取题目列表
     */
    @GET("api/questions")
    Call<List<Question>> getQuestions(@Query("category") String category, 
                                     @Query("difficulty") int difficulty,
                                     @Query("page") int page,
                                     @Query("limit") int limit);
    
    /**
     * 获取题目详情
     */
    @GET("api/questions/{id}")
    Call<Question> getQuestionDetail(@Query("id") String questionId);
    
    /**
     * 提交答题记录
     */
    @POST("api/answers")
    Call<Map<String, Object>> submitAnswer(@Body Map<String, Object> answerData);
    
    /**
     * 获取错题列表
     */
    @GET("api/wrong-questions")
    Call<List<SupabaseWrongQuestion>> getWrongQuestions(@Query("userId") String userId);
    
    /**
     * AI解析接口
     */
    @POST("api/ai/analysis")
    Call<Map<String, Object>> getAIAnalysis(@Body Map<String, Object> requestData);
    
    /**
     * AI答疑接口
     */
    @POST("api/ai/qa")
    Call<Map<String, Object>> askAI(@Body Map<String, Object> questionData);
    
    /**
     * 获取推荐题目
     */
    @GET("api/recommendations")
    Call<List<Question>> getRecommendedQuestions(@Query("userId") String userId,
                                                @Query("limit") int limit);
}