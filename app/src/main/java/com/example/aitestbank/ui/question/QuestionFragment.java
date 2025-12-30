package com.example.aitestbank.ui.question;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
// import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.service.AIService;
import com.example.aitestbank.supabase.SimpleSupabaseClient;
import com.example.aitestbank.ui.result.ExamResultFragment;
import com.example.aitestbank.utils.OperationCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 刷题Fragment - 在线答题界面
 */
public class QuestionFragment extends Fragment {
    
    private static final String TAG = "QuestionFragment";
    
    // UI组件
    private NestedScrollView questionScrollView;
    private TextView questionNumber;
    private TextView questionDifficulty;
    private TextView questionType;
    private TextView questionContent;
    private LinearLayout optionsContainer;
    // private LinearLayout analysisContainer;
    private TextView analysisContent;
    private com.google.android.material.button.MaterialButton previousButton;
    private com.google.android.material.button.MaterialButton nextButton;
    private com.google.android.material.button.MaterialButton viewAnalysisButton;
    
    // AI解析相关组件
    private com.google.android.material.card.MaterialCardView aiAnalysisContainer;
    private LinearLayout aiAnalysisHeader;
    private LinearLayout aiAnalysisContent;
    private TextView aiAnalysisText;
    // private LinearLayout solutionStepsList;
    // private LinearLayout keyPointsList;
    // private LinearLayout commonMistakesList;
    // private LinearLayout recommendationsList;
    // private LinearLayout solutionStepsContainer;
    // private LinearLayout keyPointsContainer;
    // private LinearLayout commonMistakesContainer;
    // private LinearLayout recommendationsContainer;
    
    // AI服务
    private AIService aiService;
    private boolean isAIExpanded = false;
    
    // 数据和客户端
    private SimpleSupabaseClient supabaseClient;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private boolean isAnswered = false;
    private Set<String> markedQuestions = new HashSet<>();  // 存储已标记的题目ID
    private List<Integer> userAnswers = new ArrayList<>();  // 存储用户答案，-1表示未答题
    private long startTime;  // 答题开始时间
    private long endTime;    // 答题结束时间
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSupabase();
        setupAIService();
        setupClickListeners();
        
        // 检查是否从Intent传递了单个题目数据
        Bundle args = getArguments();
        if (args != null && args.containsKey("question_id")) {
            loadSingleQuestionFromArgs(args);
        } else {
            loadQuestions();
        }
    }
    
    private void initViews(View view) {
        questionScrollView = view.findViewById(R.id.question_scroll_view);
        questionNumber = view.findViewById(R.id.question_number);
        questionDifficulty = view.findViewById(R.id.question_difficulty);
        questionType = view.findViewById(R.id.question_type);
        questionContent = view.findViewById(R.id.question_content);
        optionsContainer = view.findViewById(R.id.options_container);
        // analysisContainer = view.findViewById(R.id.analysis_container);
        analysisContent = view.findViewById(R.id.ai_analysis_text);
        previousButton = view.findViewById(R.id.previous_button);
        nextButton = view.findViewById(R.id.next_button);
        viewAnalysisButton = view.findViewById(R.id.view_analysis_button);

        // AI解析相关组件
        aiAnalysisContainer = view.findViewById(R.id.ai_analysis_container);
        aiAnalysisHeader = view.findViewById(R.id.ai_analysis_header);
        aiAnalysisContent = view.findViewById(R.id.ai_analysis_content);
        aiAnalysisText = view.findViewById(R.id.ai_analysis_text);
        // solutionStepsList = view.findViewById(R.id.solution_steps_list);
        // keyPointsList = view.findViewById(R.id.key_points_list);
        // commonMistakesList = view.findViewById(R.id.common_mistakes_list);
        // recommendationsList = view.findViewById(R.id.recommendations_list);
        // solutionStepsContainer = view.findViewById(R.id.solution_steps_container);
        // keyPointsContainer = view.findViewById(R.id.key_points_container);
        // commonMistakesContainer = view.findViewById(R.id.common_mistakes_container);
        // recommendationsContainer = view.findViewById(R.id.recommendations_container);
    }
    
    private void setupSupabase() {
        supabaseClient = SimpleSupabaseClient.getInstance();
    }
    
    private void setupAIService() {
        aiService = new AIService(getContext());
    }
    
    private void setupClickListeners() {
        // 上一题按钮
        previousButton.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayCurrentQuestion();
            }
        });
        
        // 下一题按钮
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayCurrentQuestion();
            } else {
                // 最后一题，点击交卷
                submitExam();
            }
        });
        
        // 查看解析按钮
        viewAnalysisButton.setOnClickListener(v -> {
            showQuestionAnalysis();
        });
        
        // AI解析折叠展开
        aiAnalysisHeader.setOnClickListener(v -> toggleAIExpansion());
    }
    
    private void toggleAIExpansion() {
        isAIExpanded = !isAIExpanded;
        
        if (isAIExpanded) {
            aiAnalysisContent.setVisibility(View.VISIBLE);
            // 这里可以添加展开动画
        } else {
            aiAnalysisContent.setVisibility(View.GONE);
            // 这里可以添加收起动画
        }
    }
    
    private void loadQuestions() {
        // 记录答题开始时间
        startTime = System.currentTimeMillis();
        
        // 显示加载提示
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "正在加载题目...", Toast.LENGTH_SHORT).show();
        }
        
        // 检查模式
        String mode = getArguments() != null ? getArguments().getString("mode") : null;
        
        if ("random".equals(mode)) {
            // 随机练习模式：加载随机题目
            loadRandomQuestions();
        } else if ("wrong".equals(mode)) {
            // 错题复习模式：加载错题
            loadWrongQuestions();
        } else {
            // 普通模式：从Supabase加载题目数据
            loadQuestionsFromSupabase();
        }
    }
    
    private void loadRandomQuestions() {
        new Thread(() -> {
            try {
                // 随机练习模式：获取随机题目
                String result = supabaseClient.query("questions", "*", "order=random()&limit=10");
                Log.d(TAG, "Random questions from Supabase: " + result);
                
                List<Question> loadedQuestions = parseQuestionsFromSupabase(result);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping UI update");
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        questions.clear();
                        questions.addAll(loadedQuestions);
                        currentQuestionIndex = 0;
                        initializeUserAnswers();
                        displayCurrentQuestion();
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "随机练习已开始，共 " + questions.size() + " 道题目", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 没有数据，显示示例题目
                        loadMockQuestions();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load random questions from Supabase", e);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping error UI update");
                    return;
                }
                
                // 网络错误，使用示例数据
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    loadMockQuestions();
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "随机练习加载失败，使用示例题目", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    private void loadWrongQuestions() {
        new Thread(() -> {
            try {
                // 错题复习模式：获取错题
                String result = supabaseClient.query("wrong_questions", "*", "is_mastered=eq.false&limit=10");
                Log.d(TAG, "Wrong questions from Supabase: " + result);
                
                List<Question> loadedQuestions = parseWrongQuestionsFromSupabase(result);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping UI update");
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        questions.clear();
                        questions.addAll(loadedQuestions);
                        currentQuestionIndex = 0;
                        initializeUserAnswers();
                        displayCurrentQuestion();
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "错题复习已开始，共 " + questions.size() + " 道错题", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "暂无错题，先去刷题吧！", Toast.LENGTH_SHORT).show();
                        }
                        // 返回上一页
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load wrong questions from Supabase", e);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping error UI update");
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "错题加载失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                    // 返回上一页
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                });
            }
        }).start();
    }
    
    private void loadQuestionsFromSupabase() {
        new Thread(() -> {
            try {
                // 查询questions表获取题目数据
                String result = supabaseClient.query("questions", "*", "limit=10");
                Log.d(TAG, "Questions from Supabase: " + result);
                
                List<Question> loadedQuestions = parseQuestionsFromSupabase(result);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping UI update");
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        questions.clear();
                        questions.addAll(loadedQuestions);
                        currentQuestionIndex = 0;
                        initializeUserAnswers();
                        displayCurrentQuestion();
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "成功加载 " + questions.size() + " 道题目", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 没有数据，显示示例题目
                        loadMockQuestions();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load questions from Supabase", e);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping error UI update");
                    return;
                }
                
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "网络错误，显示示例题目", Toast.LENGTH_SHORT).show();
                    }
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
                
                // 解析选项（假设options是JSON数组字符串）
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
    
    private List<Question> parseWrongQuestionsFromSupabase(String jsonResult) {
        try {
            List<Question> questionList = new ArrayList<>();
            
            JSONArray jsonArray = new JSONArray(jsonResult);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                // 从错题表中获取题目数据
                Question question = new Question();
                question.setId(obj.optString("question_id", ""));
                question.setTitle(obj.optString("question_title", "错题"));
                question.setCategory(obj.optString("category", "错题复习"));
                question.setDifficulty(obj.optInt("difficulty", 3));
                question.setType(obj.optString("type", "single_choice"));
                question.setAnalysis(obj.optString("wrong_reason", "需要重点复习"));
                
                // 解析选项（假设options是JSON数组字符串）
                String optionsStr = obj.optString("options", "[]");
                List<String> options = parseOptions(optionsStr);
                question.setOptions(options);
                
                // 设置正确答案（对于错题，可能需要从原题获取，这里简化处理）
                question.setCorrectAnswer(0);
                
                questionList.add(question);
            }
            
            return questionList;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse wrong questions JSON", e);
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
    
    private void loadSingleQuestionFromArgs(Bundle args) {
        // 从传递的参数创建单个题目
        Question question = new Question();
        
        question.setId(args.getString("question_id", ""));
        question.setTitle(args.getString("question_title", "题目"));
        question.setCategory(args.getString("question_category", "未分类"));
        question.setType(args.getString("question_type", "single_choice"));
        question.setDifficulty(args.getInt("question_difficulty", 3));
        question.setCorrectAnswer(args.getInt("question_correct_answer", 0));
        question.setAnalysis(args.getString("question_analysis", "暂无解析"));
        
        // 获取选项列表
        ArrayList<String> options = args.getStringArrayList("question_options");
        if (options != null && !options.isEmpty()) {
            question.setOptions(options);
        } else {
            // 如果没有选项，设置默认选项
            List<String> defaultOptions = new ArrayList<>();
            defaultOptions.add("选项A");
            defaultOptions.add("选项B");
            defaultOptions.add("选项C");
            defaultOptions.add("选项D");
            question.setOptions(defaultOptions);
        }
        
        // 添加到题目列表
        questions.clear();
        questions.add(question);
        currentQuestionIndex = 0;
        initializeUserAnswers();
        
        // 隐藏上一题按钮，修改下一题按钮为"提交答案"
        if (previousButton != null) {
            previousButton.setVisibility(View.GONE);
        }
        if (nextButton != null) {
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setText("提交答案");
            nextButton.setOnClickListener(v -> submitSingleAnswer());
        }
        
        // 更新题目显示
        displayCurrentQuestion();
        
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "开始答题", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadMockQuestions() {
        // 模拟数据作为fallback
        questions.clear();
        
        // 创建示例题目
        Question q1 = new Question();
        q1.setId("1");
        q1.setTitle("下列哪个是Java的基本数据类型？");
        q1.setType("single_choice");
        q1.setDifficulty(2);
        q1.setCategory("Java基础");
        q1.setCorrectAnswer(0);
        List<String> options1 = new ArrayList<>();
        options1.add("String");
        options1.add("Integer");
        options1.add("int");
        options1.add("ArrayList");
        q1.setOptions(options1);
        q1.setAnalysis("Java的基本数据类型包括：byte、short、int、long、float、double、char、boolean。String和Integer是引用类型，ArrayList是集合类。");
        
        Question q2 = new Question();
        q2.setId("2");
        q2.setTitle("Android中常用的布局有哪些？");
        q2.setType("multiple_choice");
        q2.setDifficulty(3);
        q2.setCategory("Android开发");
        q2.setCorrectAnswer(-1); // 多选题不设置单一正确答案
        List<String> options2 = new ArrayList<>();
        options2.add("LinearLayout");
        options2.add("RelativeLayout");
        options2.add("ConstraintLayout");
        options2.add("FrameLayout");
        q2.setOptions(options2);
        q2.setAnalysis("Android中常用的布局包括：LinearLayout（线性布局）、RelativeLayout（相对布局）、ConstraintLayout（约束布局）、FrameLayout（帧布局）等。");
        
        questions.add(q1);
        questions.add(q2);
        
        currentQuestionIndex = 0;
        initializeUserAnswers();
        displayCurrentQuestion();
    }
    
    private void displayCurrentQuestion() {
        if (questions.isEmpty()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "暂无题目", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // 更新题目信息
        questionNumber.setText("第 " + (currentQuestionIndex + 1) + " 题");
        questionDifficulty.setText("难度: " + getDifficultyStars(currentQuestion.getDifficulty()));
        questionType.setText(getQuestionTypeText(currentQuestion.getType()));
        questionContent.setText(currentQuestion.getTitle());
        
        // 清空之前的选择
        selectedAnswer = "";
        isAnswered = false;
        
        // 生成选项
        generateOptions(currentQuestion);

        // 隐藏解析
        aiAnalysisContainer.setVisibility(View.GONE);
        
        // 检查当前题目是否已答题但未查看解析
        boolean isAnsweredButNotViewed = checkIfAnsweredButNotViewed(currentQuestionIndex);
        if (viewAnalysisButton != null) {
            if (isAnsweredButNotViewed) {
                viewAnalysisButton.setVisibility(View.VISIBLE);
            } else {
                viewAnalysisButton.setVisibility(View.GONE);
            }
        }

        // 更新按钮状态
        updateButtonStates();
        
        // 更新进度信息
        updateProgressInfo();
        
        // 滚动到顶部
        questionScrollView.scrollTo(0, 0);
    }
    
    /**
     * 检查当前题目是否已答题但未查看解析
     */
    private boolean checkIfAnsweredButNotViewed(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= userAnswers.size()) {
            return false;
        }
        
        Integer userAnswer = userAnswers.get(questionIndex);
        // 已答题（userAnswer != -1）且AI解析未显示
        return userAnswer != null && userAnswer != -1 && aiAnalysisContainer.getVisibility() != View.VISIBLE;
    }
    

    
    private void generateOptions(Question question) {
        optionsContainer.removeAllViews();
        
        List<String> options = question.getOptions();
        if (options == null || options.isEmpty()) {
            return;
        }
        
        char optionChar = 'A';
        for (int i = 0; i < options.size(); i++) {
            View optionView = createOptionView(optionChar, options.get(i), i);
            optionsContainer.addView(optionView);
            optionChar++;
        }
    }
    
    private View createOptionView(char optionChar, String optionText, int index) {
        // 创建选项布局
        LinearLayout optionLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        optionLayout.setLayoutParams(params);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setPadding(20, 20, 20, 20);
        optionLayout.setBackgroundResource(R.drawable.bg_option_default);
        optionLayout.setElevation(2f);
        
        // 选项字母
        TextView letterView = new TextView(getContext());
        letterView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        letterView.setText(String.valueOf(optionChar) + ".");
        letterView.setTextColor(getResources().getColor(R.color.text_primary));
        letterView.setTextSize(18);
        letterView.setTypeface(null, android.graphics.Typeface.BOLD);
        letterView.setPadding(0, 0, 16, 0);
        
        // 选项文本
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        ));
        textView.setText(optionText);
        textView.setTextColor(getResources().getColor(R.color.text_primary));
        textView.setTextSize(18);
        textView.setLineSpacing(4, 1.0f);
        
        optionLayout.addView(letterView);
        optionLayout.addView(textView);
        
        // 设置点击事件
        optionLayout.setOnClickListener(v -> selectOption(String.valueOf(index), optionLayout));
        
        return optionLayout;
    }
    
    private void selectOption(String answer, View optionView) {
        if (isAnswered) {
            return; // 已答题不允许更改
        }
        
        selectedAnswer = answer;
        
        // 记录用户答案，添加安全检查
        if (currentQuestionIndex >= 0 && currentQuestionIndex < userAnswers.size()) {
            try {
                userAnswers.set(currentQuestionIndex, Integer.parseInt(answer));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid answer format: " + answer, e);
                return;
            }
        } else {
            Log.e(TAG, "Invalid question index: " + currentQuestionIndex + ", userAnswers size: " + userAnswers.size());
            return;
        }
        
        // 重置所有选项背景
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);
            child.setBackgroundResource(R.drawable.bg_option_default);
            
            // 重置文字颜色
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                if (layout.getChildCount() >= 2) {
                    TextView letterView = (TextView) layout.getChildAt(0);
                    TextView textView = (TextView) layout.getChildAt(1);
                    letterView.setTextColor(getResources().getColor(R.color.text_primary));
                    textView.setTextColor(getResources().getColor(R.color.text_primary));
                }
            }
        }
        
        // 高亮选中的选项
        optionView.setBackgroundResource(R.drawable.bg_option_selected_blue);
        
        // 设置选中文字颜色为蓝色
        if (optionView instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) optionView;
            if (layout.getChildCount() >= 2) {
                TextView letterView = (TextView) layout.getChildAt(0);
                TextView textView = (TextView) layout.getChildAt(1);
                letterView.setTextColor(getResources().getColor(R.color.primary_blue));
                textView.setTextColor(getResources().getColor(R.color.primary_blue));
            }
        }
    }
    

    

    

    
    private String getDifficultyStars(int difficulty) {
        StringBuilder stars = new StringBuilder("★");
        for (int i = 1; i < difficulty && i <= 5; i++) {
            stars.append("★");
        }
        return stars.toString();
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
    

    

    

    /**
     * 显示AI解析进度
     */
    private void showAIProgress(boolean show) {
        if (show && isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "AI正在生成智能解析...", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 提交单个题目的答案
     */
    private void submitSingleAnswer() {
        try {
            // 检查是否已选择答案
            if (selectedAnswer == null || selectedAnswer.isEmpty()) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "请先选择答案", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            // 记录答案
            if (currentQuestionIndex >= 0 && currentQuestionIndex < userAnswers.size()) {
                try {
                    userAnswers.set(currentQuestionIndex, Integer.parseInt(selectedAnswer));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid answer format: " + selectedAnswer, e);
                    return;
                }
            }
            
            // 标记为已答题
            isAnswered = true;
            
            // 立即显示答案对错结果
            showSingleQuestionAnswer();
            
            // 更新下一题按钮状态
            updateButtonStates();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to submit single answer", e);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "提交失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 显示单个题目的答案结果
     */
    private void showSingleQuestionAnswer() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // 获取用户答案索引
        Integer userAnswerIndex = null;
        if (currentQuestionIndex >= 0 && currentQuestionIndex < userAnswers.size()) {
            userAnswerIndex = userAnswers.get(currentQuestionIndex);
            if (userAnswerIndex == -1) userAnswerIndex = null;
        }
        
        // 更新所有选项的背景，显示正确答案和用户答案
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);
            
            if (currentQuestion.getCorrectAnswer() != null && i == currentQuestion.getCorrectAnswer()) {
                // 正确答案 - 绿色背景
                child.setBackgroundResource(R.drawable.bg_option_correct);
            } else if (userAnswerIndex != null && i == userAnswerIndex) {
                // 用户选择的错误答案 - 红色背景
                child.setBackgroundResource(R.drawable.bg_option_wrong);
            } else {
                // 其他选项 - 保持原样或灰色
                child.setBackgroundResource(R.drawable.bg_option_default);
            }
            
            // 禁用点击事件
            child.setClickable(false);
        }
        
        // 计算是否正确
        boolean isCorrect = false;
        if (userAnswerIndex != null && currentQuestion.getCorrectAnswer() != null) {
            isCorrect = userAnswerIndex.equals(currentQuestion.getCorrectAnswer());
            
            // 立即显示答案对错的Toast提示
            showAnswerResultToast(isCorrect);
        }
        
        // 更新按钮状态，显示查看解析按钮
        updateButtonStates();
        
        // 保存答题记录到数据库
        saveAnswerRecord(currentQuestion, userAnswerIndex, isCorrect);
        
        // 如果答错了，自动保存错题记录
        if (!isCorrect) {
            saveWrongQuestionAfterAnswer(currentQuestion, userAnswerIndex);
        }
        
        // 不立即显示详细解析，让用户选择是否查看
        // showQuestionAnalysis();
    }
    
    /**
     * 保存答题记录到数据库
     */
    private void saveAnswerRecord(Question question, Integer userAnswer, boolean isCorrect) {
        if (question == null || supabaseClient == null) {
            return;
        }
        
        try {
            // 计算答题时间（毫秒）
            long answerTime = System.currentTimeMillis() - startTime;
            
            // 生成会话ID（一次练习同一个会话）
            String sessionId = String.valueOf(startTime);
            
            // 调用保存方法
            supabaseClient.saveAnswerRecord(
                question.getId(),
                userAnswer,
                isCorrect,
                (int) answerTime,
                sessionId,
                new OperationCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, "答题记录保存成功: " + result);
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "答题记录保存失败", error);
                    }
                }
            );
            
        } catch (Exception e) {
            Log.e(TAG, "保存答题记录时出错", e);
        }
    }
    
    /**
     * 显示答案结果Toast提示
     */
    private void showAnswerResultToast(boolean isCorrect) {
        if (!isAdded() || getContext() == null) return;
        
        String message = isCorrect ? "✅ 回答正确！" : "❌ 回答错误";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 答错后自动保存错题记录
     */
    private void saveWrongQuestionAfterAnswer(Question question, Integer userAnswer) {
        try {
            // 构建错题数据
            org.json.JSONObject wrongQuestionData = new org.json.JSONObject();
            long currentTime = System.currentTimeMillis();
            
            wrongQuestionData.put("id", java.util.UUID.randomUUID().toString());
            
            // 获取当前用户ID
            com.example.aitestbank.supabase.auth.AuthManager authManager = 
                com.example.aitestbank.supabase.auth.AuthManager.getInstance(requireContext());
            String currentUserId = authManager.getCurrentUserId();
            wrongQuestionData.put("user_id", currentUserId);
            
            wrongQuestionData.put("question_id", question.getId());
            wrongQuestionData.put("question_title", question.getTitle());
            wrongQuestionData.put("user_answer", userAnswer);
            wrongQuestionData.put("correct_answer", question.getCorrectAnswer());
            wrongQuestionData.put("category", question.getCategory());
            wrongQuestionData.put("difficulty", question.getDifficulty());
            wrongQuestionData.put("type", question.getType());
            
            List<String> options = question.getOptions();
            if (options != null && !options.isEmpty()) {
                wrongQuestionData.put("options", new org.json.JSONArray(options));
            }
            
            wrongQuestionData.put("analysis", question.getAnalysis());
            wrongQuestionData.put("review_count", 1);
            wrongQuestionData.put("mastery_level", 1);
            wrongQuestionData.put("is_mastered", false);
            
            // 设置复习日期
            String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(currentTime));
            wrongQuestionData.put("last_review_date", currentDate);
            wrongQuestionData.put("next_review_date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(currentTime + 24 * 60 * 60 * 1000)));
            
            // 设置创建时间
            wrongQuestionData.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new java.util.Date(currentTime)));
            
            // 调用保存方法
            saveWrongQuestionToSupabase(wrongQuestionData);
            
        } catch (Exception e) {
            Log.e(TAG, "自动保存错题失败", e);
        }
    }
    
    /**
     * 显示题目解析
     */
    private void showQuestionAnalysis() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // 显示解析容器
        aiAnalysisContainer.setVisibility(View.VISIBLE);
        
        // 隐藏查看解析按钮
        if (viewAnalysisButton != null) {
            viewAnalysisButton.setVisibility(View.GONE);
        }
        
        // 获取用户答案索引
        Integer userAnswerIndex = null;
        if (currentQuestionIndex >= 0 && currentQuestionIndex < userAnswers.size()) {
            userAnswerIndex = userAnswers.get(currentQuestionIndex);
            if (userAnswerIndex == -1) userAnswerIndex = null;
        }
        
        // 生成AI分析
        AIService.AIAnalysisResult analysisResult = generateAIAnalysisResult(currentQuestion, userAnswerIndex);
        
        // 显示分析结果
        displayAIAnalysis(analysisResult);
    }
    
    /**
     * 生成AI分析结果
     */
    private AIService.AIAnalysisResult generateAIAnalysisResult(Question question, Integer userAnswer) {
        String analysisText = generateAIAnalysis(question, userAnswer);
        
        AIService.AIAnalysisResult result = new AIService.AIAnalysisResult();
        result.setAnalysisText(analysisText);
        
        return result;
    }
    
    /**
     * 显示AI分析结果
     */
    private void displayAIAnalysis(AIService.AIAnalysisResult analysisResult) {
        if (analysisResult != null && analysisResult.getAnalysisText() != null) {
            aiAnalysisText.setText(analysisResult.getAnalysisText());
        }
    }
    
    /**
     * 在显示新题目时重置AI解析状态
     */
    private void resetAIAnalysis() {
        aiAnalysisContainer.setVisibility(View.GONE);
        isAIExpanded = false;
        aiAnalysisContent.setVisibility(View.GONE);
    }
    
    /**
     * 生成专业的AI解析
     */
    private String generateAIAnalysis(Question question, Integer userAnswer) {
        StringBuilder analysis = new StringBuilder();
        
        // 题目信息概览
        analysis.append("🔍 **题目分析**\n\n");
        analysis.append("**题型**：").append(getQuestionTypeDescription(question.getType())).append("\n");
        analysis.append("**难度**：").append(getDifficultyDescription(question.getDifficulty())).append("\n");
        if (question.getCategory() != null) {
            analysis.append("**知识点**：").append(question.getCategory()).append("\n");
        }
        analysis.append("\n");
        
        // 核心解题思路
        analysis.append("💡 **解题思路**\n");
        analysis.append("1. **审题理解**：明确题目要求，抓住关键词和限制条件\n");
        analysis.append("2. **选项分析**：逐个分析每个选项的正确性和合理性\n");
        analysis.append("3. **排除干扰**：识别并排除明显错误的干扰项\n");
        analysis.append("4. **验证选择**：确保所选答案符合题目所有要求\n");
        analysis.append("\n");
        
        // 知识点详细解析
        if (question.getCategory() != null) {
            analysis.append("📚 **知识点详解**\n");
            analysis.append(generateKnowledgePointAnalysis(question.getCategory())).append("\n\n");
        }
        
        // 答案对比分析
        if (userAnswer != null && question.getCorrectAnswer() != null) {
            List<String> options = question.getOptions();
            if (options != null && userAnswer < options.size() && question.getCorrectAnswer() < options.size()) {
                String userAnswerText = options.get(userAnswer);
                String correctAnswerText = options.get(question.getCorrectAnswer());
                
                analysis.append("✅ **答题结果**\n");
                if (userAnswer.equals(question.getCorrectAnswer())) {
                    analysis.append("您选择了：**").append(userAnswerText).append("**\n");
                    analysis.append("**回答正确！**\n\n");
                    analysis.append("🌟 **正确原因分析**\n");
                    analysis.append("- 准确理解了题目要求\n");
                    analysis.append("- 正确识别了关键知识点\n");
                    analysis.append("- 成功排除了干扰选项\n");
                } else {
                    analysis.append("您的选择：**").append(userAnswerText).append("**\n");
                    analysis.append("正确答案：**").append(correctAnswerText).append("**\n\n");
                    analysis.append("📝 **错因深度分析**\n");
                    analysis.append(generateErrorAnalysis(question, userAnswer)).append("\n\n");
                    analysis.append("💡 **改进建议**\n");
                    analysis.append("- 加强对相关概念的理解\n");
                    analysis.append("- 练习类似题型的解题方法\n");
                    analysis.append("- 注意审题细节，避免粗心错误\n");
                }
            }
        }
        
        // 扩展学习建议
        analysis.append("\n🎯 **学习建议**\n");
        analysis.append("- 建议复习相关概念和原理\n");
        analysis.append("- 练习类似题型巩固知识\n");
        analysis.append("- 总结解题方法和技巧\n");
        
        return analysis.toString();
    }
    
    /**
     * 生成知识点详细解析
     */
    private String generateKnowledgePointAnalysis(String category) {
        switch (category) {
            case "Java基础":
                return "Java基础包括数据类型、运算符、控制语句等核心概念，是编程的基石。需要理解每种数据类型的特性和使用场景。";
            case "面向对象":
                return "面向对象编程的核心是封装、继承、多态。需要掌握类与对象的关系，理解抽象和接口的设计思想。";
            case "集合框架":
                return "Java集合框架包括List、Set、Map等数据结构，需要了解每种集合的特点、适用场景和性能差异。";
            case "多线程":
                return "多线程涉及线程创建、同步、通信等概念，需要理解线程安全、锁机制和并发编程的最佳实践。";
            case "异常处理":
                return "异常处理机制包括try-catch-finally、throw和throws，需要掌握异常分类、处理原则和最佳实践。";
            default:
                return "该知识点涉及编程基础概念，建议系统学习相关理论知识，并结合实践加深理解。";
        }
    }
    
    /**
     * 生成错误分析
     */
    private String generateErrorAnalysis(Question question, Integer userAnswer) {
        List<String> options = question.getOptions();
        if (options == null || userAnswer >= options.size()) return "";
        
        String userAnswerText = options.get(userAnswer);
        
        // 根据题目类型和用户选择生成针对性的错误分析
        if (question.getType() != null) {
            switch (question.getType()) {
                case "单选题":
                    return "可能原因：1) 对概念理解不够准确；2) 被干扰项迷惑；3) 审题不仔细；4) 知识点掌握不牢固。";
                case "多选题":
                    return "可能原因：1) 漏选了正确选项；2) 多选了错误选项；3) 对选项间关系理解不清；4) 知识点覆盖不全面。";
                case "判断题":
                    return "可能原因：1) 对概念理解有偏差；2) 忽略了关键细节；3) 混淆了相似概念；4) 判断依据不明确。";
                default:
                    return "可能对相关知识点理解不够深入，建议加强基础知识学习和题目练习。";
            }
        }
        
        return "可能对相关知识点理解不够深入，建议加强基础知识学习和题目练习。";
    }
    
    /**
     * 更新按钮状态 - 根据当前题目位置和答题状态显示不同按钮
     */
    private void updateButtonStates() {
        if (previousButton == null || nextButton == null || viewAnalysisButton == null) {
            return;
        }
        
        // 上一题按钮状态
        if (currentQuestionIndex == 0) {
            previousButton.setEnabled(false);
            previousButton.setAlpha(0.5f);
            previousButton.setText("上一题");
        } else {
            previousButton.setEnabled(true);
            previousButton.setAlpha(1.0f);
            previousButton.setText("上一题");
        }
        
        // 检查当前题目是否已答题
        boolean isCurrentQuestionAnswered = currentQuestionIndex >= 0 && 
                                           currentQuestionIndex < userAnswers.size() && 
                                           userAnswers.get(currentQuestionIndex) != -1;
        
        // 下一题按钮状态
        if (currentQuestionIndex == questions.size() - 1) {
            // 最后一题，显示"交卷"
            nextButton.setText("交卷");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                nextButton.setBackgroundTintList(getResources().getColorStateList(R.color.success_green, getContext().getTheme()));
            } else {
                nextButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.success_green)
                ));
            }
        } else {
            // 不是最后一题
            if (isCurrentQuestionAnswered) {
                // 已答题，显示"下一题"
                nextButton.setText("下一题");
                nextButton.setEnabled(true);
            } else {
                // 未答题，显示"提交答案"
                nextButton.setText("提交答案");
                nextButton.setEnabled(true);
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                nextButton.setBackgroundTintList(getResources().getColorStateList(R.color.primary_blue, getContext().getTheme()));
            } else {
                nextButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.primary_blue)
                ));
            }
        }
        
        // 查看解析按钮状态
        if (isCurrentQuestionAnswered && aiAnalysisContainer.getVisibility() != View.VISIBLE) {
            viewAnalysisButton.setVisibility(View.VISIBLE);
        } else {
            viewAnalysisButton.setVisibility(View.GONE);
        }
    }
    
    /**
     * 初始化用户答案列表
     */
    private void initializeUserAnswers() {
        userAnswers.clear();
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                userAnswers.add(-1); // -1表示未答题
            }
        }
    }
    
    /**
     * 交卷处理
     */
    private void submitExam() {
        try {
            // 记录答题结束时间
            endTime = System.currentTimeMillis();
            
            // 记录当前题目的答案
            if (selectedAnswer != null && !selectedAnswer.isEmpty()) {
                try {
                    userAnswers.set(currentQuestionIndex, Integer.parseInt(selectedAnswer));
                } catch (NumberFormatException e) {
                    userAnswers.set(currentQuestionIndex, -1); // 设置为未答
                }
            }
            
            // 安全检查
            if (questions == null) {
                questions = new ArrayList<>();
            }
            if (userAnswers == null) {
                userAnswers = new ArrayList<>();
            }
            
            // 确保数据长度一致
            while (userAnswers.size() < questions.size()) {
                userAnswers.add(-1); // 未答题
            }
            
            // 保存答题结果到数据库
            saveExamResultsToDatabase();
            
            // 显示结果页面
            if (getActivity() != null && isAdded()) {
                ExamResultFragment resultFragment = new ExamResultFragment();
                
                // 传递数据给结果页面（使用Serializable）
                Bundle args = new Bundle();
                args.putSerializable("questions", new ArrayList<>(questions));
                args.putIntegerArrayList("user_answers", new ArrayList<>(userAnswers));
                args.putLong("start_time", startTime);
                args.putLong("end_time", endTime);
                resultFragment.setArguments(args);
                
                // 跳转到结果页面
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, resultFragment)
                    .addToBackStack(null)
                    .commit();
            }
            
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "交卷成功！", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "交卷失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 保存答题结果到数据库
     */
    private void saveExamResultsToDatabase() {
        if (questions == null || userAnswers == null || supabaseClient == null) {
            return;
        }
        
        // 统计答题结果
        int totalQuestions = questions.size();
        int correctCount = 0;
        int wrongCount = 0;
        
        // 计算正确率并保存每道题的答题记录
        for (int i = 0; i < totalQuestions; i++) {
            if (i < userAnswers.size() && userAnswers.get(i) != -1) {
                Question question = questions.get(i);
                Integer userAnswer = userAnswers.get(i);
                boolean isCorrect = false;
                
                if (question.getCorrectAnswer() != null && userAnswer.equals(question.getCorrectAnswer())) {
                    correctCount++;
                    isCorrect = true;
                } else {
                    wrongCount++;
                }
                
                // 保存单题答题记录
                try {
                    long answerTime = endTime - startTime;
                    String sessionId = String.valueOf(startTime);
                    
                    supabaseClient.saveAnswerRecord(
                        question.getId(),
                        userAnswer,
                        isCorrect,
                        (int) (answerTime / totalQuestions), // 平均答题时间
                        sessionId,
                        new OperationCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                Log.d(TAG, "答题记录保存成功");
                            }
                            
                            @Override
                            public void onError(Exception error) {
                                Log.e(TAG, "答题记录保存失败", error);
                            }
                        }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "保存答题记录时出错", e);
                }
            }
        }
        
        // 计算正确率
        double accuracyRate = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;
        
        // 保存用户统计数据（包括学习天数）
        saveUserStatistics(totalQuestions, correctCount, accuracyRate);
        
        // 保存错题记录
        saveWrongQuestions();
    }
    
    /**
     * 保存用户统计数据
     */
    private void saveUserStatistics(int totalQuestions, int correctCount, double accuracyRate) {
        if (supabaseClient == null) {
            Log.w(TAG, "Supabase客户端为空，无法保存统计数据");
            return;
        }
        
        // 调用Supabase API更新用户统计数据
        supabaseClient.updateUserStatistics(totalQuestions, correctCount, accuracyRate, new OperationCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "用户统计数据保存成功: " + result);
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "保存用户统计数据失败", error);
            }
        });
    }
    
    /**
     * 保存错题记录
     */
    private void saveWrongQuestions() {
        Log.d(TAG, "开始保存错题记录...");
        
        if (questions == null || userAnswers == null) {
            Log.w(TAG, "questions或userAnswers为空，无法保存错题");
            return;
        }
        
        if (supabaseClient == null) {
            Log.e(TAG, "Supabase客户端为空，无法保存错题");
            return;
        }
        
        int wrongCount = 0;
        
        for (int i = 0; i < questions.size(); i++) {
            if (i < userAnswers.size() && userAnswers.get(i) != -1) {
                Question question = questions.get(i);
                Integer userAnswer = userAnswers.get(i);
                
                // 如果是错题，保存到错题本
                if (question.getCorrectAnswer() != null && !userAnswer.equals(question.getCorrectAnswer())) {
                    Log.d(TAG, "发现错题: 题目ID=" + question.getId() + 
                          ", 用户答案=" + userAnswer + ", 正确答案=" + question.getCorrectAnswer());
                    saveWrongQuestionToDatabase(question, userAnswer);
                    wrongCount++;
                }
            }
        }
        
        Log.d(TAG, "错题保存完成，共发现 " + wrongCount + " 道错题");
    }
    
    /**
     * 保存错题到数据库
     */
    private void saveWrongQuestionToDatabase(Question question, Integer userAnswer) {
        // 获取当前用户ID
        com.example.aitestbank.supabase.auth.AuthManager authManager = 
            com.example.aitestbank.supabase.auth.AuthManager.getInstance(requireContext());
        String currentUserId = authManager.getCurrentUserId();
        
        // 调用Supabase API保存错题记录
        Log.d(TAG, "保存错题记录: 题目ID=" + question.getId() + 
              ", 用户答案=" + userAnswer + ", 正确答案=" + question.getCorrectAnswer() + 
              ", 用户ID=" + currentUserId);
        
        // 构建错题数据
        org.json.JSONObject wrongQuestionData = new org.json.JSONObject();
        try {
            // 生成唯一ID
            wrongQuestionData.put("id", java.util.UUID.randomUUID().toString());
            wrongQuestionData.put("user_id", currentUserId); // 使用真实用户ID
            wrongQuestionData.put("question_id", question.getId());
            wrongQuestionData.put("question_title", question.getTitle());
            wrongQuestionData.put("user_answer", userAnswer);
            wrongQuestionData.put("correct_answer", question.getCorrectAnswer());
            wrongQuestionData.put("category", question.getCategory());
            wrongQuestionData.put("difficulty", question.getDifficulty());
            wrongQuestionData.put("type", question.getType());
            
            // 解析选项为JSON字符串
            List<String> options = question.getOptions();
            if (options != null && !options.isEmpty()) {
                wrongQuestionData.put("options", new org.json.JSONArray(options));
            }
            
            wrongQuestionData.put("analysis", question.getAnalysis());
            wrongQuestionData.put("review_count", 1); // 初始复习次数为1
            wrongQuestionData.put("mastery_level", 1); // 初始掌握等级为1
            wrongQuestionData.put("is_mastered", false); // 初始未掌握
            
            // 设置日期字段（数据库是date类型，需要转换为日期格式）
            long currentTime = System.currentTimeMillis();
            wrongQuestionData.put("last_review_date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(currentTime)));
            wrongQuestionData.put("next_review_date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(currentTime + 24 * 60 * 60 * 1000))); // 明天复习
            
            // 时间戳字段需要转换为ISO格式字符串
            wrongQuestionData.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new java.util.Date(currentTime)));
            
            // 调试：打印构建的错题数据
            Log.d(TAG, "构建的错题数据: " + wrongQuestionData.toString());
            
            // 调用Supabase API插入错题记录
            saveWrongQuestionToSupabase(wrongQuestionData);
            
        } catch (Exception e) {
            Log.e(TAG, "构建错题数据失败", e);
        }
    }
    
    /**
     * 调用Supabase API保存错题记录
     */
    private void saveWrongQuestionToSupabase(org.json.JSONObject wrongQuestionData) {
        new Thread(() -> {
            try {
                // 获取当前用户ID
                com.example.aitestbank.supabase.auth.AuthManager authManager = 
                    com.example.aitestbank.supabase.auth.AuthManager.getInstance(requireContext());
                String userId = authManager.getCurrentUserId();
                
                // 如果用户ID为空，说明用户未登录，不保存错题
                if (userId == null || userId.isEmpty()) {
                    Log.e(TAG, "用户未登录，无法保存错题");
                    return;
                }
                
                // 先检查是否已存在相同题目的错题记录
                String existingQuery = supabaseClient.query("wrong_questions", "*", 
                    "question_id=eq." + wrongQuestionData.getString("question_id") + "&user_id=eq." + userId + "&limit=1");
                
                if (existingQuery != null && existingQuery.length() > 2 && !existingQuery.equals("[]")) {
                    // 已存在记录，更新复习次数和最后复习时间
                    org.json.JSONObject updateData = new org.json.JSONObject();
                    updateData.put("review_count", wrongQuestionData.getInt("review_count") + 1);
                    
                    // 使用正确的日期格式
                    String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis()));
                    updateData.put("last_review_date", currentDate);
                    updateData.put("is_mastered", false); // 重新标记为未掌握
                    
                    // 修复：更新时同时指定question_id和user_id
                    String filter = "question_id=eq." + wrongQuestionData.getString("question_id") + "&user_id=eq." + userId;
                    String result = supabaseClient.update("wrong_questions", updateData, filter);
                    Log.d(TAG, "更新错题记录成功: " + result);
                } else {
                    // 新错题记录，插入到数据库
                    String result = supabaseClient.insert("wrong_questions", wrongQuestionData);
                    Log.d(TAG, "插入错题记录成功: " + result);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "保存错题到Supabase失败", e);
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 更新进度信息
     */
    private void updateProgressInfo() {
        if (!isAdded() || getActivity() == null || questions == null) return;
        
        int answeredCount = 0;
        int totalCount = questions.size();
        
        // 统计已答题数
        for (Integer answer : userAnswers) {
            if (answer != null && answer >= 0) {
                answeredCount++;
            }
        }
        
        // 更新进度显示
        TextView progressPercent = getActivity().findViewById(R.id.progress_percent);
        TextView questionProgressText = getActivity().findViewById(R.id.question_progress_text);
        ProgressBar progressBar = getActivity().findViewById(R.id.progress_bar);
        
        if (progressPercent != null) {
            int percent = totalCount > 0 ? (answeredCount * 100 / totalCount) : 0;
            progressPercent.setText(percent + "%");
        }
        
        if (questionProgressText != null) {
            questionProgressText.setText("第" + (currentQuestionIndex + 1) + "题/共" + totalCount + "题");
        }
        
        if (progressBar != null) {
            int progress = totalCount > 0 ? (answeredCount * 100 / totalCount) : 0;
            progressBar.setProgress(progress);
        }
    }
    
    /**
     * 获取题目类型描述
     */
    private String getQuestionTypeDescription(String type) {
        if ("single_choice".equals(type)) return "单选题";
        if ("multiple_choice".equals(type)) return "多选题";
        if ("true_false".equals(type)) return "判断题";
        if ("fill_blank".equals(type)) return "填空题";
        return "单选题";
    }
    
    /**
     * 获取难度描述
     */
    private String getDifficultyDescription(Integer difficulty) {
        if (difficulty == null) return "中等";
        if (difficulty <= 2) return "简单";
        if (difficulty <= 3) return "中等";
        if (difficulty <= 4) return "困难";
        return "专家级";
    }
    
    /**
     * 获取设备ID
     */
    private String getDeviceId() {
        try {
            // 使用Android ID作为设备标识
            android.content.Context context = getActivity();
            if (context != null) {
                String androidId = android.provider.Settings.Secure.getString(
                    context.getContentResolver(), 
                    android.provider.Settings.Secure.ANDROID_ID
                );
                if (androidId != null && !androidId.isEmpty()) {
                    return androidId;
                }
            }
            
            // 如果无法获取Android ID，使用随机UUID
            return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } catch (Exception e) {
            Log.e(TAG, "获取设备ID失败", e);
            return "default_device";
        }
    }
    
    // 用户配置文件创建状态缓存
    private static java.util.Map<String, Boolean> userProfileCreationStatus = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 同步确保用户配置文件存在（避免外键约束错误）
     */
    private boolean ensureUserProfileExistsSync(String userId, String deviceId) {
        // 使用同步块确保线程安全
        synchronized (userProfileCreationStatus) {
            // 检查是否正在创建或已创建
            if (userProfileCreationStatus.containsKey(userId)) {
                // 用户配置文件状态已存在，直接返回结果
                return userProfileCreationStatus.get(userId);
            }
            
            // 标记为正在创建（使用false表示正在创建中）
            userProfileCreationStatus.put(userId, false);
        }
        
        boolean userExists = false;
        boolean creationSuccess = false;
        
        try {
            // 检查用户配置文件是否已存在，增加超时处理
            String existingUser = supabaseClient.query("user_profiles", "id", 
                "id=eq." + userId + "&limit=1");
            
            if (existingUser != null && existingUser.length() > 2 && !existingUser.equals("[]")) {
                // 用户配置文件已存在
                Log.d(TAG, "用户配置文件已存在: " + userId);
                userExists = true;
                creationSuccess = true;
            }
        } catch (Exception e) {
            // 查询失败，可能是网络问题或表不存在，尝试直接创建用户配置文件
            Log.w(TAG, "查询用户配置文件失败，尝试创建: " + e.getMessage());
            userExists = false; // 假设用户不存在，尝试创建
        }
        
        if (!userExists) {
            try {
                // 用户配置文件不存在，创建新的
                org.json.JSONObject userProfile = new org.json.JSONObject();
                userProfile.put("id", userId);
                userProfile.put("device_id", deviceId);
                userProfile.put("username", "用户_" + deviceId.substring(0, 8));
                userProfile.put("display_name", "用户_" + deviceId.substring(0, 8));
                userProfile.put("daily_goal", 20);
                userProfile.put("total_questions", 0);
                userProfile.put("correct_questions", 0);
                userProfile.put("study_days", 0);
                
                // 设置创建时间
                long currentTime = System.currentTimeMillis();
                userProfile.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new java.util.Date(currentTime)));
                
                String result = supabaseClient.insert("user_profiles", userProfile);
                Log.d(TAG, "创建用户配置文件成功: " + result);
                
                creationSuccess = true;
                
            } catch (Exception e) {
                // 如果是唯一约束错误（用户已存在），返回true
                if (e.getMessage() != null && (e.getMessage().contains("duplicate key") || e.getMessage().contains("23505") || e.getMessage().contains("already exists"))) {
                    Log.d(TAG, "用户配置文件已存在（并发创建）: " + userId);
                    creationSuccess = true;
                } else {
                    // 处理网络超时或连接错误
                    if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("connection") || e.getMessage().contains("network"))) {
                        Log.w(TAG, "网络连接问题，重试查询用户: " + userId);
                        
                        // 网络问题，重试查询确认用户是否存在
                        try {
                            Thread.sleep(500);
                            String retryQuery = supabaseClient.query("user_profiles", "id", 
                                "id=eq." + userId + "&limit=1");
                            
                            if (retryQuery != null && retryQuery.length() > 2 && !retryQuery.equals("[]")) {
                                Log.d(TAG, "网络重试成功，用户配置文件存在: " + userId);
                                creationSuccess = true;
                            }
                        } catch (Exception retryEx) {
                            Log.e(TAG, "网络重试失败", retryEx);
                            creationSuccess = false;
                        }
                    } else {
                        Log.e(TAG, "确保用户配置文件存在失败", e);
                        creationSuccess = false;
                    }
                }
            }
        } else {
            // 用户已存在
            creationSuccess = true;
        }
        
        // 更新状态
        synchronized (userProfileCreationStatus) {
            userProfileCreationStatus.put(userId, creationSuccess);
        }
        
        return creationSuccess;
    }
    
    /**
     * 异步确保用户配置文件存在（用于批量操作）
     */
    private void ensureUserProfileExists(String userId, String deviceId) {
        new Thread(() -> {
            ensureUserProfileExistsSync(userId, deviceId);
        }).start();
    }
}