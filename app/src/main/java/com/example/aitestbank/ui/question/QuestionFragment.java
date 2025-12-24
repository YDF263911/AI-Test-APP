package com.example.aitestbank.ui.question;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
// import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.service.AIService;
import com.example.aitestbank.supabase.SimpleSupabaseClient;

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
    private Button previousButton;
    private Button markButton;
    private Button submitButton;
    private Button nextButton;
    
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
        markButton = view.findViewById(R.id.mark_button);
        submitButton = view.findViewById(R.id.submit_button);
        nextButton = view.findViewById(R.id.next_button);

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
        
        // 标记按钮
        markButton.setOnClickListener(v -> {
            toggleMarkQuestion();
        });
        
        // 提交按钮
        submitButton.setOnClickListener(v -> {
            if (!selectedAnswer.isEmpty()) {
                checkAnswer();
            } else {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "请选择一个答案", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // 下一题按钮
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayCurrentQuestion();
            } else {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "已经是最后一题了", Toast.LENGTH_SHORT).show();
                }
            }
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
        
        // 隐藏上一题/下一题按钮，因为只有一题
        if (previousButton != null) {
            previousButton.setVisibility(View.GONE);
        }
        if (nextButton != null) {
            nextButton.setVisibility(View.GONE);
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
        // analysisContainer.setVisibility(View.GONE);
        aiAnalysisContainer.setVisibility(View.GONE);

        // 重置按钮状态
        submitButton.setEnabled(true);
        submitButton.setText("提交");
        
        // 更新标记按钮状态
        updateMarkButtonState(currentQuestion);
        
        // 滚动到顶部
        questionScrollView.scrollTo(0, 0);
    }
    
    private void updateMarkButtonState(Question question) {
        if (markedQuestions.contains(question.getId())) {
            // 已标记状态
            markButton.setText("取消标记");
            markButton.setBackgroundColor(getResources().getColor(R.color.accent_orange));
        } else {
            // 未标记状态
            markButton.setText("标记题目");
            markButton.setBackgroundColor(getResources().getColor(R.color.primary_blue));
        }
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
        optionLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setPadding(16, 12, 16, 12);
        optionLayout.setBackgroundResource(R.drawable.bg_search_bar);
        
        // 选项字母
        TextView letterView = new TextView(getContext());
        letterView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        letterView.setText(String.valueOf(optionChar) + ".");
        letterView.setTextColor(getResources().getColor(R.color.text_primary));
        letterView.setTextSize(16);
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
        textView.setTextSize(16);
        
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
        
        // 重置所有选项背景
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);
            child.setBackgroundResource(R.drawable.bg_search_bar);
        }
        
        // 高亮选中的选项
        optionView.setBackgroundResource(R.drawable.bg_card_white);
    }
    
    private void checkAnswer() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        isAnswered = true;
        
        // 检查答案
        boolean isCorrect = false;
        if (currentQuestion.getType().equals("single_choice")) {
            try {
                int selectedIndex = Integer.parseInt(selectedAnswer);
                isCorrect = (selectedIndex == currentQuestion.getCorrectAnswer());
            } catch (NumberFormatException e) {
                isCorrect = false;
            }
        } else {
            // 多选题暂不支持自动判分
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "多选题暂不支持自动判分", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        // 显示基础解析
        analysisContent.setText(currentQuestion.getAnalysis());
        // analysisContainer.setVisibility(View.VISIBLE);
        aiAnalysisContainer.setVisibility(View.VISIBLE);
        aiAnalysisContent.setVisibility(View.VISIBLE);
        
        // 调用AI智能解析
        if (aiService != null) {
            showAIProgress(true);
            aiService.getAIAnalysis(currentQuestion, selectedAnswer, new AIService.AICallback() {
                @Override
                public void onSuccess(AIService.AIAnalysisResult result) {
                    // 检查Fragment是否仍然有效
                    if (!isAdded() || getActivity() == null) {
                        Log.w(TAG, "Fragment is not attached, skipping AI result display");
                        return;
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        // 再次检查Fragment状态
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }
                        showAIProgress(false);
                        displayAIAnalysis(result);
                    });
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    // 检查Fragment是否仍然有效
                    if (!isAdded() || getActivity() == null) {
                        Log.w(TAG, "Fragment is not attached, skipping AI error handling");
                        return;
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        // 再次检查Fragment状态
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }
                        showAIProgress(false);
                        // 如果AI解析失败，仍然显示基础解析
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "AI解析暂时不可用，显示基础解析", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        
        // 更新按钮状态
        submitButton.setEnabled(false);
        submitButton.setText(isCorrect ? "回答正确" : "回答错误");
        submitButton.setBackgroundColor(isCorrect ? 
            getResources().getColor(R.color.success_green) : 
            getResources().getColor(R.color.error_red));
        
        // 显示结果提示
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), isCorrect ? "回答正确！🎉" : "回答错误，请查看解析", Toast.LENGTH_SHORT).show();
        }
        // 保存答题记录到Supabase
        saveAnswerRecord(currentQuestion, isCorrect);
    }
    
    private void saveAnswerRecord(Question question, boolean isCorrect) {
        new Thread(() -> {
            try {
                // 创建答题记录数据
                JSONObject answerRecord = new JSONObject();
                answerRecord.put("question_id", question.getId());
                answerRecord.put("user_answer", selectedAnswer);
                answerRecord.put("is_correct", isCorrect);
                answerRecord.put("timestamp", System.currentTimeMillis());
                
                // 插入到answer_records表
                String result = supabaseClient.insert("answer_records", answerRecord.toString());
                Log.d(TAG, "答题记录保存结果: " + result);
                
                // 如果回答错误，自动添加到错题本
                if (!isCorrect) {
                    saveToWrongQuestions(question);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "保存答题记录失败", e);
            }
        }).start();
    }
    
    private void saveToWrongQuestions(Question question) {
        try {
            // 检查是否已存在错题记录
            String checkResult = supabaseClient.query("wrong_questions", "id", "question_id=eq." + question.getId());
            JSONArray existingRecords = new JSONArray(checkResult);
            
            if (existingRecords.length() == 0) {
                // 创建新的错题记录
                JSONObject wrongQuestion = new JSONObject();
                wrongQuestion.put("question_id", question.getId());
                wrongQuestion.put("user_answer", selectedAnswer);
                wrongQuestion.put("wrong_count", 1);
                wrongQuestion.put("last_wrong_time", System.currentTimeMillis());
                wrongQuestion.put("is_mastered", false);
                
                String insertResult = supabaseClient.insert("wrong_questions", wrongQuestion.toString());
                Log.d(TAG, "新增错题记录结果: " + insertResult);
            } else {
                // 更新现有错题记录
                JSONObject existingRecord = existingRecords.getJSONObject(0);
                String wrongQuestionId = existingRecord.getString("id");
                
                JSONObject updateData = new JSONObject();
                updateData.put("wrong_count", existingRecord.optInt("wrong_count", 0) + 1);
                updateData.put("last_wrong_time", System.currentTimeMillis());
                updateData.put("user_answer", selectedAnswer);
                
                String updateResult = supabaseClient.update("wrong_questions", wrongQuestionId, updateData.toString());
                Log.d(TAG, "更新错题记录结果: " + updateResult);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "保存错题记录失败", e);
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
    
    private void toggleMarkQuestion() {
        if (questions.isEmpty() || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        String questionId = currentQuestion.getId();
        
        if (markedQuestions.contains(questionId)) {
            // 取消标记
            markedQuestions.remove(questionId);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "已取消标记", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 添加标记
            markedQuestions.add(questionId);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "题目已标记", Toast.LENGTH_SHORT).show();
            }
        }
        
        // 更新标记按钮状态
        updateMarkButtonState(currentQuestion);
        
        // 保存标记状态到本地存储（可选）
        saveMarkedQuestions();
    }
    
    private void saveMarkedQuestions() {
        // 这里可以实现将标记的题目保存到本地存储或Supabase
        // 简化实现：暂时只在内存中保存
        Log.d(TAG, "已标记题目数量: " + markedQuestions.size());
        
        // TODO: 实现持久化存储
        // 可以保存到SharedPreferences或Supabase的marked_questions表
    }
    
        // 可以在应用启动时加载已标记的题目
    private void loadMarkedQuestions() {
        // TODO: 从本地存储或Supabase加载已标记的题目
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
     * 显示AI智能解析结果
     */
    private void displayAIAnalysis(AIService.AIAnalysisResult result) {
        if (result == null) {
            return;
        }
        
        // 显示AI解析容器
        aiAnalysisContainer.setVisibility(View.VISIBLE);
        
        // 设置基础解析文本
        if (result.getAnalysisText() != null && !result.getAnalysisText().isEmpty()) {
            aiAnalysisText.setText(result.getAnalysisText());
        } else {
            aiAnalysisText.setText("暂无详细解析");
        }

        // 清空之前的列表内容
        // solutionStepsList.removeAllViews();
        // keyPointsList.removeAllViews();
        // commonMistakesList.removeAllViews();
        // recommendationsList.removeAllViews();

        // 显示解题步骤
        // if (result.getSolutionSteps() != null && !result.getSolutionSteps().isEmpty()) {
        //     displayListItems(solutionStepsList, result.getSolutionSteps(), "步骤");
        //     solutionStepsContainer.setVisibility(View.VISIBLE);
        // } else {
        //     solutionStepsContainer.setVisibility(View.GONE);
        // }

        // 显示核心考点
        // if (result.getKeyPoints() != null && !result.getKeyPoints().isEmpty()) {
        //     displayListItems(keyPointsList, result.getKeyPoints(), "•");
        //     keyPointsContainer.setVisibility(View.VISIBLE);
        // } else {
        //     keyPointsContainer.setVisibility(View.GONE);
        // }

        // 显示易错点
        // if (result.getCommonMistakes() != null && !result.getCommonMistakes().isEmpty()) {
        //     displayListItems(commonMistakesList, result.getCommonMistakes(), "⚠️");
        //     commonMistakesContainer.setVisibility(View.VISIBLE);
        // } else {
        //     commonMistakesContainer.setVisibility(View.GONE);
        // }

        // 显示学习建议
        // if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
        //     displayListItems(recommendationsList, result.getRecommendations(), "💡");
        //     recommendationsContainer.setVisibility(View.VISIBLE);
        // } else {
        //     recommendationsContainer.setVisibility(View.GONE);
        // }
        
        // 默认展开AI解析
        if (!isAIExpanded) {
            toggleAIExpansion();
        }
        
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "AI智能解析生成完成", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示列表项
     */
    /*
    private void displayListItems(LinearLayout container, List<String> items, String prefix) {
        for (int i = 0; i < items.size(); i++) {
            TextView textView = new TextView(getContext());
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textView.setText(prefix + " " + items.get(i));
            textView.setTextColor(getResources().getColor(R.color.text_primary));
            textView.setTextSize(14);
            textView.setLineSpacing(4, 1);
            textView.setPadding(8, 8, 8, 8);

            container.addView(textView);
        }
    }
    */
    
    /**
     * 在显示新题目时重置AI解析状态
     */
    private void resetAIAnalysis() {
        aiAnalysisContainer.setVisibility(View.GONE);
        isAIExpanded = false;
        aiAnalysisContent.setVisibility(View.GONE);
    }
}