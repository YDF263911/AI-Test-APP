# Supabase API密钥修复指南

## 🚨 问题诊断

您遇到的401错误是因为API密钥格式不正确。

**错误格式**：`sb_publishable_tt30huNDJ6ucqEIC48H8aw_dtD2-tn7`  
**正确格式**：`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`（以eyJ开头的JWT token）

## 🔧 解决步骤

### 1. 获取正确的API密钥

1. 访问 [Supabase Dashboard](https://app.supabase.com)
2. 选择您的项目 `AI-Test-Bank`
3. 在左侧菜单中点击 **Settings** (齿轮图标)
4. 选择 **API** 选项卡
5. 在 **Project API keys** 部分找到 **anon/public** 密钥
6. 点击复制按钮获取完整的密钥（应该是一个很长的、以`eyJ`开头的字符串）

### 2. 更新Android项目中的密钥

需要更新以下两个文件中的API密钥：

#### 文件1：AITestBankApplication.java
```java
// 将这一行：
private static final String SUPABASE_ANON_KEY = "在此处粘贴您的正确Supabase匿名密钥";

// 替换为：
private static final String SUPABASE_ANON_KEY = "您从Supabase控制台复制的完整密钥";
```

#### 文件2：SupabaseConnectionTest.java
```java
// 将这一行：
private static final String SUPABASE_KEY = "在此处粘贴您的正确Supabase匿名密钥";

// 替换为：
private static final String SUPABASE_KEY = "您从Supabase控制台复制的完整密钥";
```

### 3. 验证修复

更新密钥后，重新运行应用测试：

1. **基础连接**应该返回 ✅ 成功
2. **API密钥连接**应该返回 ✅ 成功  
3. **数据查询**应该继续 ✅ 成功

## 🎯 为什么会出现这个问题？

- Supabase的API密钥是JWT格式的token，不是简单的字符串
- 错误的密钥格式会导致认证失败（401错误）
- 即使查询成功，基础连接和API密钥验证仍然会失败

## ⚠️ 重要提醒

1. **不要暴露密钥**：确保不要将真实的API密钥提交到公共代码仓库
2. **密钥安全**：这个是公开密钥，可以安全地在客户端应用中使用
3. **环境变量**：在生产环境中，建议使用环境变量管理密钥

## 🚀 完成后的预期结果

所有三个测试都应该显示 ✅ 成功：

- ✅ **测试基础连接**：HTTP连接成功
- ✅ **测试API密钥连接**：API密钥验证成功  
- ✅ **测试数据查询**：数据查询成功（已经正常工作）

---

**如果问题仍然存在，请检查：**
1. 网络连接是否正常
2. Supabase项目是否正常运行
3. 数据库表是否已创建
4. Android应用是否有网络权限