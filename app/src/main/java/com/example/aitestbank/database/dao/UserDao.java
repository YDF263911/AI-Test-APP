package com.example.aitestbank.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aitestbank.model.User;

import java.util.List;

/**
 * 用户数据访问对象
 */
@Dao
public interface UserDao {
    
    /**
     * 插入用户信息（冲突时替换）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);
    
    /**
     * 更新用户信息
     */
    @Update
    int updateUser(User user);
    
    /**
     * 删除用户
     */
    @Delete
    int deleteUser(User user);
    
    /**
     * 根据ID获取用户
     */
    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(String userId);
    
    /**
     * 获取用户（同步方法）
     */
    @Query("SELECT * FROM users WHERE user_id = :userId")
    User getUserByIdSync(String userId);
    
    /**
     * 获取所有用户
     */
    @Query("SELECT * FROM users ORDER BY last_login DESC")
    LiveData<List<User>> getAllUsers();
    
    /**
     * 更新用户最后登录时间
     */
    @Query("UPDATE users SET last_login = :loginTime WHERE user_id = :userId")
    int updateLastLogin(String userId, long loginTime);
    
    /**
     * 更新用户学习统计
     */
    @Query("UPDATE users SET total_questions = :totalQuestions, correct_answers = :correctAnswers, study_time = :studyTime, last_active = :lastActive WHERE user_id = :userId")
    int updateStudyStats(String userId, int totalQuestions, int correctAnswers, long studyTime, long lastActive);
    
    /**
     * 更新用户偏好设置
     */
    @Query("UPDATE users SET question_difficulty = :difficulty, study_mode = :studyMode, daily_target = :dailyTarget, reminder_enabled = :reminderEnabled WHERE user_id = :userId")
    int updatePreferences(String userId, String difficulty, String studyMode, int dailyTarget, boolean reminderEnabled);
    
    /**
     * 增加用户学习天数
     */
    @Query("UPDATE users SET consecutive_days = consecutive_days + 1, longest_streak = CASE WHEN consecutive_days + 1 > longest_streak THEN consecutive_days + 1 ELSE longest_streak END WHERE user_id = :userId")
    int incrementConsecutiveDays(String userId);
    
    /**
     * 重置连续学习天数（用于中断后重新开始）
     */
    @Query("UPDATE users SET consecutive_days = 1 WHERE user_id = :userId")
    int resetConsecutiveDays(String userId);
    
    /**
     * 获取用户统计信息
     */
    @Query("""
        SELECT 
            user_id as userId,
            username,
            total_questions as totalQuestions,
            correct_answers as correctAnswers,
            study_time as studyTime,
            consecutive_days as consecutiveDays,
            longest_streak as longestStreak,
            last_active as lastActive,
            correct_answers * 100.0 / CASE WHEN total_questions > 0 THEN total_questions ELSE 1 END as accuracy
        FROM users 
        WHERE user_id = :userId
        """)
    LiveData<UserStats> getUserStats(String userId);
    
    /**
     * 获取学习统计信息（同步方法）
     */
    @Query("""
        SELECT 
            user_id as userId,
            username,
            total_questions as totalQuestions,
            correct_answers as correctAnswers,
            study_time as studyTime,
            consecutive_days as consecutiveDays,
            longest_streak as longestStreak,
            last_active as lastActive,
            correct_answers * 100.0 / CASE WHEN total_questions > 0 THEN total_questions ELSE 1 END as accuracy
        FROM users 
        WHERE user_id = :userId
        """)
    UserStats getUserStatsSync(String userId);
    
    /**
     * 获取最活跃的用户
     */
    @Query("SELECT * FROM users ORDER BY study_time DESC LIMIT :limit")
    List<User> getMostActiveUsers(int limit);
    
    /**
     * 获取准确率最高的用户
     */
    @Query("SELECT * FROM users WHERE total_questions >= :minQuestions ORDER BY (correct_answers * 100.0 / total_questions) DESC LIMIT :limit")
    List<User> getMostAccurateUsers(int minQuestions, int limit);
    
    /**
     * 检查用户是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE user_id = :userId")
    boolean userExists(String userId);
    
    /**
     * 获取今日活跃用户数量
     */
    @Query("SELECT COUNT(*) FROM users WHERE last_active >= :todayStart")
    int getTodayActiveUsers(long todayStart);
    
    /**
     * 用户统计信息类
     */
    class UserStats {
        public String userId;
        public String username;
        public int totalQuestions;
        public int correctAnswers;
        public long studyTime;
        public int consecutiveDays;
        public int longestStreak;
        public long lastActive;
        public double accuracy;
    }
}