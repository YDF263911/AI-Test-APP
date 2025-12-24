-- AI智能题库APP - Supabase数据库结构
-- 在Supabase项目的SQL编辑器中执行此脚本

-- 1. 题目表
CREATE TABLE questions (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    options TEXT[], -- 选项数组
    correct_answer INTEGER,
    analysis TEXT,
    ai_analysis TEXT,
    knowledge_points TEXT[], -- 知识点数组
    difficulty INTEGER CHECK (difficulty >= 1 AND difficulty <= 5),
    category TEXT, -- 考试类型：校招/考公/考研
    subject TEXT, -- 学科分类
    source TEXT, -- 题目来源
    type TEXT CHECK (type IN ('single_choice', 'multiple_choice', 'true_false', 'fill_blank')),
    tags TEXT[], -- 标签数组
    view_count BIGINT DEFAULT 0,
    correct_rate DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. 用户扩展表 (简化版，不关联Auth)
CREATE TABLE user_profiles (
    id TEXT PRIMARY KEY, -- 使用UUID作为主键
    device_id TEXT UNIQUE, -- 设备ID作为唯一标识
    email TEXT UNIQUE,
    username TEXT UNIQUE,
    display_name TEXT,
    avatar_url TEXT,
    phone TEXT,
    study_target TEXT, -- 学习目标：校招/考公/考研
    study_subject TEXT, -- 学习学科
    study_plan TEXT,
    daily_goal INTEGER DEFAULT 20,
    total_questions BIGINT DEFAULT 0,
    correct_questions BIGINT DEFAULT 0,
    study_days INTEGER DEFAULT 0,
    last_study_date DATE,
    study_streak INTEGER DEFAULT 0,
    preferences JSONB, -- 用户偏好设置
    is_premium BOOLEAN DEFAULT FALSE,
    premium_expired_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. 错题表
CREATE TABLE wrong_questions (
    id TEXT PRIMARY KEY,
    user_id TEXT REFERENCES user_profiles(id) ON DELETE CASCADE,
    question_id TEXT REFERENCES questions(id) ON DELETE CASCADE,
    question_title TEXT,
    options TEXT[],
    correct_answer INTEGER,
    user_answer INTEGER,
    analysis TEXT,
    ai_analysis TEXT,
    knowledge_points TEXT[],
    difficulty INTEGER,
    category TEXT,
    subject TEXT,
    source TEXT,
    type TEXT,
    wrong_reason TEXT,
    review_count INTEGER DEFAULT 1,
    mastery_level INTEGER CHECK (mastery_level >= 1 AND mastery_level <= 5),
    is_mastered BOOLEAN DEFAULT FALSE,
    next_review_date DATE,
    last_review_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. 答题记录表
CREATE TABLE answer_records (
    id TEXT PRIMARY KEY,
    user_id TEXT REFERENCES user_profiles(id) ON DELETE CASCADE,
    question_id TEXT REFERENCES questions(id) ON DELETE CASCADE,
    user_answer INTEGER,
    is_correct BOOLEAN,
    answer_time INTEGER, -- 答题用时（秒）
    session_id TEXT, -- 会话ID，用于统计单次答题情况
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. 学习统计表
CREATE TABLE study_statistics (
    id TEXT PRIMARY KEY,
    user_id TEXT REFERENCES user_profiles(id) ON DELETE CASCADE,
    date DATE UNIQUE,
    questions_answered INTEGER DEFAULT 0,
    questions_correct INTEGER DEFAULT 0,
    study_time INTEGER DEFAULT 0, -- 学习时长（分钟）
    categories_practiced TEXT[], -- 练习的分类
    subjects_practiced TEXT[], -- 练习的学科
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. 题库分类表
CREATE TABLE question_categories (
    id TEXT PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    icon TEXT, -- 图标名称或URL
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. 学科表
CREATE TABLE subjects (
    id TEXT PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    category_id TEXT REFERENCES question_categories(id) ON DELETE CASCADE,
    description TEXT,
    icon TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 创建索引
CREATE INDEX idx_questions_category ON questions(category);
CREATE INDEX idx_questions_subject ON questions(subject);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_questions_type ON questions(type);
CREATE INDEX idx_questions_created_at ON questions(created_at);

CREATE INDEX idx_wrong_questions_user_id ON wrong_questions(user_id);
CREATE INDEX idx_wrong_questions_question_id ON wrong_questions(question_id);
CREATE INDEX idx_wrong_questions_next_review ON wrong_questions(next_review_date);
CREATE INDEX idx_wrong_questions_is_mastered ON wrong_questions(is_mastered);

CREATE INDEX idx_answer_records_user_id ON answer_records(user_id);
CREATE INDEX idx_answer_records_question_id ON answer_records(question_id);
CREATE INDEX idx_answer_records_created_at ON answer_records(created_at);

CREATE INDEX idx_study_statistics_user_id ON study_statistics(user_id);
CREATE INDEX idx_study_statistics_date ON study_statistics(date);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为相关表创建更新时间触发器
CREATE TRIGGER update_questions_updated_at BEFORE UPDATE ON questions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_wrong_questions_updated_at BEFORE UPDATE ON wrong_questions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_study_statistics_updated_at BEFORE UPDATE ON study_statistics FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_question_categories_updated_at BEFORE UPDATE ON question_categories FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_subjects_updated_at BEFORE UPDATE ON subjects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 简化版配置 - 暂时不使用RLS，所有表完全开放访问
-- 后续需要时再启用Row Level Security

-- 注意：生产环境建议启用RLS策略保护用户数据

-- 插入初始数据
INSERT INTO question_categories (id, name, description, icon, sort_order) VALUES
('campus_recruitment', '校园招聘', '互联网公司校园招聘题目', 'school', 1),
('civil_service', '公务员考试', '国家公务员和省考题目', 'government', 2),
('postgraduate', '考研', '硕士研究生入学考试题目', 'graduation', 3);

INSERT INTO subjects (id, name, category_id, description, sort_order) VALUES
('programming', '编程语言', 'campus_recruitment', 'Java、Python、C++等编程语言题目', 1),
('algorithm', '算法与数据结构', 'campus_recruitment', '排序、搜索、动态规划等算法题目', 2),
('database', '数据库', 'campus_recruitment', 'SQL、NoSQL等数据库题目', 3),
('network', '计算机网络', 'campus_recruitment', 'TCP/IP、HTTP等网络协议题目', 4),
('operating_system', '操作系统', 'campus_recruitment', '进程、内存、文件系统等操作系统题目', 5),
('verbal_reasoning', '言语理解', 'civil_service', '行测言语理解题目', 1),
('quantitative_reasoning', '数量关系', 'civil_service', '行测数量关系题目', 2),
('logical_reasoning', '判断推理', 'civil_service', '行测判断推理题目', 3),
('mathematics', '数学', 'postgraduate', '高等数学、线性代数等数学题目', 1),
('english', '英语', 'postgraduate', '考研英语题目', 2),
('politics', '政治', 'postgraduate', '考研政治题目', 3);

-- 创建用于实时订阅的函数
CREATE OR REPLACE FUNCTION notify_question_update()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM pg_notify('question_changes', json_build_object(
        'table', TG_TABLE_NAME,
        'operation', TG_OP,
        'id', NEW.id
    )::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER question_notification
    AFTER INSERT OR UPDATE OR DELETE ON questions
    FOR EACH ROW EXECUTE FUNCTION notify_question_update();