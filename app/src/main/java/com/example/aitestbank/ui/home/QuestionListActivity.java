package com.example.aitestbank.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.supabase.SimpleSupabaseClient;
import com.example.aitestbank.ui.adapter.QuestionListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目列表Activity - 显示特定分类下的题目列表
 */
public class QuestionListActivity extends AppCompatActivity {
    
    private static final String TAG = "QuestionListActivity";
    
    // UI组件
    private TextView categoryTitle;
    private TextView questionCount;
    private RecyclerView questionRecyclerView;
    private Button backButton;
    
    // 数据和客户端
    private SimpleSupabaseClient supabaseClient;
    private QuestionListAdapter questionListAdapter;
    private List<Question> questions = new ArrayList<>();
    private String currentCategory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        
        // 获取传递的分类信息
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("category")) {
            currentCategory = intent.getStringExtra("category");
        } else {
            Toast.makeText(this, "未找到分类信息", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        initViews();
        setupSupabase();
        setupRecyclerView();
        setupClickListeners();
        loadQuestionsByCategory();
    }
    
    private void initViews() {
        categoryTitle = findViewById(R.id.category_title);
        questionCount = findViewById(R.id.question_count);
        questionRecyclerView = findViewById(R.id.question_recycler_view);
        backButton = findViewById(R.id.back_button);
        
        // 设置分类标题
        if (categoryTitle != null && currentCategory != null) {
            categoryTitle.setText(currentCategory + " 题目列表");
        }
    }
    
    private void setupSupabase() {
        supabaseClient = SimpleSupabaseClient.getInstance();
    }
    
    private void setupRecyclerView() {
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionListAdapter = new QuestionListAdapter();
        questionRecyclerView.setAdapter(questionListAdapter);
        
        // 设置题目项点击事件
        questionListAdapter.setOnQuestionClickListener(new QuestionListAdapter.OnQuestionClickListener() {
            @Override
            public void onQuestionClick(Question question) {
                // 跳转到答题界面
                startQuizForQuestion(question);
            }
        });
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            finish();
        });
    }
    
    private void loadQuestionsByCategory() {
        Toast.makeText(this, "正在加载" + currentCategory + "的题目...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                // 根据分类查询题目
                String filter = "category=eq." + currentCategory;
                String result = supabaseClient.query("questions", "*", filter);
                Log.d(TAG, "Questions for category " + currentCategory + ": " + result);
                
                List<Question> loadedQuestions = parseQuestionsFromSupabase(result);
                
                runOnUiThread(() -> {
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        questions.clear();
                        questions.addAll(loadedQuestions);
                        questionListAdapter.setQuestions(questions);
                        updateQuestionCount();
                        Toast.makeText(this, "成功加载 " + questions.size() + " 道题目", Toast.LENGTH_SHORT).show();
                    } else {
                        loadMockQuestions();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load questions by category", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "网络错误，显示示例题目", Toast.LENGTH_SHORT).show();
                    loadMockQuestions();
                });
            }
        }).start();
    }
    
    private List<Question> parseQuestionsFromSupabase(String jsonResult) {
        try {
            List<Question> questionList = new ArrayList<>();
            
            JSONArray jsonArray = new JSONArray(jsonResult);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                Question question = new Question();
                question.setId(obj.getString("id"));
                question.setTitle(obj.getString("title"));
                question.setCorrectAnswer(obj.getInt("correct_answer"));
                question.setAnalysis(obj.optString("analysis", "暂无解析"));
                question.setCategory(obj.optString("category", "未分类"));
                question.setDifficulty(obj.optInt("difficulty", 3));
                question.setType(obj.optString("type", "single_choice"));
                
                // 解析选项
                String optionsStr = obj.optString("options", "[]");
                List<String> options = parseOptions(optionsStr);
                question.setOptions(options);
                
                questionList.add(question);
            }
            
            return questionList;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse questions JSON", e);
            return null;
        }
    }
    
    private List<String> parseOptions(String optionsStr) {
        try {
            List<String> options = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(optionsStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                options.add(jsonArray.getString(i));
            }
            return options;
        } catch (Exception e) {
            // 如果解析失败，返回默认选项
            List<String> defaultOptions = new ArrayList<>();
            defaultOptions.add("选项A");
            defaultOptions.add("选项B");
            defaultOptions.add("选项C");
            defaultOptions.add("选项D");
            return defaultOptions;
        }
    }
    
    private void loadMockQuestions() {
        // 模拟数据作为fallback
        questions.clear();
        
        // 根据分类创建示例题目
        if (currentCategory.contains("Java")) {
            // Java相关题目
            Question q1 = new Question();
            q1.setId("1");
            q1.setTitle("下列哪个是Java的基本数据类型？");
            q1.setType("single_choice");
            q1.setDifficulty(2);
            q1.setCategory("Java基础");
            q1.setCorrectAnswer(2);
            List<String> options1 = new ArrayList<>();
            options1.add("String");
            options1.add("Integer");
            options1.add("int");
            options1.add("ArrayList");
            q1.setOptions(options1);
            q1.setAnalysis("Java的基本数据类型包括：byte、short、int、long、float、double、char、boolean。");
            
            Question q2 = new Question();
            q2.setId("2");
            q2.setTitle("Java中如何实现多线程？");
            q2.setType("single_choice");
            q2.setDifficulty(3);
            q2.setCategory("Java基础");
            q2.setCorrectAnswer(0);
            List<String> options2 = new ArrayList<>();
            options2.add("继承Thread类");
            options2.add("实现Runnable接口");
            options2.add("使用Executor框架");
            options2.add("以上都是");
            q2.setOptions(options2);
            q2.setAnalysis("Java中实现多线程的方式包括：继承Thread类、实现Runnable接口、使用Executor框架等。");
            
            questions.add(q1);
            questions.add(q2);
        } else {
            // 其他分类题目
            Question q1 = new Question();
            q1.setId("3");
            q1.setTitle("Android中常用的布局有哪些？");
            q1.setType("multiple_choice");
            q1.setDifficulty(3);
            q1.setCategory("Android开发");
            q1.setCorrectAnswer(-1);
            List<String> options1 = new ArrayList<>();
            options1.add("LinearLayout");
            options1.add("RelativeLayout");
            options1.add("ConstraintLayout");
            options1.add("FrameLayout");
            q1.setOptions(options1);
            q1.setAnalysis("Android中常用的布局包括：LinearLayout、RelativeLayout、ConstraintLayout、FrameLayout等。");
            
            questions.add(q1);
        }
        
        questionListAdapter.setQuestions(questions);
        updateQuestionCount();
    }
    
    private void updateQuestionCount() {
        if (questionCount != null) {
            questionCount.setText("共 " + questions.size() + " 道题目");
        }
    }
    
    private void startQuizForQuestion(Question question) {
        // 跳转到答题界面
        Intent intent = new Intent(this, com.example.aitestbank.ui.question.QuestionActivity.class);
        intent.putExtra("question_id", question.getId());
        intent.putExtra("question_title", question.getTitle());
        intent.putExtra("question_category", question.getCategory());
        
        // 传递题目数据
        intent.putExtra("question_type", question.getType());
        intent.putExtra("question_difficulty", question.getDifficulty());
        intent.putExtra("question_options", new ArrayList<>(question.getOptions()));
        intent.putExtra("question_correct_answer", question.getCorrectAnswer());
        intent.putExtra("question_analysis", question.getAnalysis());
        
        startActivity(intent);
    }
    
    private String getQuestionTypeText(String type) {
        switch (type) {
            case "single_choice": return "单选题";
            case "multiple_choice": return "多选题";
            case "true_false": return "判断题";
            case "fill_blank": return "填空题";
            default: return "单选题";
        }
    }
    
    private String getDifficultyStars(int difficulty) {
        StringBuilder stars = new StringBuilder("★");
        for (int i = 1; i < difficulty && i <= 5; i++) {
            stars.append("★");
        }
        return stars.toString();
    }
}