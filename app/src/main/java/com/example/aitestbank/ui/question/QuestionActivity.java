package com.example.aitestbank.ui.question;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.aitestbank.R;

import java.util.ArrayList;

/**
 * 答题Activity - 承载QuestionFragment的容器
 */
public class QuestionActivity extends AppCompatActivity {
    
    private static final String TAG = "QuestionActivity";
    
    // UI组件
    private TextView questionTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        
        // 获取传递的数据
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        
        // 如果是随机练习或错题复习模式，不需要具体的题目数据
        if ("random".equals(mode) || "wrong".equals(mode)) {
            // 这些模式会从数据库加载题目，不需要传递具体题目数据
        } else if (intent == null || !intent.hasExtra("question_id")) {
            // 如果是普通模式但缺少题目数据，显示错误
            Toast.makeText(this, "题目数据错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupQuestionFragment();
    }
    
    private void initViews() {
        questionTitle = findViewById(R.id.question_title);
        
        // 设置标题
        Intent intent = getIntent();
        String title = intent.getStringExtra("question_title");
        String category = intent.getStringExtra("question_category");
        
        if (questionTitle != null) {
            if (category != null && title != null) {
                questionTitle.setText(category + " - " + title);
            } else if (title != null) {
                questionTitle.setText(title);
            } else {
                questionTitle.setText("开始答题");
            }
        }
    }
    
    private void setupQuestionFragment() {
        // 创建QuestionFragment并传递数据
        QuestionFragment questionFragment = new QuestionFragment();
        
        // 创建Bundle传递数据
        Bundle bundle = new Bundle();
        Intent intent = getIntent();
        
        // 传递模式信息
        String mode = intent.getStringExtra("mode");
        if (mode != null) {
            bundle.putString("mode", mode);
        }
        
        // 传递标题信息
        String title = intent.getStringExtra("title");
        if (title != null) {
            bundle.putString("title", title);
        }
        
        // 传递AI解析开关
        boolean enableAI = intent.getBooleanExtra("enableAI", false);
        bundle.putBoolean("enableAI", enableAI);
        
        // 如果是普通模式，传递具体的题目数据
        if (!"random".equals(mode) && !"wrong".equals(mode)) {
            bundle.putString("question_id", intent.getStringExtra("question_id"));
            bundle.putString("question_title", intent.getStringExtra("question_title"));
            bundle.putString("question_category", intent.getStringExtra("question_category"));
            bundle.putString("question_type", intent.getStringExtra("question_type"));
            bundle.putInt("question_difficulty", intent.getIntExtra("question_difficulty", 3));
            bundle.putInt("question_correct_answer", intent.getIntExtra("question_correct_answer", 0));
            bundle.putString("question_analysis", intent.getStringExtra("question_analysis"));
            
            // 传递选项列表
            ArrayList<String> options = intent.getStringArrayListExtra("question_options");
            if (options != null) {
                bundle.putStringArrayList("question_options", options);
            }
        }
        
        questionFragment.setArguments(bundle);
        
        // 添加Fragment到容器
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.question_fragment_container, questionFragment);
        transaction.commit();
    }
}