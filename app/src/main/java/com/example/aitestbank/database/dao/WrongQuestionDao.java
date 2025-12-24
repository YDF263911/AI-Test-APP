package com.example.aitestbank.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.aitestbank.model.WrongQuestion;

import java.util.Date;
import java.util.List;

/**
 * 错题数据访问对象
 */
@Dao
public interface WrongQuestionDao {
    
    /**
     * 插入错题（冲突时更新）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWrongQuestion(WrongQuestion wrongQuestion);
    
    /**
     * 更新错题信息
     */
    @Update
    int updateWrongQuestion(WrongQuestion wrongQuestion);
    
    /**
     * 删除错题
     */
    @Delete
    int deleteWrongQuestion(WrongQuestion wrongQuestion);
    
    /**
     * 根据ID获取错题
     */
    @Query("SELECT * FROM wrong_questions WHERE id = :id")
    LiveData<WrongQuestion> getWrongQuestionById(int id);
    
    /**
     * 根据题目ID获取错题
     */
    @Query("SELECT * FROM wrong_questions WHERE question_id = :questionId AND user_id = :userId")
    LiveData<WrongQuestion> getWrongQuestionByQuestionId(String questionId, String userId);
    
    /**
     * 获取用户的所有错题
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId ORDER BY wrong_count DESC, created_at DESC")
    LiveData<List<WrongQuestion>> getAllWrongQuestions(String userId);
    
    /**
     * 获取用户的所有错题（同步方法）
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId ORDER BY wrong_count DESC, created_at DESC")
    List<WrongQuestion> getAllWrongQuestionsSync(String userId);
    
    /**
     * 根据学科获取错题
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId AND subject = :subject ORDER BY wrong_count DESC")
    LiveData<List<WrongQuestion>> getWrongQuestionsBySubject(String userId, String subject);
    
    /**
     * 根据掌握状态获取错题
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId AND mastery_status = :status ORDER BY last_wrong_time DESC")
    LiveData<List<WrongQuestion>> getWrongQuestionsByMasteryStatus(String userId, String status);
    
    /**
     * 获取需要复习的错题（掌握状态为未掌握或部分掌握）
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId AND mastery_status IN ('not_mastered', 'partially_mastered') ORDER BY priority DESC, last_wrong_time DESC")
    LiveData<List<WrongQuestion>> getReviewRequiredWrongQuestions(String userId);
    
    /**
     * 获取高错误次数的错题
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId AND wrong_count >= :minCount ORDER BY wrong_count DESC")
    LiveData<List<WrongQuestion>> getHighErrorCountQuestions(String userId, int minCount);
    
    /**
     * 获取最近添加的错题
     */
    @Query("SELECT * FROM wrong_questions WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<WrongQuestion>> getRecentWrongQuestions(String userId, int limit);
    
    /**
     * 更新错题的错误次数
     */
    @Query("UPDATE wrong_questions SET wrong_count = wrong_count + 1, last_wrong_time = :lastWrongTime WHERE id = :id")
    int incrementWrongCount(int id, long lastWrongTime);
    
    /**
     * 更新错题的掌握状态
     */
    @Query("UPDATE wrong_questions SET mastery_status = :status, review_count = review_count + 1, last_review_time = :reviewTime WHERE id = :id")
    int updateMasteryStatus(int id, String status, long reviewTime);
    
    /**
     * 更新错题优先级
     */
    @Query("UPDATE wrong_questions SET priority = :priority WHERE id = :id")
    int updatePriority(int id, int priority);
    
    /**
     * 标记错题为已掌握
     */
    @Query("UPDATE wrong_questions SET mastery_status = 'mastered', last_review_time = :reviewTime WHERE id = :id")
    int markAsMastered(int id, long reviewTime);
    
    /**
     * 重置错题的掌握状态（从已掌握改为部分掌握）
     */
    @Query("UPDATE wrong_questions SET mastery_status = 'partially_mastered', last_review_time = :reviewTime WHERE id = :id")
    int resetMasteryStatus(int id, long reviewTime);
    
    /**
     * 删除指定用户的所有错题
     */
    @Query("DELETE FROM wrong_questions WHERE user_id = :userId")
    int deleteAllWrongQuestions(String userId);
    
    /**
     * 删除已掌握的错题
     */
    @Query("DELETE FROM wrong_questions WHERE user_id = :userId AND mastery_status = 'mastered'")
    int deleteMasteredWrongQuestions(String userId);
    
    /**
     * 获取错题统计信息
     */
    @Query("""
        SELECT 
            COUNT(*) as totalWrongQuestions,
            COUNT(CASE WHEN mastery_status = 'mastered' THEN 1 END) as masteredQuestions,
            COUNT(CASE WHEN mastery_status = 'partially_mastered' THEN 1 END) as partiallyMasteredQuestions,
            COUNT(CASE WHEN mastery_status = 'not_mastered' THEN 1 END) as notMasteredQuestions,
            SUM(wrong_count) as totalWrongCount,
            AVG(wrong_count) as avgWrongCount
        FROM wrong_questions 
        WHERE user_id = :userId
        """)
    LiveData<WrongQuestionStats> getWrongQuestionStats(String userId);
    
    /**
     * 获取错题统计信息（同步方法）
     */
    @Query("""
        SELECT 
            COUNT(*) as totalWrongQuestions,
            COUNT(CASE WHEN mastery_status = 'mastered' THEN 1 END) as masteredQuestions,
            COUNT(CASE WHEN mastery_status = 'partially_mastered' THEN 1 END) as partiallyMasteredQuestions,
            COUNT(CASE WHEN mastery_status = 'not_mastered' THEN 1 END) as notMasteredQuestions,
            SUM(wrong_count) as totalWrongCount,
            AVG(wrong_count) as avgWrongCount
        FROM wrong_questions 
        WHERE user_id = :userId
        """)
    WrongQuestionStats getWrongQuestionStatsSync(String userId);
    
    /**
     * 根据学科统计错题数量
     */
    @Query("""
        SELECT 
            subject,
            COUNT(*) as questionCount,
            COUNT(CASE WHEN mastery_status = 'mastered' THEN 1 END) as masteredCount
        FROM wrong_questions 
        WHERE user_id = :userId 
        GROUP BY subject
        ORDER BY questionCount DESC
        """)
    LiveData<List<WrongQuestionCountStats>> getWrongQuestionStatsBySubject(String userId);
    
    /**
     * 获取错题最多的学科
     */
    @Query("""
        SELECT subject 
        FROM wrong_questions 
        WHERE user_id = :userId 
        GROUP BY subject 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
        """)
    String getMostProblematicSubject(String userId);
    
    /**
     * 清理超过指定天数的已掌握错题
     */
    @Query("DELETE FROM wrong_questions WHERE user_id = :userId AND mastery_status = 'mastered' AND last_review_time < :cutoffTime")
    int cleanupOldMasteredQuestions(String userId, long cutoffTime);
    
    /**
     * 错题统计信息类
     */
    class WrongQuestionStats {
        public int totalWrongQuestions;
        public int masteredQuestions;
        public int partiallyMasteredQuestions;
        public int notMasteredQuestions;
        public int totalWrongCount;
        public double avgWrongCount;
    }
    
    /**
     * 错题数量统计类
     */
    class WrongQuestionCountStats {
        public String subject;
        public int questionCount;
        public int masteredCount;
    }
}