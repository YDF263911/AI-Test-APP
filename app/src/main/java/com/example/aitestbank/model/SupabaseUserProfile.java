package com.example.aitestbank.model;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 用户档案数据模型 - 与Supabase user_profiles表结构完全对齐
 * 基于MCP工具查询的实际表结构生成
 */
public class SupabaseUserProfile {
    
    @SerializedName("id")
    private String id; // TEXT PRIMARY KEY - UUID
    
    @SerializedName("device_id")
    private String deviceId; // TEXT - 设备ID，唯一约束
    
    @SerializedName("email")
    private String email; // TEXT - 邮箱，唯一约束
    
    @SerializedName("username")
    private String username; // TEXT - 用户名，唯一约束
    
    @SerializedName("display_name")
    private String displayName; // TEXT - 显示名称
    
    @SerializedName("avatar_url")
    private String avatarUrl; // TEXT - 头像URL
    
    @SerializedName("phone")
    private String phone; // TEXT - 手机号
    
    @SerializedName("study_target")
    private String studyTarget; // TEXT - 学习目标
    
    @SerializedName("study_subject")
    private String studySubject; // TEXT - 学习科目
    
    @SerializedName("study_plan")
    private String studyPlan; // TEXT - 学习计划
    
    @SerializedName("daily_goal")
    private Integer dailyGoal = 20; // INTEGER DEFAULT 20 - 每日目标
    
    @SerializedName("total_questions")
    private Long totalQuestions = 0L; // BIGINT DEFAULT 0 - 总答题数
    
    @SerializedName("correct_questions")
    private Long correctQuestions = 0L; // BIGINT DEFAULT 0 - 正确答题数
    
    @SerializedName("study_days")
    private Integer studyDays = 0; // INTEGER DEFAULT 0 - 学习天数
    
    @SerializedName("last_study_date")
    private String lastStudyDate; // DATE - 最后学习日期
    
    @SerializedName("study_streak")
    private Integer studyStreak = 0; // INTEGER DEFAULT 0 - 连续学习天数
    
    @SerializedName("preferences")
    private Map<String, Object> preferences; // JSONB - 用户偏好设置
    
    @SerializedName("is_premium")
    private Boolean isPremium = false; // BOOLEAN DEFAULT false - 是否高级用户
    
    @SerializedName("premium_expired_at")
    private String premiumExpiredAt; // TIMESTAMPTZ - 高级会员到期时间
    
    @SerializedName("created_at")
    private String createdAt; // TIMESTAMPTZ DEFAULT NOW()
    
    @SerializedName("updated_at")
    private String updatedAt; // TIMESTAMPTZ DEFAULT NOW()
    
    // 计算字段（非数据库字段）
    private transient Float accuracyRate;
    
    // 构造函数
    public SupabaseUserProfile() {
    }
    
    public SupabaseUserProfile(String deviceId) {
        this.id = java.util.UUID.randomUUID().toString();
        this.deviceId = deviceId;
        this.username = "用户_" + deviceId.substring(0, Math.min(8, deviceId.length()));
        this.displayName = "AI刷题用户";
        this.dailyGoal = 20;
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
    
    public Integer getDailyGoal() {
        return dailyGoal;
    }
    
    public void setDailyGoal(Integer dailyGoal) {
        this.dailyGoal = dailyGoal;
    }
    
    public Long getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(Long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public Long getCorrectQuestions() {
        return correctQuestions;
    }
    
    public void setCorrectQuestions(Long correctQuestions) {
        this.correctQuestions = correctQuestions;
    }
    
    public Integer getStudyDays() {
        return studyDays;
    }
    
    public void setStudyDays(Integer studyDays) {
        this.studyDays = studyDays;
    }
    
    public String getLastStudyDate() {
        return lastStudyDate;
    }
    
    public void setLastStudyDate(String lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }
    
    public Integer getStudyStreak() {
        return studyStreak;
    }
    
    public void setStudyStreak(Integer studyStreak) {
        this.studyStreak = studyStreak;
    }
    
    public Map<String, Object> getPreferences() {
        return preferences;
    }
    
    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }
    
    public Boolean getPremium() {
        return isPremium;
    }
    
    public void setPremium(Boolean premium) {
        isPremium = premium;
    }
    
    public String getPremiumExpiredAt() {
        return premiumExpiredAt;
    }
    
    public void setPremiumExpiredAt(String premiumExpiredAt) {
        this.premiumExpiredAt = premiumExpiredAt;
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
    
    // 计算属性方法
    public Float getAccuracyRate() {
        if (accuracyRate != null) {
            return accuracyRate;
        }
        if (totalQuestions > 0) {
            return (float) correctQuestions / totalQuestions * 100;
        }
        return 0f;
    }
    
    public void setAccuracyRate(Float accuracyRate) {
        this.accuracyRate = accuracyRate;
    }
    
    /**
     * 计算并更新正确率
     */
    public void calculateAccuracyRate() {
        this.accuracyRate = getAccuracyRate();
    }
    
    /**
     * 转换为Date类型的最后学习日期
     */
    public Date getLastStudyDateAsDate() {
        try {
            if (lastStudyDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(lastStudyDate);
            }
        } catch (Exception e) {
            android.util.Log.e("SupabaseUserProfile", "解析最后学习日期失败", e);
        }
        return null;
    }
    
    /**
     * 设置最后学习日期（Date类型）
     */
    public void setLastStudyDateFromDate(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            this.lastStudyDate = sdf.format(date);
        }
    }
    
    /**
     * 检查是否为高级用户且未过期
     */
    public boolean isPremiumValid() {
        if (!isPremium || premiumExpiredAt == null) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date expiredDate = sdf.parse(premiumExpiredAt);
            return expiredDate.after(new Date());
        } catch (Exception e) {
            android.util.Log.e("SupabaseUserProfile", "解析会员到期时间失败", e);
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "SupabaseUserProfile{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", username='" + username + '\'' +
                ", totalQuestions=" + totalQuestions +
                ", correctQuestions=" + correctQuestions +
                ", accuracyRate=" + getAccuracyRate() +
                ", studyStreak=" + studyStreak +
                ", isPremium=" + isPremium +
                '}';
    }
}