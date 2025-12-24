package com.example.aitestbank.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;
import androidx.room.ForeignKey;

import java.util.Date;

/**
 * 错题数据模型 - 本地数据库存储
 */
@Entity(
    tableName = "wrong_questions",
    indices = {
        @Index(value = {"user_id", "question_id"}, unique = true),
        @Index(value = {"user_id"}),
        @Index(value = {"subject"}),
        @Index(value = {"mastery_status"})
    },
    foreignKeys = {
        @ForeignKey(
            entity = User.class,
            parentColumns = "user_id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class WrongQuestion {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "question_id")
    private String questionId;
    
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "question_text")
    private String questionText;
    
    @ColumnInfo(name = "question_type")
    private String questionType;
    
    @ColumnInfo(name = "options")
    private String options; // JSON格式的选项数组
    
    @ColumnInfo(name = "correct_answer")
    private String correctAnswer;
    
    @ColumnInfo(name = "user_answer")
    private String userAnswer;
    
    @ColumnInfo(name = "explanation")
    private String explanation;
    
    @ColumnInfo(name = "subject")
    private String subject;
    
    @ColumnInfo(name = "difficulty")
    private String difficulty;
    
    @ColumnInfo(name = "wrong_count")
    private int wrongCount;
    
    @ColumnInfo(name = "mastery_status")
    private String masteryStatus; // not_mastered, partially_mastered, mastered
    
    @ColumnInfo(name = "priority")
    private int priority; // 1-5，5为最高优先级
    
    @ColumnInfo(name = "review_count")
    private int reviewCount;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "last_wrong_time")
    private long lastWrongTime;
    
    @ColumnInfo(name = "last_review_time")
    private long lastReviewTime;
    
    @ColumnInfo(name = "tags")
    private String tags; // JSON格式的标签数组
    
    @ColumnInfo(name = "is_marked")
    private boolean isMarked; // 是否被标记
    
    @ColumnInfo(name = "notes")
    private String notes; // 用户笔记
    
    // 构造函数
    public WrongQuestion() {
        this.createdAt = System.currentTimeMillis();
        this.lastWrongTime = System.currentTimeMillis();
        this.wrongCount = 1;
        this.masteryStatus = "not_mastered";
        this.priority = 3;
        this.reviewCount = 0;
        this.isMarked = false;
    }
    
    public WrongQuestion(String questionId, String userId, String questionText, 
                        String questionType, String options, String correctAnswer, 
                        String userAnswer, String explanation, String subject, 
                        String difficulty) {
        this();
        this.questionId = questionId;
        this.userId = userId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
        this.explanation = explanation;
        this.subject = subject;
        this.difficulty = difficulty;
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public String getOptions() {
        return options;
    }
    
    public void setOptions(String options) {
        this.options = options;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getUserAnswer() {
        return userAnswer;
    }
    
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public int getWrongCount() {
        return wrongCount;
    }
    
    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }
    
    public String getMasteryStatus() {
        return masteryStatus;
    }
    
    public void setMasteryStatus(String masteryStatus) {
        this.masteryStatus = masteryStatus;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getLastWrongTime() {
        return lastWrongTime;
    }
    
    public void setLastWrongTime(long lastWrongTime) {
        this.lastWrongTime = lastWrongTime;
    }
    
    public long getLastReviewTime() {
        return lastReviewTime;
    }
    
    public void setLastReviewTime(long lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public boolean isMarked() {
        return isMarked;
    }
    
    public void setMarked(boolean marked) {
        isMarked = marked;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // 业务方法
    
    /**
     * 增加错误次数
     */
    public void incrementWrongCount() {
        this.wrongCount++;
        this.lastWrongTime = System.currentTimeMillis();
    }
    
    /**
     * 增加复习次数
     */
    public void incrementReviewCount() {
        this.reviewCount++;
        this.lastReviewTime = System.currentTimeMillis();
    }
    
    /**
     * 标记为已掌握
     */
    public void markAsMastered() {
        this.masteryStatus = "mastered";
        this.lastReviewTime = System.currentTimeMillis();
        incrementReviewCount();
    }
    
    /**
     * 标记为部分掌握
     */
    public void markAsPartiallyMastered() {
        this.masteryStatus = "partially_mastered";
        this.lastReviewTime = System.currentTimeMillis();
        incrementReviewCount();
    }
    
    /**
     * 重置掌握状态
     */
    public void resetMasteryStatus() {
        this.masteryStatus = "not_mastered";
        this.priority = Math.min(5, this.priority + 1); // 提高优先级
    }
    
    /**
     * 检查是否已掌握
     */
    public boolean isMastered() {
        return "mastered".equals(masteryStatus);
    }
    
    /**
     * 检查是否需要复习
     */
    public boolean needsReview() {
        return !"mastered".equals(masteryStatus);
    }
    
    /**
     * 获取准确率（基于复习次数和掌握状态）
     */
    public double getMasteryRate() {
        if (reviewCount == 0) return 0.0;
        
        switch (masteryStatus) {
            case "mastered":
                return 0.8 + (0.2 * Math.min(1.0, reviewCount / 5.0));
            case "partially_mastered":
                return 0.5 + (0.3 * Math.min(1.0, reviewCount / 3.0));
            default:
                return Math.max(0.1, 0.3 - (wrongCount * 0.05));
        }
    }
    
    /**
     * 计算优先级分数（用于排序）
     */
    public double getPriorityScore() {
        double score = priority * 10; // 基础优先级
        
        // 错误次数越多，分数越高
        score += wrongCount * 2;
        
        // 最近错误的时间权重
        long daysSinceWrong = (System.currentTimeMillis() - lastWrongTime) / (1000 * 60 * 60 * 24);
        score += Math.max(0, 10 - daysSinceWrong); // 越近的错误分数越高
        
        // 掌握状态权重
        switch (masteryStatus) {
            case "not_mastered":
                score += 15;
                break;
            case "partially_mastered":
                score += 8;
                break;
            case "mastered":
                score -= 5;
                break;
        }
        
        // 标记的题目分数更高
        if (isMarked) {
            score += 20;
        }
        
        return score;
    }
    
    @Override
    public String toString() {
        return "WrongQuestion{" +
                "id=" + id +
                ", questionId='" + questionId + '\'' +
                ", userId='" + userId + '\'' +
                ", subject='" + subject + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", wrongCount=" + wrongCount +
                ", masteryStatus='" + masteryStatus + '\'' +
                ", priority=" + priority +
                ", createdAt=" + new Date(createdAt) +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        WrongQuestion that = (WrongQuestion) o;
        
        return questionId != null ? questionId.equals(that.questionId) && userId.equals(that.userId) : false;
    }
    
    @Override
    public int hashCode() {
        int result = questionId != null ? questionId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}