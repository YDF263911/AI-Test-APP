package com.example.aitestbank.model;

import java.util.Date;

/**
 * 用户数据模型
 */
public class User {
    
    private String id;
    
    private String deviceId;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String phone;
    
    // 学习目标相关
    private String studyTarget;
    private String studySubject;
    private String studyPlan;
    private int dailyGoal;
    
    // 学习统计
    private long totalQuestions;
    private long correctQuestions;
    private int studyDays;
    private String lastStudyDate;
    private int studyStreak;
    
    // 偏好设置
    private String preferences;
    private boolean isPremium;
    private Date premiumExpiredAt;
    
    // 时间戳
    private Date createdAt;
    private Date updatedAt;
    
    // 构造函数
    public User() {
        this.dailyGoal = 20; // 默认每日目标
        this.totalQuestions = 0;
        this.correctQuestions = 0;
        this.studyDays = 0;
        this.studyStreak = 0;
        this.isPremium = false;
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getStudyTarget() {
        return studyTarget;
    }
    
    public void setStudyTarget(String studyTarget) {
        this.studyTarget = studyTarget;
    }
    
    public String getStudySubject() {
        return studySubject;
    }
    
    public void setStudySubject(String studySubject) {
        this.studySubject = studySubject;
    }
    
    public String getStudyPlan() {
        return studyPlan;
    }
    
    public void setStudyPlan(String studyPlan) {
        this.studyPlan = studyPlan;
    }
    
    public int getDailyGoal() {
        return dailyGoal;
    }
    
    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
    }
    
    public long getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public long getCorrectQuestions() {
        return correctQuestions;
    }
    
    public void setCorrectQuestions(long correctQuestions) {
        this.correctQuestions = correctQuestions;
    }
    
    public int getStudyDays() {
        return studyDays;
    }
    
    public void setStudyDays(int studyDays) {
        this.studyDays = studyDays;
    }
    
    public String getLastStudyDate() {
        return lastStudyDate;
    }
    
    public void setLastStudyDate(String lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }
    
    public int getStudyStreak() {
        return studyStreak;
    }
    
    public void setStudyStreak(int studyStreak) {
        this.studyStreak = studyStreak;
    }
    
    public String getPreferences() {
        return preferences;
    }
    
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public boolean getIsPremium() {
        return isPremium;
    }
    
    public void setIsPremium(boolean isPremium) {
        this.isPremium = isPremium;
    }
    
    public Date getPremiumExpiredAt() {
        return premiumExpiredAt;
    }
    
    public void setPremiumExpiredAt(Date premiumExpiredAt) {
        this.premiumExpiredAt = premiumExpiredAt;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}