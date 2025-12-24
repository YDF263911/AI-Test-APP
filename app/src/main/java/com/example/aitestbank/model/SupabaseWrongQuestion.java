package com.example.aitestbank.model;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 错题数据模型 - 与Supabase wrong_questions表结构完全对齐
 * 基于MCP工具查询的实际表结构生成
 */
public class SupabaseWrongQuestion {
    
    @SerializedName("id")
    private String id; // TEXT PRIMARY KEY - UUID
    
    @SerializedName("user_id")
    private String userId; // TEXT - 关联user_profiles.id
    
    @SerializedName("question_id")
    private String questionId; // TEXT - 关联questions.id
    
    @SerializedName("question_title")
    private String questionTitle; // TEXT - 题目标题
    
    @SerializedName("options")
    private List<String> options; // ARRAY - 选项数组
    
    @SerializedName("correct_answer")
    private Integer correctAnswer; // INTEGER - 正确答案
    
    @SerializedName("user_answer")
    private Integer userAnswer; // INTEGER - 用户答案
    
    @SerializedName("analysis")
    private String analysis; // TEXT - 解析
    
    @SerializedName("ai_analysis")
    private String aiAnalysis; // TEXT - AI解析
    
    @SerializedName("knowledge_points")
    private List<String> knowledgePoints; // ARRAY - 知识点
    
    @SerializedName("difficulty")
    private Integer difficulty; // INTEGER - 难度1-5
    
    @SerializedName("category")
    private String category; // TEXT - 分类
    
    @SerializedName("subject")
    private String subject; // TEXT - 学科
    
    @SerializedName("source")
    private String source; // TEXT - 来源
    
    @SerializedName("type")
    private String type; // TEXT - 题型
    
    @SerializedName("wrong_reason")
    private String wrongReason; // TEXT - 错误原因
    
    @SerializedName("review_count")
    private Integer reviewCount = 1; // INTEGER DEFAULT 1 - 复习次数
    
    @SerializedName("mastery_level")
    private Integer masteryLevel; // INTEGER - 掌握级别1-5
    
    @SerializedName("is_mastered")
    private Boolean isMastered = false; // BOOLEAN DEFAULT false - 是否掌握
    
    @SerializedName("next_review_date")
    private String nextReviewDate; // DATE - 下次复习日期
    
    @SerializedName("last_review_date")
    private String lastReviewDate; // DATE - 最后复习日期
    
    @SerializedName("created_at")
    private String createdAt; // TIMESTAMPTZ DEFAULT NOW()
    
    @SerializedName("updated_at")
    private String updatedAt; // TIMESTAMPTZ DEFAULT NOW()
    
    // 构造函数
    public SupabaseWrongQuestion() {
    }
    
    public SupabaseWrongQuestion(String userId, String questionId, String questionTitle, 
                               Integer correctAnswer, Integer userAnswer) {
        this.id = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
        this.reviewCount = 1;
        this.isMastered = false;
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        this.updatedAt = this.createdAt;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getQuestionTitle() {
        return questionTitle;
    }
    
    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public Integer getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(Integer correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public Integer getUserAnswer() {
        return userAnswer;
    }
    
    public void setUserAnswer(Integer userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public String getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    
    public String getAiAnalysis() {
        return aiAnalysis;
    }
    
    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
    
    public List<String> getKnowledgePoints() {
        return knowledgePoints;
    }
    
    public void setKnowledgePoints(List<String> knowledgePoints) {
        this.knowledgePoints = knowledgePoints;
    }
    
    public Integer getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getWrongReason() {
        return wrongReason;
    }
    
    public void setWrongReason(String wrongReason) {
        this.wrongReason = wrongReason;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public Integer getMasteryLevel() {
        return masteryLevel;
    }
    
    public void setMasteryLevel(Integer masteryLevel) {
        // 约束在1-5范围内，与数据库CHECK约束保持一致
        if (masteryLevel != null) {
            this.masteryLevel = Math.max(1, Math.min(5, masteryLevel));
        }
    }
    
    public Boolean getMastered() {
        return isMastered;
    }
    
    public void setMastered(Boolean mastered) {
        isMastered = mastered;
    }
    
    public String getNextReviewDate() {
        return nextReviewDate;
    }
    
    public void setNextReviewDate(String nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }
    
    public String getLastReviewDate() {
        return lastReviewDate;
    }
    
    public void setLastReviewDate(String lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "SupabaseWrongQuestion{" +
                "id='" + id + '\'' +
                ", questionTitle='" + questionTitle + '\'' +
                ", userId='" + userId + '\'' +
                ", reviewCount=" + reviewCount +
                ", masteryLevel=" + masteryLevel +
                ", isMastered=" + isMastered +
                '}';
    }
}