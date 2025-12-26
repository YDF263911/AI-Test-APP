package com.example.aitestbank.model;

import java.util.Date;
import java.util.List;

/**
 * 学习计划数据模型
 */
public class StudyPlan {
    private String id;
    private String userId;
    private int dailyGoal;          // 每日目标题数
    private int todayCompleted;     // 今日已完成题数
    private int consecutiveDays;    // 连续学习天数
    private int totalStudyDays;     // 总学习天数
    private Date lastStudyDate;     // 最后学习日期
    private List<DailyStat> weeklyStats; // 本周统计
    
    // 构造函数
    public StudyPlan() {}
    
    public StudyPlan(String userId, int dailyGoal) {
        this.id = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.dailyGoal = dailyGoal;
        this.todayCompleted = 0;
        this.consecutiveDays = 0;
        this.totalStudyDays = 0;
        this.lastStudyDate = new Date();
    }
    
    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public int getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(int dailyGoal) { this.dailyGoal = dailyGoal; }
    
    public int getTodayCompleted() { return todayCompleted; }
    public void setTodayCompleted(int todayCompleted) { this.todayCompleted = todayCompleted; }
    
    public int getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(int consecutiveDays) { this.consecutiveDays = consecutiveDays; }
    
    public int getTotalStudyDays() { return totalStudyDays; }
    public void setTotalStudyDays(int totalStudyDays) { this.totalStudyDays = totalStudyDays; }
    
    public Date getLastStudyDate() { return lastStudyDate; }
    public void setLastStudyDate(Date lastStudyDate) { this.lastStudyDate = lastStudyDate; }
    
    public List<DailyStat> getWeeklyStats() { return weeklyStats; }
    public void setWeeklyStats(List<DailyStat> weeklyStats) { this.weeklyStats = weeklyStats; }
    
    /**
     * 获取今日完成进度百分比
     */
    public int getTodayProgress() {
        if (dailyGoal <= 0) return 0;
        return Math.min((todayCompleted * 100) / dailyGoal, 100);
    }
    
    /**
     * 判断是否完成今日目标
     */
    public boolean isDailyGoalCompleted() {
        return todayCompleted >= dailyGoal;
    }
    
    /**
     * 获取激励文案
     */
    public String getMotivationText() {
        if (isDailyGoalCompleted()) {
            return "🎉 今日目标已完成！继续保持！";
        } else if (todayCompleted > 0) {
            return String.format("💪 加油！还需完成 %d 题达成目标", dailyGoal - todayCompleted);
        } else {
            return "🌟 开始今天的刷题之旅吧！";
        }
    }
    
    /**
     * 每日统计子模型
     */
    public static class DailyStat {
        private String date;
        private int completedQuestions;
        private int correctQuestions;
        private boolean studied;
        
        public DailyStat() {}
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public int getCompletedQuestions() { return completedQuestions; }
        public void setCompletedQuestions(int completedQuestions) { this.completedQuestions = completedQuestions; }
        
        public int getCorrectQuestions() { return correctQuestions; }
        public void setCorrectQuestions(int correctQuestions) { this.correctQuestions = correctQuestions; }
        
        public boolean isStudied() { return studied; }
        public void setStudied(boolean studied) { this.studied = studied; }
        
        /**
         * 获取正确率
         */
        public int getAccuracy() {
            if (completedQuestions <= 0) return 0;
            return (correctQuestions * 100) / completedQuestions;
        }
    }
}