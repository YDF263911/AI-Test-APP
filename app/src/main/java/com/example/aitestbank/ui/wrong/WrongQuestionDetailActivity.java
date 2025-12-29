package com.example.aitestbank.ui.wrong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.supabase.SimpleSupabaseClient;
import com.example.aitestbank.ui.adapter.WrongQuestionAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 错题详情Activity - 显示错题详细信息并提供重新答题功能
 */
public class WrongQuestionDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "WrongQuestionDetailActivity";
    
    // UI组件
    private ScrollView detailScrollView;
    private TextView questionTitle;
    private TextView questionDifficulty;
    private TextView questionCategory;
    private TextView questionType;
    private TextView questionContent;
    private LinearLayout optionsContainer;
    private LinearLayout analysisContainer;
    private TextView analysisContent;
    private TextView wrongAnswerText;
    private TextView correctAnswerText;
    private Button redoButton;
    private Button backButton;
    
    // 数据和客户端
    private SimpleSupabaseClient supabaseClient;
    private WrongQuestionAdapter.WrongQuestionItem wrongQuestionItem;
    private Question question;
    private JSONObject currentQuestionObj; // 添加字段存储当前题目数据
    
    /**
     * 获取分类的中文显示名称
     */
    private String getCategoryDisplayName(String category) {
        if (category == null || category.isEmpty()) {
            return "未分类";
        }
        
        // 分类映射表
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("campus_recruitment", "校园招聘");
        categoryMap.put("Java基础", "Java基础");
        categoryMap.put("Python", "Python");
        categoryMap.put("操作系统", "操作系统");
        categoryMap.put("数据库/SQL", "数据库/SQL");
        categoryMap.put("数据结构与算法", "数据结构与算法");
        categoryMap.put("计算机网络", "计算机网络");
        categoryMap.put("Android开发", "Android开发");
        categoryMap.put("Java框架", "Java框架");
        categoryMap.put("前端框架", "前端框架");
        categoryMap.put("工具", "开发工具");
        categoryMap.put("数据库", "数据库");
        categoryMap.put("编程基础", "编程基础");
        categoryMap.put("网络", "网络");
        
        return categoryMap.getOrDefault(category, category);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_question_detail);
        
        // 获取传递的错题数据
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("wrong_question_id")) {
            String wrongQuestionId = intent.getStringExtra("wrong_question_id");
            loadWrongQuestionDetail(wrongQuestionId);
        } else {
            Toast.makeText(this, "未找到错题数据", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        initViews();
        setupClickListeners();
        setupSupabase();
    }
    
    private void initViews() {
        detailScrollView = findViewById(R.id.detail_scroll_view);
        questionTitle = findViewById(R.id.question_title);
        questionDifficulty = findViewById(R.id.question_difficulty);
        questionCategory = findViewById(R.id.question_category);
        questionType = findViewById(R.id.question_type);
        questionContent = findViewById(R.id.question_content);
        optionsContainer = findViewById(R.id.options_container);
        analysisContainer = findViewById(R.id.analysis_container);
        analysisContent = findViewById(R.id.analysis_content);
        wrongAnswerText = findViewById(R.id.wrong_answer_text);
        correctAnswerText = findViewById(R.id.correct_answer_text);
        redoButton = findViewById(R.id.redo_button);
        backButton = findViewById(R.id.back_button);
    }
    
    private void setupSupabase() {
        supabaseClient = SimpleSupabaseClient.getInstance();
    }
    
    private void setupClickListeners() {
        redoButton.setOnClickListener(v -> {
            redoQuestion();
        });
        
        backButton.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void loadWrongQuestionDetail(String wrongQuestionId) {
        Toast.makeText(this, "正在加载错题详情...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                // 从Supabase获取错题详情
                String result = supabaseClient.query("wrong_questions", "*", "id=eq." + wrongQuestionId);
                Log.d(TAG, "Wrong question detail from Supabase: " + result);
                
                JSONArray jsonArray = new JSONArray(result);
                if (jsonArray.length() > 0) {
                    JSONObject wrongQuestionObj = jsonArray.getJSONObject(0);
                    String questionId = wrongQuestionObj.getString("question_id");
                    
                    // 获取题目详情
                    loadQuestionDetail(questionId, wrongQuestionObj);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load wrong question detail", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    finish(); // 直接关闭页面，不显示模拟数据
                });
            }
        }).start();
    }
    
    private void loadQuestionDetail(String questionId, JSONObject wrongQuestionObj) {
        try {
            String questionResult = supabaseClient.query("questions", "*", "id=eq." + questionId);
            JSONArray questionArray = new JSONArray(questionResult);
            
            if (questionArray.length() > 0) {
                JSONObject questionObj = questionArray.getJSONObject(0);
                
                runOnUiThread(() -> {
                    displayQuestionDetail(questionObj, wrongQuestionObj);
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load question detail", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "题目数据加载失败", Toast.LENGTH_SHORT).show();
                finish(); // 直接关闭页面，不显示模拟数据
            });
        }
    }
    
    /**
     * 获取题目类型的友好显示名称
     */
    private String getQuestionTypeDisplayName(String type) {
        if (type == null || type.isEmpty()) {
            return "单选题";
        }
        
        // 类型映射表
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("single_choice", "单选题");
        typeMap.put("multiple_choice", "多选题");
        typeMap.put("true_false", "判断题");
        typeMap.put("fill_blank", "填空题");
        typeMap.put("programming", "编程题");
        
        return typeMap.getOrDefault(type, type);
    }
    
    private void displayQuestionDetail(JSONObject questionObj, JSONObject wrongQuestionObj) {
        try {
            // 保存当前题目对象用于重新答题
            this.currentQuestionObj = questionObj;
            
            // 设置题目信息
            questionTitle.setText("错题详情");
            questionContent.setText(questionObj.getString("title"));
            
            // 设置分类
            String category = questionObj.optString("category", "未分类");
            String displayCategory = getCategoryDisplayName(category);
            questionCategory.setText("分类: " + displayCategory);
            
            // 设置难度和类型 - 使用友好名称
            String difficulty = getDifficultyText(questionObj.optInt("difficulty", 3));
            questionDifficulty.setText("难度: " + difficulty);
            String questionTypeStr = questionObj.optString("type", "single_choice");
            String displayType = getQuestionTypeDisplayName(questionTypeStr);
            
            // 只在非单选题时显示题型
            if (!"single_choice".equals(questionTypeStr)) {
                questionType.setVisibility(View.VISIBLE);
                questionType.setText("题型: " + displayType);
            } else {
                questionType.setVisibility(View.GONE);
            }
            
            // 设置选项
            setupOptions(questionObj);
            
            // 设置详细解析
            setupDetailedAnalysis(questionObj);
            
            // 设置错误答案和正确答案 - 显示具体内容
            int wrongAnswerIndex = wrongQuestionObj.optInt("user_answer", -1);
            String wrongAnswerText = getAnswerTextWithOption(questionObj, wrongAnswerIndex);
            String correctAnswerText = getCorrectAnswerText(questionObj);
            
            this.wrongAnswerText.setText("你的答案: " + wrongAnswerText);
            this.correctAnswerText.setText("正确答案: " + correctAnswerText);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to display question detail", e);
        }
    }
    
    private void setupOptions(JSONObject questionObj) {
        optionsContainer.removeAllViews();
        
        try {
            String optionsJson = questionObj.optString("options", "[]");
            JSONArray optionsArray = new JSONArray(optionsJson);
            
            for (int i = 0; i < optionsArray.length(); i++) {
                String option = optionsArray.getString(i);
                TextView optionView = new TextView(this);
                optionView.setText((char)('A' + i) + ". " + option);
                optionView.setTextSize(16);
                optionView.setPadding(16, 8, 16, 8);
                
                // 标记正确答案
                int correctAnswer = questionObj.optInt("correct_answer", 0);
                if (i == correctAnswer) {
                    optionView.setBackgroundColor(getResources().getColor(R.color.success_green));
                    optionView.setTextColor(getResources().getColor(R.color.white));
                }
                
                optionsContainer.addView(optionView);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup options", e);
        }
    }
    
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
     * 根据答案索引获取带选项的文本显示
     */
    private String getAnswerTextWithOption(JSONObject questionObj, int answerIndex) {
        try {
            if (answerIndex == -1) {
                return "未作答";
            }
            
            String optionsJson = questionObj.optString("options", "[]");
            JSONArray optionsArray = new JSONArray(optionsJson);
            
            if (answerIndex >= 0 && answerIndex < optionsArray.length()) {
                char optionLetter = (char)('A' + answerIndex);
                String optionText = optionsArray.getString(answerIndex);
                return optionLetter + ". " + optionText;
            } else {
                return "无效答案(索引:" + answerIndex + ")";
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get answer text with option", e);
            return "解析错误";
        }
    }
    
    private String getCorrectAnswerText(JSONObject questionObj) {
        try {
            int correctAnswer = questionObj.optInt("correct_answer", 0);
            return getAnswerTextWithOption(questionObj, correctAnswer);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get correct answer text", e);
            return "未知";
        }
    }
    
    /**
     * 设置详细解析（包含知识点、解题思路等）
     */
    private void setupDetailedAnalysis(JSONObject questionObj) {
        try {
            StringBuilder analysisBuilder = new StringBuilder();
            
            // 主要解析内容
            String mainAnalysis = questionObj.optString("analysis", "");
            if (!mainAnalysis.isEmpty()) {
                analysisBuilder.append("【题目解析】\n").append(mainAnalysis).append("\n\n");
            }
            
            // 知识点标签
            String tags = questionObj.optString("tags", "");
            if (!tags.isEmpty()) {
                analysisBuilder.append("【知识点】\n").append(formatTags(tags)).append("\n\n");
            }
            
            // 解题思路
            String solution = questionObj.optString("solution", "");
            if (!solution.isEmpty()) {
                analysisBuilder.append("【解题思路】\n").append(solution).append("\n\n");
            }
            
            // 易错点提醒
            String tips = questionObj.optString("tips", "");
            if (!tips.isEmpty()) {
                analysisBuilder.append("【易错点提醒】\n").append(tips).append("\n\n");
            }
            
            // 如果没有任何解析内容，显示默认提示
            String finalAnalysis = analysisBuilder.toString().trim();
            if (finalAnalysis.isEmpty()) {
                finalAnalysis = "暂无详细解析，建议复习相关知识点后再练习类似题目。";
            }
            
            analysisContent.setText(finalAnalysis);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup detailed analysis", e);
            analysisContent.setText("解析加载失败，请稍后重试。");
        }
    }
    
    /**
     * 格式化标签字符串
     */
    private String formatTags(String tags) {
        if (tags == null || tags.isEmpty()) {
            return "无";
        }
        
        try {
            // 如果是JSON数组格式的标签
            if (tags.startsWith("[") && tags.endsWith("]")) {
                JSONArray tagArray = new JSONArray(tags);
                StringBuilder formattedTags = new StringBuilder();
                for (int i = 0; i < tagArray.length(); i++) {
                    if (i > 0) formattedTags.append("、");
                    formattedTags.append(tagArray.getString(i));
                }
                return formattedTags.toString();
            } else {
                // 普通逗号分隔的标签
                String[] tagList = tags.split(",");
                StringBuilder formattedTags = new StringBuilder();
                for (int i = 0; i < tagList.length; i++) {
                    if (i > 0) formattedTags.append("、");
                    formattedTags.append(tagList[i].trim());
                }
                return formattedTags.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to format tags", e);
            return tags; // 返回原字符串
        }
    }
    

    
    private void redoQuestion() {
        // 显示重新答题界面
        showRedoQuestionDialog();
    }
    
    private void showRedoQuestionDialog() {
        // 创建完整的重新答题对话框
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_redo_question, null);
        
        TextView questionTitle = dialogView.findViewById(R.id.redo_question_title);
        TextView questionContent = dialogView.findViewById(R.id.redo_question_content);
        LinearLayout optionsContainer = dialogView.findViewById(R.id.redo_options_container);
        
        // 设置题目信息
        questionTitle.setText("重新答题");
        questionContent.setText(this.questionContent.getText());
        
        // 设置选项
        setupRedoOptions(optionsContainer);
        
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("提交答案", null) // 稍后设置点击事件
            .setNegativeButton("取消", (d, which) -> d.dismiss())
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button submitButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            submitButton.setOnClickListener(v -> {
                submitRedoAnswer(dialog);
            });
        });
        
        dialog.show();
    }
    
    private void setupRedoOptions(LinearLayout optionsContainer) {
        optionsContainer.removeAllViews();
        
        try {
            if (currentQuestionObj == null) {
                Log.e(TAG, "currentQuestionObj is null");
                return;
            }
            
            // 从实际题目数据获取选项
            String optionsJson = currentQuestionObj.optString("options", "[]");
            JSONArray optionsArray = new JSONArray(optionsJson);
            
            for (int i = 0; i < optionsArray.length(); i++) {
                String option = optionsArray.getString(i);
                
                View optionView = getLayoutInflater().inflate(R.layout.item_redo_option, optionsContainer, false);
                TextView optionText = optionView.findViewById(R.id.option_text);
                
                optionText.setText((char)('A' + i) + ". " + option);
                
                final int selectedIndex = i;
                optionView.setOnClickListener(v -> {
                    // 清除其他选项的选中状态
                    for (int j = 0; j < optionsContainer.getChildCount(); j++) {
                        View child = optionsContainer.getChildAt(j);
                        child.setBackgroundColor(getResources().getColor(R.color.bg_secondary));
                    }
                    
                    // 设置当前选项为选中状态
                    optionView.setBackgroundColor(getResources().getColor(R.color.primary_light));
                    
                    // 保存用户选择的答案
                    selectedRedoAnswer = selectedIndex;
                });
                
                optionsContainer.addView(optionView);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup redo options", e);
        }
    }
    
    private void submitRedoAnswer(android.app.AlertDialog dialog) {
        if (selectedRedoAnswer == -1) {
            Toast.makeText(this, "请选择答案", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查答案是否正确
        boolean isCorrect = checkAnswer(selectedRedoAnswer);
        
        // 显示结果
        showRedoResult(isCorrect);
        
        dialog.dismiss();
    }
    
    private boolean checkAnswer(int userAnswer) {
        if (currentQuestionObj == null) {
            Log.e(TAG, "currentQuestionObj is null in checkAnswer");
            return false;
        }
        
        // 根据实际的正确答案检查
        int correctAnswer = currentQuestionObj.optInt("correct_answer", 0);
        String optionsJson = currentQuestionObj.optString("options", "[]");
        String title = currentQuestionObj.optString("title", "");
        
        try {
            JSONArray optionsArray = new JSONArray(optionsJson);
            
            // 边界检查：确保索引在有效范围内
            boolean isValidUserAnswer = (userAnswer >= 0 && userAnswer < optionsArray.length());
            boolean isValidCorrectAnswer = (correctAnswer >= 0 && correctAnswer < optionsArray.length());
            
            String userAnswerText = isValidUserAnswer ? optionsArray.getString(userAnswer) : "INVALID_INDEX";
            String correctAnswerText = isValidCorrectAnswer ? optionsArray.getString(correctAnswer) : "INVALID_INDEX";
            
            Log.d(TAG, "=== Answer Check Debug ===");
            Log.d(TAG, "Question: " + title);
            Log.d(TAG, "User answer index: " + userAnswer + ", valid: " + isValidUserAnswer + ", text: " + userAnswerText);
            Log.d(TAG, "Correct answer index: " + correctAnswer + ", valid: " + isValidCorrectAnswer + ", text: " + correctAnswerText);
            Log.d(TAG, "Options count: " + optionsArray.length() + ", Options: " + optionsJson);
            
            // 如果有无效的索引，返回false并记录错误
            if (!isValidUserAnswer || !isValidCorrectAnswer) {
                Log.e(TAG, "Invalid answer index detected! User: " + userAnswer + ", Correct: " + correctAnswer + ", Options length: " + optionsArray.length());
                return false;
            }
            
            boolean matchResult = (userAnswer == correctAnswer);
            Log.d(TAG, "Match result: " + matchResult);
            Log.d(TAG, "==========================");
            
            return matchResult;
            
        } catch (Exception e) {
            Log.e(TAG, "Error in checkAnswer", e);
            return false;
        }
    }
    
    private void showRedoResult(boolean isCorrect) {
        String message = isCorrect ? "恭喜你！回答正确！" : "答案不正确，请继续努力！";
        int icon = isCorrect ? R.drawable.ic_success : R.drawable.ic_error;
        
        new android.app.AlertDialog.Builder(this)
            .setTitle(isCorrect ? "回答正确" : "回答错误")
            .setMessage(message)
            .setIcon(icon)
            .setPositiveButton("确定", (dialog, which) -> {
                // 如果回答正确，可以更新错题状态
                if (isCorrect) {
                    updateWrongQuestionStatus();
                }
            })
            .show();
    }
    
    private void updateWrongQuestionStatus() {
        // 获取当前错题ID并更新状态为已掌握
        new Thread(() -> {
            try {
                // 我们需要重新获取错题ID，因为loadWrongQuestionDetail是在线程中执行的
                // 这里简化处理，在实际应用中应该保存wrongQuestionId作为成员变量
                String wrongQuestionId = getIntent().getStringExtra("wrong_question_id");
                if (wrongQuestionId == null || wrongQuestionId.isEmpty()) {
                    Log.e(TAG, "Cannot update wrong question status: wrong_question_id is null");
                    runOnUiThread(() -> {
                        Toast.makeText(WrongQuestionDetailActivity.this, "更新失败：未找到错题ID", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // 构建更新数据 - 使用正确的字段 is_mastered
                JSONObject updateData = new JSONObject();
                updateData.put("is_mastered", true);
                updateData.put("last_wrong_time", System.currentTimeMillis());
                
                // 调用Supabase更新接口
                String result = supabaseClient.update("wrong_questions", wrongQuestionId, updateData.toString());
                Log.d(TAG, "Update wrong question status result: " + result);
                
                // 检查更新是否成功
                if (result != null && !result.contains("error")) {
                    runOnUiThread(() -> {
                        Toast.makeText(WrongQuestionDetailActivity.this, "✅ 恭喜！该题已标记为已掌握", Toast.LENGTH_SHORT).show();
                        
                        // 发送广播通知错题列表刷新（统一Action）
                        Intent refreshIntent = new Intent("ACTION_WRONG_QUESTION_UPDATED");
                        sendBroadcast(refreshIntent);
                        
                        // 延迟关闭页面，让用户看到成功提示
                        new android.os.Handler().postDelayed(() -> {
                            finish();
                        }, 1500);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(WrongQuestionDetailActivity.this, "❌ 更新失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to update wrong question status", e);
                runOnUiThread(() -> {
                    Toast.makeText(WrongQuestionDetailActivity.this, "❌ 更新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    // 添加成员变量
    private int selectedRedoAnswer = -1;
}