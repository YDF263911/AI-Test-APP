-- 插入题目数据到Supabase数据库
-- 解决外键约束错误：wrong_questions_question_id_fkey

-- 插入Java基础题目
INSERT INTO questions (id, title, type, difficulty, category, correct_answer, options, analysis) VALUES
('1', 'Java的基本数据类型有哪些？', 'single_choice', 1, 'Java基础', 2, '["String", "Integer", "int", "ArrayList"]', 'Java的基本数据类型包括：byte、short、int、long、float、double、char、boolean。'),
('2', 'Java中如何实现多线程？', 'single_choice', 3, 'Java基础', 3, '["继承Thread类", "实现Runnable接口", "使用Executor框架", "以上都是"]', 'Java中实现多线程的方式包括：继承Thread类、实现Runnable接口、使用Executor框架等。'),
('3', 'Android中常用的布局有哪些？', 'multiple_choice', 3, 'Android开发', -1, '["LinearLayout", "RelativeLayout", "ConstraintLayout", "FrameLayout"]', 'Android中常用的布局包括：LinearLayout、RelativeLayout、ConstraintLayout、FrameLayout等。'),
('4', 'Spring框架的核心特性是什么？', 'single_choice', 2, 'Java框架', 0, '["依赖注入", "面向切面编程", "事务管理", "以上都是"]', 'Spring框架的核心特性包括依赖注入、面向切面编程、事务管理等。'),
('5', 'MySQL中如何创建索引？', 'single_choice', 2, '数据库', 1, '["CREATE INDEX", "ALTER TABLE ADD INDEX", "CREATE UNIQUE INDEX", "以上都是"]', 'MySQL中创建索引可以使用CREATE INDEX、ALTER TABLE ADD INDEX等语句。'),
('6', 'HTTP状态码200表示什么？', 'single_choice', 1, '网络', 0, '["请求成功", "未找到资源", "服务器错误", "重定向"]', 'HTTP状态码200表示请求成功，服务器已成功处理请求。'),
('7', 'React的核心概念有哪些？', 'multiple_choice', 2, '前端框架', -1, '["组件", "状态", "虚拟DOM", "生命周期"]', 'React的核心概念包括组件、状态、虚拟DOM、生命周期等。'),
('8', 'Python中列表和元组的区别？', 'single_choice', 1, 'Python', 1, '["列表可变，元组不可变", "列表不可变，元组可变", "两者都不可变", "两者都可变"]', 'Python中列表是可变的，元组是不可变的。'),
('9', '什么是面向对象编程？', 'single_choice', 1, '编程基础', 0, '["封装、继承、多态", "函数式编程", "过程式编程", "逻辑编程"]', '面向对象编程的核心概念是封装、继承、多态。'),
('10', 'Git中如何撤销本地修改？', 'single_choice', 2, '工具', 1, '["git reset", "git checkout", "git revert", "git stash"]', 'Git中可以使用git checkout来撤销本地未提交的修改。');

-- 验证插入结果
SELECT id, title, category FROM questions ORDER BY id;