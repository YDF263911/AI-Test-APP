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
                    Toast.makeText(this, "加载失败，显示示例数据", Toast.LENGTH_SHORT).show();
                    loadMockData();
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
                loadMockData();
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
    
    private void loadMockData() {
        // 模拟数据作为fallback
        questionTitle.setText("错题详情");
        questionContent.setText("下列哪个是Java的基本数据类型？");
        questionDifficulty.setText("难度: 简单");
        questionType.setText("类型: 单选题");
        
        // 设置选项
        optionsContainer.removeAllViews();
        String[] mockOptions = {"String", "Integer", "int", "ArrayList"};
        for (int i = 0; i < mockOptions.length; i++) {
            TextView optionView = new TextView(this);
            optionView.setText((char)('A' + i) + ". " + mockOptions[i]);
            optionView.setTextSize(16);
            optionView.setPadding(16, 8, 16, 8);
            
            if (i == 2) { // int是正确答案
                optionView.setBackgroundColor(getResources().getColor(R.color.success_green));
                optionView.setTextColor(getResources().getColor(R.color.white));
            }
            
            optionsContainer.addView(optionView);
        }
        
        // 设置解析
        analysisContent.setText("Java的基本数据类型包括：byte、short、int、long、float、double、char、boolean。String、Integer、ArrayList都是引用类型。");
        
        // 设置错误答案和正确答案
        wrongAnswerText.setText("你的答案: A. String");
        correctAnswerText.setText("正确答案: C. int");
    }
    
    private void redoQuestion() {
        // 跳转到重新答题界面
        Toast.makeText(this, "开始重新答题", Toast.LENGTH_SHORT).show();
        
        // 这里可以跳转到专门的重新答题界面，或者直接在当前界面实现答题功能
        // 简化实现：显示答题界面
        showRedoQuestionDialog();
    }
    
    private void showRedoQuestionDialog() {
        // 创建重新答题的对话框或界面
        // 这里简化实现，实际应该创建一个完整的答题界面
        
        Toast.makeText(this, "重新答题功能开发中...", Toast.LENGTH_SHORT).show();
        
        // 可以在这里实现一个简单的答题界面
        // 例如：显示题目和选项，让用户重新选择答案
        
        // 临时实现：显示一个简单的答题提示
        new android.app.AlertDialog.Builder(this)
            .setTitle("重新答题")
            .setMessage("请重新选择正确答案：" + questionContent.getText())
            .setPositiveButton("提交答案", (dialog, which) -> {
                Toast.makeText(this, "答案已提交", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }
}