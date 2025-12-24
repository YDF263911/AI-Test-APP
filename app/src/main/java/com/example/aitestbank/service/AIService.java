package com.example.aitestbank.service;

import android.content.Context;
import android.util.Log;

import com.example.aitestbank.model.Question;
import com.example.aitestbank.utils.NetworkUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AI解析服务类 - 负责与AI服务对接，生成智能解析内容
 */
public class AIService {
    
    private static final String TAG = "AIService";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final DeepSeekService deepSeekService;
    private final Context context;
    
    public AIService(Context context) {
        this.context = context;
        this.deepSeekService = NetworkUtils.getDeepSeekService();
    }
    
    /**
     * 获取AI智能解析
     * @param question 题目对象
     * @param userAnswer 用户答案
     * @param callback 回调接口
     */
    public void getAIAnalysis(Question question, String userAnswer, AICallback callback) {
        // 构造DeepSeek API请求数据
        Map<String, Object> requestData = new HashMap<>();
        
        // 构建系统提示
        String systemPrompt = buildSystemPrompt(question, userAnswer);
        
        // 构建用户消息
        String userMessage = buildUserMessage(question, userAnswer);
        
        // 设置消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        requestData.put("model", "deepseek-chat");
        requestData.put("messages", messages);
        requestData.put("temperature", 0.7);
        requestData.put("max_tokens", 2000);
        
        Call<DeepSeekService.DeepSeekResponse> call = deepSeekService.chatCompletion(requestData);
        
        call.enqueue(new Callback<DeepSeekService.DeepSeekResponse>() {
            @Override
            public void onResponse(Call<DeepSeekService.DeepSeekResponse> call, Response<DeepSeekService.DeepSeekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeepSeekService.DeepSeekResponse deepSeekResponse = response.body();
                    handleDeepSeekResponse(deepSeekResponse, question, userAnswer, callback);
                } else {
                    Log.e(TAG, "DeepSeek API调用失败，状态码：" + response.code());
                    // 如果API调用失败，使用本地AI解析
                    generateLocalAIAnalysis(question, userAnswer, callback);
                }
            }
            
            @Override
            public void onFailure(Call<DeepSeekService.DeepSeekResponse> call, Throwable t) {
                Log.e(TAG, "DeepSeek API调用失败", t);
                // 使用本地AI解析作为fallback
                generateLocalAIAnalysis(question, userAnswer, callback);
            }
        });
    }
    
    /**
     * 构建系统提示
     */
    private String buildSystemPrompt(Question question, String userAnswer) {
        return "你是一个专业的题库解析助手，专门为编程和技术类题目提供详细解析。请按照以下格式提供解析：\n" +
                "1. 题目解析：详细分析题目内容和解题思路\n" +
                "2. 解题步骤：分步骤说明解题过程\n" +
                "3. 核心考点：列出题目涉及的关键知识点\n" +
                "4. 易错点提醒：指出常见错误和注意事项\n" +
                "5. 学习建议：提供针对性的学习建议\n" +
                "请确保解析专业、准确、易懂。";
    }
    
    /**
     * 构建用户消息
     */
    private String buildUserMessage(Question question, String userAnswer) {
        StringBuilder message = new StringBuilder();
        
        message.append("题目信息：\n");
        message.append("题目ID：").append(question.getId()).append("\n");
        message.append("题目类型：").append(getQuestionTypeText(question.getType())).append("\n");
        message.append("题目难度：").append(getDifficultyText(question.getDifficulty())).append("\n");
        message.append("题目分类：").append(question.getCategory()).append("\n\n");
        
        message.append("题目内容：\n").append(question.getTitle()).append("\n\n");
        
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            message.append("选项：\n");
            List<String> options = question.getOptions();
            for (int i = 0; i < options.size(); i++) {
                message.append((char)('A' + i)).append(". ").append(options.get(i)).append("\n");
            }
            message.append("\n");
        }
        
        message.append("正确答案：").append(question.getCorrectAnswer()).append("\n");
        message.append("用户答案：").append(userAnswer != null ? userAnswer : "未作答").append("\n\n");
        
        message.append("请为这道题目提供详细的AI解析。");
        
        return message.toString();
    }
    
    /**
     * 处理DeepSeek API响应
     */
    private void handleDeepSeekResponse(DeepSeekService.DeepSeekResponse response, Question question, String userAnswer, AICallback callback) {
        try {
            AIAnalysisResult result = new AIAnalysisResult();
            
            // 获取AI生成的内容
            String aiContent = response.getContent();
            
            // 解析AI返回的内容
            result.setAnalysisText(parseAIContent(aiContent, "题目解析"));
            result.setSolutionSteps(parseAIContentList(aiContent, "解题步骤"));
            result.setKeyPoints(parseAIContentList(aiContent, "核心考点"));
            result.setCommonMistakes(parseAIContentList(aiContent, "易错点提醒"));
            result.setRecommendations(parseAIContentList(aiContent, "学习建议"));
            
            // 如果AI解析内容为空，使用本地解析作为fallback
            if (result.getAnalysisText() == null || result.getAnalysisText().isEmpty()) {
                Log.w(TAG, "AI解析内容为空，使用本地解析");
                generateLocalAIAnalysis(question, userAnswer, callback);
                return;
            }
            
            callback.onSuccess(result);
            
        } catch (Exception e) {
            Log.e(TAG, "处理DeepSeek响应失败", e);
            generateLocalAIAnalysis(question, userAnswer, callback);
        }
    }
    
    /**
     * 从AI内容中解析特定部分
     */
    private String parseAIContent(String content, String sectionName) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        try {
            String[] lines = content.split("\\n");
            StringBuilder sectionContent = new StringBuilder();
            boolean inSection = false;
            
            for (String line : lines) {
                if (line.trim().startsWith(sectionName + "：") || line.trim().startsWith(sectionName)) {
                    inSection = true;
                    // 跳过标题行
                    continue;
                }
                
                if (inSection) {
                    // 如果遇到新的章节标题，结束当前章节
                    if (line.trim().matches("^\\d+\\.\\s+.*") || 
                        line.trim().matches(".*：.*") && 
                        !sectionName.equals("题目解析")) {
                        break;
                    }
                    
                    sectionContent.append(line.trim()).append("\n");
                }
            }
            
            String result = sectionContent.toString().trim();
            return result.isEmpty() ? "暂无" + sectionName : result;
            
        } catch (Exception e) {
            Log.e(TAG, "解析AI内容失败：" + sectionName, e);
            return "解析失败，请查看详细解析";
        }
    }
    
    /**
     * 从AI内容中解析列表部分
     */
    private List<String> parseAIContentList(String content, String sectionName) {
        List<String> items = new ArrayList<>();
        String sectionContent = parseAIContent(content, sectionName);
        
        if (sectionContent.equals("暂无" + sectionName) || sectionContent.isEmpty()) {
            items.add("暂无" + sectionName);
            return items;
        }
        
        try {
            // 按行分割，过滤空行
            String[] lines = sectionContent.split("\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                // 移除数字序号（如"1. "、"2. "等）
                String cleanedLine = line.trim().replaceAll("^\\d+\\.\\s*", "");
                if (!cleanedLine.isEmpty()) {
                    items.add(cleanedLine);
                }
            }
            
            if (items.isEmpty()) {
                items.add("暂无" + sectionName);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "解析AI列表内容失败：" + sectionName, e);
            items.add("解析失败");
        }
        
        return items;
    }
    
    /**
     * 生成本地AI解析（fallback方案）
     */
    private void generateLocalAIAnalysis(Question question, String userAnswer, AICallback callback) {
        executor.execute(() -> {
            try {
                AIAnalysisResult result = new AIAnalysisResult();
                
                // 根据题目类型生成不同的解析
                String analysis = generateLocalAnalysisByType(question, userAnswer);
                result.setAnalysisText(analysis);
                
                // 生成解题步骤
                result.setSolutionSteps(generateSolutionSteps(question));
                
                // 生成核心考点
                result.setKeyPoints(generateKeyPoints(question));
                
                // 生成易错点
                result.setCommonMistakes(generateCommonMistakes(question));
                
                // 生成学习建议
                result.setRecommendations(generateRecommendations(question));
                
                // 在主线程回调
                new android.os.Handler(context.getMainLooper()).post(() -> {
                    callback.onSuccess(result);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "生成本地AI解析失败", e);
                new android.os.Handler(context.getMainLooper()).post(() -> {
                    callback.onFailure("解析生成失败，请稍后重试");
                });
            }
        });
    }
    
    /**
     * 根据题目类型生成本地解析
     */
    private String generateLocalAnalysisByType(Question question, String userAnswer) {
        StringBuilder analysis = new StringBuilder();
        
        // 基础解析
        analysis.append("题目解析：\n");
        
        // 根据题目类型生成特定解析
        switch (question.getType()) {
            case "single_choice":
                analysis.append(generateSingleChoiceAnalysis(question, userAnswer));
                break;
            case "multiple_choice":
                analysis.append(generateMultipleChoiceAnalysis(question, userAnswer));
                break;
            case "true_false":
                analysis.append(generateTrueFalseAnalysis(question, userAnswer));
                break;
            case "fill_blank":
                analysis.append(generateFillBlankAnalysis(question, userAnswer));
                break;
            default:
                analysis.append("本题为" + getQuestionTypeText(question.getType()) + "\n");
                break;
        }
        
        // 添加难度说明
        analysis.append("\n难度评估：" + getDifficultyText(question.getDifficulty()));
        
        return analysis.toString();
    }
    
    /**
     * 生成单选题解析
     */
    private String generateSingleChoiceAnalysis(Question question, String userAnswer) {
        StringBuilder analysis = new StringBuilder();
        
        try {
            int correctIndex = question.getCorrectAnswer();
            List<String> options = question.getOptions();
            
            if (correctIndex >= 0 && correctIndex < options.size()) {
                analysis.append("正确答案是：" + (char)('A' + correctIndex) + ". " + options.get(correctIndex) + "\n\n");
                
                if (userAnswer != null && !userAnswer.isEmpty()) {
                    int userIndex = Integer.parseInt(userAnswer);
                    if (userIndex == correctIndex) {
                        analysis.append("✅ 您的回答正确！\n");
                    } else {
                        analysis.append("❌ 您的回答错误，正确答案是选项" + (char)('A' + correctIndex) + "\n");
                    }
                }
            }
            
        } catch (Exception e) {
            analysis.append("题目格式异常，无法生成详细解析。");
        }
        
        return analysis.toString();
    }
    
    /**
     * 生成多选题解析
     */
    private String generateMultipleChoiceAnalysis(Question question, String userAnswer) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("本题为多选题，需要选择所有正确的选项。\n");
        analysis.append("多选题考察综合判断能力，需要仔细分析每个选项。\n");
        
        if (userAnswer != null && !userAnswer.isEmpty()) {
            analysis.append("您选择了选项：" + userAnswer + "\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * 生成判断题解析
     */
    private String generateTrueFalseAnalysis(Question question, String userAnswer) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("本题为判断题，需要判断陈述的正确性。\n");
        
        if (userAnswer != null && !userAnswer.isEmpty()) {
            analysis.append("您选择了：" + (userAnswer.equals("0") ? "正确" : "错误") + "\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * 生成填空题解析
     */
    private String generateFillBlankAnalysis(Question question, String userAnswer) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("本题为填空题，需要根据题意填写正确答案。\n");
        
        if (userAnswer != null && !userAnswer.isEmpty()) {
            analysis.append("您填写的答案是：" + userAnswer + "\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * 生成解题步骤
     */
    private List<String> generateSolutionSteps(Question question) {
        List<String> steps = new ArrayList<>();
        
        // 根据题目类型生成不同解题步骤
        switch (question.getType()) {
            case "single_choice":
                steps.add("第一步：仔细阅读题目，理解题意");
                steps.add("第二步：分析每个选项的含义和区别");
                steps.add("第三步：排除明显错误的选项");
                steps.add("第四步：选择最符合题意的正确答案");
                break;
            case "multiple_choice":
                steps.add("第一步：完整阅读题目，注意'多选'关键词");
                steps.add("第二步：逐个分析每个选项的正确性");
                steps.add("第三步：选择所有符合条件的选项");
                steps.add("第四步：确认没有遗漏正确选项");
                break;
            default:
                steps.add("请根据具体题目类型制定解题策略");
                break;
        }
        
        return steps;
    }
    
    /**
     * 生成核心考点
     */
    private List<String> generateKeyPoints(Question question) {
        List<String> keyPoints = new ArrayList<>();
        
        // 根据题目分类生成核心考点
        String category = question.getCategory();
        if (category != null) {
            switch (category.toLowerCase()) {
                case "java基础":
                    keyPoints.add("基本数据类型与引用类型的区别");
                    keyPoints.add("面向对象编程概念");
                    keyPoints.add("常用类库的使用");
                    break;
                case "android开发":
                    keyPoints.add("Android四大组件");
                    keyPoints.add("常用布局方式");
                    keyPoints.add("生命周期管理");
                    break;
                case "数据结构":
                    keyPoints.add("数组、链表、栈、队列等基础结构");
                    keyPoints.add("排序算法原理");
                    keyPoints.add("查找算法应用");
                    break;
                default:
                    keyPoints.add("基础概念理解");
                    keyPoints.add("逻辑推理能力");
                    keyPoints.add("实际应用能力");
                    break;
            }
        }
        
        return keyPoints;
    }
    
    /**
     * 生成易错点
     */
    private List<String> generateCommonMistakes(Question question) {
        List<String> mistakes = new ArrayList<>();
        
        // 根据题目类型和难度生成常见错误
        mistakes.add("审题不仔细，忽略关键信息");
        mistakes.add("概念混淆，理解偏差");
        mistakes.add("粗心大意，计算错误");
        
        if (question.getDifficulty() >= 4) {
            mistakes.add("复杂情境下思维定势影响判断");
            mistakes.add("多知识点综合应用能力不足");
        }
        
        return mistakes;
    }
    
    /**
     * 生成学习建议
     */
    private List<String> generateRecommendations(Question question) {
        List<String> recommendations = new ArrayList<>();
        
        recommendations.add("加强相关概念的理解和记忆");
        recommendations.add("多做类似题目巩固知识");
        recommendations.add("注意总结解题方法和技巧");
        
        if (question.getDifficulty() >= 4) {
            recommendations.add("建议进行专题训练，提高综合应用能力");
        }
        
        return recommendations;
    }
    
    /**
     * 解析AI返回的解析文本
     */
    private String parseAnalysisText(Map<String, Object> response) {
        Object analysis = response.get("analysis");
        return analysis != null ? analysis.toString() : "暂无详细解析";
    }
    
    /**
     * 解析解题步骤
     */
    private List<String> parseSolutionSteps(Map<String, Object> response) {
        return parseStringList(response, "solution_steps");
    }
    
    /**
     * 解析核心考点
     */
    private List<String> parseKeyPoints(Map<String, Object> response) {
        return parseStringList(response, "key_points");
    }
    
    /**
     * 解析易错点
     */
    private List<String> parseCommonMistakes(Map<String, Object> response) {
        return parseStringList(response, "common_mistakes");
    }
    
    /**
     * 解析学习建议
     */
    private List<String> parseRecommendations(Map<String, Object> response) {
        return parseStringList(response, "recommendations");
    }
    
    /**
     * 通用字符串列表解析方法
     */
    private List<String> parseStringList(Map<String, Object> response, String key) {
        List<String> result = new ArrayList<>();
        
        try {
            Object obj = response.get(key);
            if (obj instanceof List) {
                result = (List<String>) obj;
            }
        } catch (Exception e) {
            Log.e(TAG, "解析" + key + "失败", e);
        }
        
        return result;
    }
    
    /**
     * 获取题目类型文本
     */
    private String getQuestionTypeText(String type) {
        switch (type) {
            case "single_choice": return "单选题";
            case "multiple_choice": return "多选题";
            case "true_false": return "判断题";
            case "fill_blank": return "填空题";
            default: return "单选题";
        }
    }
    
    /**
     * 获取难度文本
     */
    private String getDifficultyText(int difficulty) {
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "较易";
            case 3: return "中等";
            case 4: return "较难";
            case 5: return "困难";
            default: return "中等";
        }
    }
    
    /**
     * AI解析结果类
     */
    public static class AIAnalysisResult {
        private String analysisText;
        private List<String> solutionSteps;
        private List<String> keyPoints;
        private List<String> commonMistakes;
        private List<String> recommendations;
        
        public String getAnalysisText() { return analysisText; }
        public void setAnalysisText(String analysisText) { this.analysisText = analysisText; }
        
        public List<String> getSolutionSteps() { return solutionSteps; }
        public void setSolutionSteps(List<String> solutionSteps) { this.solutionSteps = solutionSteps; }
        
        public List<String> getKeyPoints() { return keyPoints; }
        public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }
        
        public List<String> getCommonMistakes() { return commonMistakes; }
        public void setCommonMistakes(List<String> commonMistakes) { this.commonMistakes = commonMistakes; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
    
    /**
     * AI解析回调接口
     */
    public interface AICallback {
        void onSuccess(AIAnalysisResult result);
        void onFailure(String errorMessage);
    }
}

