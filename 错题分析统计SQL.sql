-- 错题分析统计SQL查询
-- 用于在错题本页面显示薄弱点分析

-- 1. 各科目错题数量统计
SELECT 
    category,
    COUNT(*) as wrong_count
FROM wrong_questions 
WHERE user_id = '{user_id}' AND is_mastered = false
GROUP BY category
ORDER BY wrong_count DESC;

-- 2. 高频错误知识点统计
SELECT 
    unnest(knowledge_points) as knowledge_point,
    COUNT(*) as error_count
FROM wrong_questions 
WHERE user_id = '{user_id}' AND is_mastered = false
GROUP BY knowledge_point
ORDER BY error_count DESC
LIMIT 10;

-- 3. 错误难度分布
SELECT 
    difficulty,
    COUNT(*) as count
FROM wrong_questions 
WHERE user_id = '{user_id}' AND is_mastered = false
GROUP BY difficulty
ORDER BY difficulty;

-- 4. 各科目掌握率统计
WITH total_stats AS (
    SELECT 
        category,
        COUNT(*) as total_count
    FROM wrong_questions 
    WHERE user_id = '{user_id}'
    GROUP BY category
),
mastered_stats AS (
    SELECT 
        category,
        COUNT(*) as mastered_count
    FROM wrong_questions 
    WHERE user_id = '{user_id}' AND is_mastered = true
    GROUP BY category
)
SELECT 
    t.category,
    t.total_count,
    COALESCE(m.mastered_count, 0) as mastered_count,
    CASE 
        WHEN t.total_count > 0 THEN ROUND((COALESCE(m.mastered_count, 0) * 100.0 / t.total_count), 1)
        ELSE 0 
    END as mastery_rate
FROM total_stats t
LEFT JOIN mastered_stats m ON t.category = m.category
ORDER BY mastery_rate ASC; -- 掌握率低的排前面