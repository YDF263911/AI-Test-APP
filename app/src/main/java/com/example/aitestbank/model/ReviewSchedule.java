package com.example.aitestbank.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 错题复习计划模型
 */
public class ReviewSchedule {
    private long firstReviewTime;           // 首次复习时间
    private List<Long> reviewTimes;         // 所有复习时间点
    private List<Long> completedReviews;    // 已完成的复习
    private String wrongQuestionId;         // 关联的错题ID
    
    public ReviewSchedule() {
        this.reviewTimes = new ArrayList<>();
        this.completedReviews = new ArrayList<>();
    }
    
    public ReviewSchedule(String wrongQuestionId, long firstReviewTime) {
        this();
        this.wrongQuestionId = wrongQuestionId;
        this.firstReviewTime = firstReviewTime;
    }
    
    /**
     * 添加复习时间点
     */
    public void addReviewTime(long reviewTime) {
        reviewTimes.add(reviewTime);
    }
    
    /**
     * 标记为已复习
     */
    public void markAsReviewed(long reviewTime) {
        completedReviews.add(reviewTime);
    }
    
    /**
     * 检查是否在指定时间点已复习
     */
    public boolean isReviewedAt(long reviewTime) {
        return completedReviews.contains(reviewTime);
    }
    
    /**
     * 获取下一个复习时间点
     */
    public Long getNextReviewTime() {
        long currentTime = System.currentTimeMillis();
        
        for (Long reviewTime : reviewTimes) {
            if (reviewTime > currentTime && !completedReviews.contains(reviewTime)) {
                return reviewTime;
            }
        }
        
        return null;
    }
    
    /**
     * 获取即将到期的复习时间点
     */
    public List<Long> getUpcomingReviews(long withinHours) {
        List<Long> upcoming = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long deadline = currentTime + (withinHours * 60 * 60 * 1000);
        
        for (Long reviewTime : reviewTimes) {
            if (reviewTime > currentTime && reviewTime <= deadline && 
                !completedReviews.contains(reviewTime)) {
                upcoming.add(reviewTime);
            }
        }
        
        return upcoming;
    }
    
    // Getters and Setters
    public long getFirstReviewTime() { return firstReviewTime; }
    public void setFirstReviewTime(long firstReviewTime) { this.firstReviewTime = firstReviewTime; }
    
    public List<Long> getReviewTimes() { return reviewTimes; }
    public void setReviewTimes(List<Long> reviewTimes) { this.reviewTimes = reviewTimes; }
    
    public List<Long> getCompletedReviews() { return completedReviews; }
    public void setCompletedReviews(List<Long> completedReviews) { this.completedReviews = completedReviews; }
    
    public String getWrongQuestionId() { return wrongQuestionId; }
    public void setWrongQuestionId(String wrongQuestionId) { this.wrongQuestionId = wrongQuestionId; }
}