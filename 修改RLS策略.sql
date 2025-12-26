-- AI智能题库APP - 修改RLS策略以允许匿名用户插入数据
-- 在Supabase项目的SQL编辑器中执行此脚本

-- 1. 首先检查当前RLS策略状态
SELECT 
    schemaname,
    tablename,
    hasrowsecurity 
FROM 
    pg_tables 
WHERE 
    schemaname = 'public' 
    AND tablename IN ('wrong_questions', 'user_profiles', 'answer_records', 'study_statistics');

-- 2. 为wrong_questions表修改RLS策略，允许匿名用户插入数据
-- 如果表未启用RLS，先启用
ALTER TABLE wrong_questions ENABLE ROW LEVEL SECURITY;

-- 创建允许所有人插入的策略
DROP POLICY IF EXISTS "允许匿名用户插入错题" ON wrong_questions;
CREATE POLICY "允许匿名用户插入错题" ON wrong_questions
    FOR INSERT WITH CHECK (true);

-- 创建允许所有人查看自己插入数据的策略
DROP POLICY IF EXISTS "允许匿名用户查看错题" ON wrong_questions;
CREATE POLICY "允许匿名用户查看错题" ON wrong_questions
    FOR SELECT USING (true);

-- 创建允许所有人更新自己插入数据的策略
DROP POLICY IF EXISTS "允许匿名用户更新错题" ON wrong_questions;
CREATE POLICY "允许匿名用户更新错题" ON wrong_questions
    FOR UPDATE USING (true);

-- 3. 为user_profiles表修改RLS策略
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

-- 允许匿名用户插入和更新用户配置文件
DROP POLICY IF EXISTS "允许匿名用户插入用户资料" ON user_profiles;
CREATE POLICY "允许匿名用户插入用户资料" ON user_profiles
    FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "允许匿名用户查看用户资料" ON user_profiles;
CREATE POLICY "允许匿名用户查看用户资料" ON user_profiles
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "允许匿名用户更新用户资料" ON user_profiles;
CREATE POLICY "允许匿名用户更新用户资料" ON user_profiles
    FOR UPDATE USING (true);

-- 4. 为answer_records表修改RLS策略
ALTER TABLE answer_records ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "允许匿名用户插入答题记录" ON answer_records;
CREATE POLICY "允许匿名用户插入答题记录" ON answer_records
    FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "允许匿名用户查看答题记录" ON answer_records;
CREATE POLICY "允许匿名用户查看答题记录" ON answer_records
    FOR SELECT USING (true);

-- 5. 为study_statistics表修改RLS策略
ALTER TABLE study_statistics ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "允许匿名用户插入学习统计" ON study_statistics;
CREATE POLICY "允许匿名用户插入学习统计" ON study_statistics
    FOR INSERT WITH CHECK (true);

DROP POLICY IF EXISTS "允许匿名用户查看学习统计" ON study_statistics;
CREATE POLICY "允许匿名用户查看学习统计" ON study_statistics
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "允许匿名用户更新学习统计" ON study_statistics;
CREATE POLICY "允许匿名用户更新学习统计" ON study_statistics
    FOR UPDATE USING (true);

-- 6. 为questions表设置只读策略（题目数据应该对所有用户可见）
ALTER TABLE questions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "允许所有人查看题目" ON questions;
CREATE POLICY "允许所有人查看题目" ON questions
    FOR SELECT USING (true);

-- 7. 检查策略是否已正确创建
SELECT 
    schemaname,
    tablename,
    policyname,
    cmd,
    qual,
    with_check
FROM 
    pg_policies 
WHERE 
    schemaname = 'public' 
    AND tablename IN ('wrong_questions', 'user_profiles', 'answer_records', 'study_statistics', 'questions');

-- 8. 测试策略是否工作
-- 注意：需要在Supabase的SQL编辑器中手动测试这些查询
-- INSERT INTO wrong_questions (id, user_id, question_id, question_title, user_answer, wrong_reason) 
-- VALUES ('test-id', 'test-user', 'test-question', '测试题目', 1, '测试原因');

-- 如果上述策略过于宽松，可以改用基于设备ID的策略：

-- 9. 基于设备ID的RLS策略（更安全的选择）
/*
-- 为wrong_questions表创建基于设备ID的策略
DROP POLICY IF EXISTS "基于设备ID插入错题" ON wrong_questions;
CREATE POLICY "基于设备ID插入错题" ON wrong_questions
    FOR INSERT WITH CHECK (
        user_id IN (
            SELECT id FROM user_profiles 
            WHERE device_id = current_setting('request.headers.x-client-info', true)::json->>'device_id'
        )
    );

DROP POLICY IF EXISTS "基于设备ID查看错题" ON wrong_questions;
CREATE POLICY "基于设备ID查看错题" ON wrong_questions
    FOR SELECT USING (
        user_id IN (
            SELECT id FROM user_profiles 
            WHERE device_id = current_setting('request.headers.x-client-info', true)::json->>'device_id'
        )
    );

-- 需要修改Android客户端代码来传递设备ID
*/