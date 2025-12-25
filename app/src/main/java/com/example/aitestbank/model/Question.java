package com.example.aitestbank.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import java.util.Date;

/**
 * 简化的题目数据模型 - 不使用Room注解
 * 用于API接口的基本数据传输
 */
public class Question implements Serializable {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("options")
    private List<String> options;
    
    @SerializedName("correct_answer")
    private Integer correctAnswer;
    
    @SerializedName("analysis")
    private String analysis;
    
    @SerializedName("ai_analysis")
    private String aiAnalysis;
    
    @SerializedName("knowledge_points")
    private List<String> knowledgePoints;
    
    @SerializedName("difficulty")
    private Integer difficulty;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("subject")
    private String subject;
    
    @SerializedName("source")
    private String source;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("tags")
    private List<String> tags;
    
    @SerializedName("view_count")
    private Long viewCount;
    
    @SerializedName("correct_rate")
    private Double correctRate;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    // 构造函数
    public Question() {
    }
    
    public Question(String title, List<String> options, Integer correctAnswer) {
        this.title = title;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Double getCorrectRate() {
        return correctRate;
    }
    
    public void setCorrectRate(Double correctRate) {
        this.correctRate = correctRate;
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
        return "Question{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                ", difficulty=" + difficulty +
                ", correctAnswer=" + correctAnswer +
                ", viewCount=" + viewCount +
                ", correctRate=" + correctRate +
                '}';
    }
}