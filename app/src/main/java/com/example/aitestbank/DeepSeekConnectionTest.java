package com.example.aitestbank;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aitestbank.model.Question;
import com.example.aitestbank.service.AIService;

import java.util.Arrays;

/**
 * DeepSeek API连接测试Activity
 */
public class DeepSeekConnectionTest extends AppCompatActivity {
    
    private static final String TAG = "DeepSeekTest";
    private TextView resultTextView;
    private Button testButton;
    private AIService aiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deepseek_test);
        
        initViews();
        setupListeners();
        
        // 初始化AI服务
        aiService = new AIService(this);
    }
    
    private void initViews() {
        resultTextView = findViewById(R.id.result_text_view);
        testButton = findViewById(R.id.test_button);
    }
    
    private void setupListeners() {
        testButton.setOnClickListener(v -> testDeepSeekConnection());
    }
    
    /**
     * 测试DeepSeek API连接
     */
    private void testDeepSeekConnection() {
        resultTextView.setText("正在测试DeepSeek API连接...\n\n");
        testButton.setEnabled(false);
        
        // 创建一个测试题目
        Question testQuestion = createTestQuestion();
        
        // 测试AI解析功能
        aiService.getAIAnalysis(testQuestion, "1", new AIService.AICallback() {
            @Override
            public void onSuccess(AIService.AIAnalysisResult result) {
                runOnUiThread(() -> {
                    StringBuilder resultText = new StringBuilder();
                    resultText.append("✅ DeepSeek API连接成功！\n\n");
                    resultText.append("📝 题目解析：\n");
                    resultText.append(result.getAnalysisText()).append("\n\n");
                    
                    resultText.append("🔍 解题步骤：\n");
                    for (int i = 0; i < result.getSolutionSteps().size(); i++) {
                        resultText.append("  ").append(i + 1).append(". ").append(result.getSolutionSteps().get(i)).append("\n");
                    }
                    resultText.append("\n");
                    
                    resultText.append("🎯 核心考点：\n");
                    for (String point : result.getKeyPoints()) {
                        resultText.append("  • ").append(point).append("\n");
                    }
                    resultText.append("\n");
                    
                    resultText.append("⚠️ 易错点提醒：\n");
                    for (String mistake : result.getCommonMistakes()) {
                        resultText.append("  • ").append(mistake).append("\n");
                    }
                    
                    resultTextView.setText(resultText.toString());
                    testButton.setEnabled(true);
                    
                    Toast.makeText(DeepSeekConnectionTest.this, "DeepSeek连接测试成功！", Toast.LENGTH_LONG).show();
                });
                
                Log.d(TAG, "DeepSeek API测试成功");
            }
            
            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    String resultText = "❌ DeepSeek API连接失败\n\n" +
                            "错误信息：" + errorMessage + "\n\n" +
                            "可能原因：\n" +
                            "• API密钥无效或过期\n" +
                            "• 网络连接问题\n" +
                            "• DeepSeek服务暂时不可用\n" +
                            "• 请求频率限制\n\n" +
                            "已自动切换到本地解析模式。";
                    
                    resultTextView.setText(resultText);
                    testButton.setEnabled(true);
                    
                    Toast.makeText(DeepSeekConnectionTest.this, "DeepSeek连接测试失败，使用本地解析", Toast.LENGTH_LONG).show();
                });
                
                Log.e(TAG, "DeepSeek API测试失败: " + errorMessage);
            }
        });
    }
    
    /**
     * 创建测试题目
     */
    private Question createTestQuestion() {
        Question question = new Question();
        question.setId("test_001");
        question.setTitle("在Java中，以下哪个关键字用于定义常量？");
        question.setOptions(Arrays.asList(
            "A. var",
            "B. let", 
            "C. const",
            "D. final"
        ));
        question.setCorrectAnswer(3); // D选项
        question.setType("single_choice");
        question.setCategory("Java基础");
        question.setDifficulty(2);
        question.setAnalysis("Java中使用final关键字定义常量。");
        
        return question;
    }
}