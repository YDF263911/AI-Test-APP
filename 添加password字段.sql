-- 为user_profiles表添加password字段
-- 在Supabase项目的SQL编辑器中执行此脚本

ALTER TABLE user_profiles 
ADD COLUMN password TEXT;

-- 添加注释说明
COMMENT ON COLUMN user_profiles.password IS '用户密码（实际项目中应该加密存储）';

-- 验证字段是否添加成功
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'user_profiles' 
AND table_schema = 'public'
ORDER BY ordinal_position;