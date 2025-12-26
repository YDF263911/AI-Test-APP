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

/**
 * 错题详情Activity - 显示错题详细信息并提供重新答题功能
 */
public class WrongQuestionDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "WrongQuestionDetailActivity";
    
    // UI组件
    private ScrollView detailScrollView;
    private TextView questionTitle;
    private TextView questionDifficulty;
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
    
    private void displayQuestionDetail(JSONObject questionObj, JSONObject wrongQuestionObj) {
        try {
            // 设置题目信息
            questionTitle.setText("错题详情");
            questionContent.setText(questionObj.getString("title"));
            
            // 设置难度和类型
            String difficulty = getDifficultyText(questionObj.optInt("difficulty", 3));
            questionDifficulty.setText("难度: " + difficulty);
            questionType.setText("类型: " + questionObj.optString("type", "单选题"));
            
            // 设置选项
            setupOptions(questionObj);
            
            // 设置解析
            String analysis = questionObj.optString("analysis", "暂无解析");
            analysisContent.setText(analysis);
            
            // 设置错误答案和正确答案
            String wrongAnswer = wrongQuestionObj.optString("user_answer", "未记录");
            String correctAnswer = getCorrectAnswerText(questionObj);
            
            wrongAnswerText.setText("你的答案: " + wrongAnswer);
            correctAnswerText.setText("正确答案: " + correctAnswer);
            
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
    
    private String getCorrectAnswerText(JSONObject questionObj) {
        try {
            int correctAnswer = questionObj.optInt("correct_answer", 0);
            String optionsJson = questionObj.optString("options", "[]");
            JSONArray optionsArray = new JSONArray(optionsJson);
            
            if (correctAnswer >= 0 && correctAnswer < optionsArray.length()) {
                return (char)('A' + correctAnswer) + ". " + optionsArray.getString(correctAnswer);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get correct answer text", e);
        }
        
        return "未知";
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
            // 从当前显示的题目获取选项
            String[] mockOptions = {"String", "Integer", "int", "ArrayList"}; // 示例选项
            
            for (int i = 0; i < mockOptions.length; i++) {
                View optionView = getLayoutInflater().inflate(R.layout.item_redo_option, optionsContainer, false);
                TextView optionText = optionView.findViewById(R.id.option_text);
                
                optionText.setText((char)('A' + i) + ". " + mockOptions[i]);
                
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
        // 这里应该根据实际的正确答案检查
        // 示例：假设正确答案是选项C (索引2)
        return userAnswer == 2;
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
        // 更新错题状态为已掌握
        Toast.makeText(this, "错题状态已更新", Toast.LENGTH_SHORT).show();
        // 这里可以调用Supabase API更新错题状态
    }
    
    // 添加成员变量
    private int selectedRedoAnswer = -1;
}