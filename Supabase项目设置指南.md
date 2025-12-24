# AI-Test-Bank Supabase 数据库设置指南

## 🎯 项目信息
- **项目名称**: AI-Test-Bank
- **URL**: https://jypjsjbkspmsutmdvelq.supabase.co
- **状态**: ✅ 配置已更新到Android项目中

## 📋 下一步操作

### 1. 在Supabase控制台中执行SQL脚本

1. 访问 [Supabase Dashboard](https://app.supabase.com)
2. 选择您的项目 `AI-Test-Bank`
3. 在左侧菜单中点击 **SQL Editor**
4. 点击 **New query** 创建新查询
5. 复制并执行项目根目录下的 `Supabase数据库结构.sql` 脚本

### 2. 验证数据库表创建

执行SQL后，验证以下表是否已创建：
- ✅ `questions` - 题目表
- ✅ `user_profiles` - 用户配置表（简化版）
- ✅ `wrong_questions` - 错题表
- ✅ `answer_records` - 答题记录表
- ✅ `study_statistics` - 学习统计表

### 3. 测试Android应用连接

1. 在Android Studio中运行项目
2. 观察Logcat中的连接日志
3. 确认没有认证相关的错误

### 4. 可选：添加示例数据

您可以在SQL编辑器中运行以下命令添加测试数据：

```sql
-- 添加示例题目
INSERT INTO questions (id, content, question_type, difficulty, subject, options, correct_answer, explanation, created_at, updated_at) VALUES
('1', '以下哪个是Java的基本数据类型？', 'single_choice', 1, 'Java', '["int", "String", "Array", "Object"]', 'int', 'int是Java的8种基本数据类型之一', NOW(), NOW()),
('2', 'Java中的构造函数特点是什么？', 'multiple_choice', 2, 'Java', '["没有返回值", "方法名与类名相同", "可以重载", "必须手动调用"]', '["没有返回值", "方法名与类名相同", "可以重载"]', '构造函数用于初始化对象，具有特定的语法特征', NOW(), NOW());

-- 添加示例用户（使用设备ID）
INSERT INTO user_profiles (id, device_id, username, created_at, updated_at) VALUES
('device-001', 'android-device-001', '测试用户', NOW(), NOW());
```

## 🔧 配置说明

### 已配置的Android组件
- ✅ Supabase客户端配置
- ✅ 无认证模式（基于设备ID）
- ✅ 完整的Repository层
- ✅ 数据迁移工具

### 安全设置
- 当前使用简化配置，暂未启用RLS（Row Level Security）
- 适合开发和测试阶段
- 生产环境建议后续启用安全策略

## 🚀 测试清单

运行应用后，验证以下功能：
- [ ] 应用启动无错误
- [ ] 可以连接到Supabase
- [ ] 可以获取题目列表
- [ ] 可以创建用户配置
- [ ] 可以保存答题记录

如果遇到问题，请检查：
1. 网络连接
2. API密钥是否正确
3. 数据库表是否正确创建
4. Android网络权限配置

---

**恭喜！🎉 您的AI题库APP现在已经连接到云端数据库了！**