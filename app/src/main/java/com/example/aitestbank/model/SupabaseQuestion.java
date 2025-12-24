package com.example.aitestbank.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Date;

/**
 * Supabase专用的题目数据模型
 * 用于与Supabase数据库交互
 */
public class SupabaseQuestion {
    
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
    
    @SerializedName("created_at")
    private Date createdAt;
    
    @SerializedName("updated_at")
    private Date updatedAt;
    
    // 默认构造函数
    public SupabaseQuestion() {}
    
    // 从Question对象转换
    public SupabaseQuestion(Question question) {
        this.id = question.getId();
        this.title = question.getTitle();
        this.options = question.getOptions();
        this.correctAnswer = question.getCorrectAnswer();
        this.analysis = question.getAnalysis();
        this.aiAnalysis = question.getAiAnalysis();
        this.knowledgePoints = question.getKnowledgePoints();
        this.difficulty = question.getDifficulty();
        this.category = question.getCategory();
        this.subject = question.getSubject();
        this.source = question.getSource();
        this.type = question.getType();
        this.tags = question.getTags();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public Integer getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(Integer correctAnswer) { this.correctAnswer = correctAnswer; }
    
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    
    public String getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(String aiAnalysis) { this.aiAnalysis = aiAnalysis; }
    
    public List<String> getKnowledgePoints() { return knowledgePoints; }
    public void setKnowledgePoints(List<String> knowledgePoints) { this.knowledgePoints = knowledgePoints; }
    
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    // 转换为Question对象
    public Question toQuestion() {
        Question question = new Question();
        question.setId(this.id);
        question.setTitle(this.title);
        question.setOptions(this.options);
        question.setCorrectAnswer(this.correctAnswer);
        question.setAnalysis(this.analysis);
        question.setAiAnalysis(this.aiAnalysis);
        question.setKnowledgePoints(this.knowledgePoints);
        question.setDifficulty(this.difficulty);
        question.setCategory(this.category);
        question.setSubject(this.subject);
        question.setSource(this.source);
        question.setType(this.type);
        question.setTags(this.tags);
        return question;
    }
}