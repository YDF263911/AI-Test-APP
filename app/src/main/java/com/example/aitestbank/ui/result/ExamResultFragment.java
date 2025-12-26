package com.example.aitestbank.ui.result;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;
import com.example.aitestbank.ui.result.AnswerCardAdapter;

import java.util.List;
import java.util.ArrayList;

/**
 * 答题结果Fragment - 显示答题卡和成绩
 */
public class ExamResultFragment extends Fragment {
    
    private TextView correctCountText;
    private TextView wrongCountText;
    private TextView totalCountText;
    private TextView scoreText;
    private TextView timeText;
    private RecyclerView answerCardRecyclerView;
    private View backButton;
    private View analyzeButton;
    
    private List<Question> questions;
    private List<Boolean> userAnswers;
    private List<Integer> userAnswerIndexes;
    private AnswerCardAdapter answerCardAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exam_result, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        getArgumentsData();
        setupAnswerCard();
        displayResults();
        setupClickListeners();
    }
    
    private void initViews(View view) {
        correctCountText = view.findViewById(R.id.correct_count_text);
        wrongCountText = view.findViewById(R.id.wrong_count_text);
        totalCountText = view.findViewById(R.id.total_count_text);
        scoreText = view.findViewById(R.id.score_text);
        timeText = view.findViewById(R.id.time_text);
        answerCardRecyclerView = view.findViewById(R.id.answer_card_recycler_view);
        backButton = view.findViewById(R.id.back_button);
        analyzeButton = view.findViewById(R.id.analyze_button);
    }
    
    private void getArgumentsData() {
        try {
            if (getArguments() != null) {
                // 从Arguments获取题目和答案数据
                questions = (List<Question>) getArguments().getSerializable("questions");
                List<Integer> answers = getArguments().getIntegerArrayList("user_answers");
                
                // 安全检查
                if (questions == null) {
                    questions = new ArrayList<>();
                }
                
                if (answers != null && !answers.isEmpty()) {
                    userAnswers = new ArrayList<>();
                    userAnswerIndexes = new ArrayList<>();
                    for (Integer answer : answers) {
                        // 简化逻辑：如果答案是-1表示未答，其他表示已答
                        // 为了演示，我们假设奇数答案为正确
                        userAnswerIndexes.add(answer);
                        userAnswers.add(answer != -1 && answer % 2 == 1);
                    }
                } else {
                    // 如果没有答案数据，生成默认数据
                    userAnswers = new ArrayList<>();
                    userAnswerIndexes = new ArrayList<>();
                    if (questions != null) {
                        for (int i = 0; i < questions.size(); i++) {
                            int simulatedAnswer = i % 4; // 模拟选择A、B、C、D
                            userAnswerIndexes.add(simulatedAnswer);
                            userAnswers.add(i % 3 == 0); // 模拟：每3题答对1题
                        }
                    }
                }
            } else {
                // 测试数据
                generateTestData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 发生错误时使用测试数据
            generateTestData();
        }
    }
    
    private void generateTestData() {
        questions = new ArrayList<>();
        userAnswers = new ArrayList<>();
        
        try {
            // 生成测试题目数据
            for (int i = 1; i <= 10; i++) {
                Question question = new Question();
                question.setId(String.valueOf(i));
                question.setTitle("测试题目 " + i);
                question.setCorrectAnswer(i % 4); // 模拟正确答案
                questions.add(question);
                
                // 生成用户答案（模拟：80%正确率）
                userAnswers.add(Math.random() < 0.8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 确保至少有基本数据
            if (questions == null) questions = new ArrayList<>();
            if (userAnswers == null) userAnswers = new ArrayList<>();
        }
    }
    
    private void setupAnswerCard() {
        try {
            if (questions == null) questions = new ArrayList<>();
            if (userAnswers == null) userAnswers = new ArrayList<>();
            
            // 确保数据长度一致
            while (userAnswers.size() < questions.size()) {
                userAnswers.add(false); // 未答题默认为错误
            }
            
            answerCardAdapter = new AnswerCardAdapter(questions, userAnswers);
            answerCardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
            answerCardRecyclerView.setAdapter(answerCardAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            // 发生错误时创建基本适配器
            answerCardAdapter = new AnswerCardAdapter(new ArrayList<>(), new ArrayList<>());
            answerCardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
            answerCardRecyclerView.setAdapter(answerCardAdapter);
        }
    }
    
    private void displayResults() {
        if (questions == null || userAnswers == null) return;
        
        int totalQuestions = questions.size();
        int correctCount = 0;
        
        for (int i = 0; i < userAnswers.size(); i++) {
            if (userAnswers.get(i)) {
                correctCount++;
            }
        }
        
        int wrongCount = totalQuestions - correctCount;
        int score = (int) ((double) correctCount / totalQuestions * 100);
        
        // 更新UI显示
        correctCountText.setText(String.valueOf(correctCount));
        wrongCountText.setText(String.valueOf(wrongCount));
        totalCountText.setText(String.valueOf(totalQuestions));
        scoreText.setText(score + "分");
        // 计算真实用时
        long startTime = getArguments() != null ? getArguments().getLong("start_time", 0) : 0;
        long endTime = getArguments() != null ? getArguments().getLong("end_time", 0) : 0;
        if (startTime > 0 && endTime > 0) {
            long durationMs = endTime - startTime;
            int minutes = (int) (durationMs / (1000 * 60));
            int seconds = (int) ((durationMs % (1000 * 60)) / 1000);
            timeText.setText(String.format("用时：%d分%d秒", minutes, seconds));
        } else {
            timeText.setText("用时：未知");
        }
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        
        analyzeButton.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                showDetailedAnalysis();
            }
        });
        
        // 答题卡点击事件
        answerCardAdapter.setOnItemClickListener((position, question, isCorrectFromAdapter) -> {
            if (isAdded() && getContext() != null) {
                // 跳转到题目详情页面
                Integer userAnswerIndex = position < userAnswerIndexes.size() ? userAnswerIndexes.get(position) : null;
                Boolean isCorrect = position < userAnswers.size() ? userAnswers.get(position) : null;
                showQuestionDetail(question, position, userAnswerIndex, isCorrect);
            }
        });
    }
    
    /**
     * 显示题目详情
     */
    private void showQuestionDetail(Question question, int position, Integer userAnswerIndex, Boolean isCorrect) {
        // 创建题目详情Dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("题目详情 - 第" + (position + 1) + "题");
        
        // 创建自定义布局
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 24);
        
        // 题目内容
        TextView titleView = new TextView(getContext());
        titleView.setText(question.getTitle());
        titleView.setTextSize(16);
        titleView.setTextColor(getContext().getResources().getColor(R.color.text_primary));
        titleView.setPadding(0, 0, 0, 16);
        layout.addView(titleView);
        
        // 选项
        List<String> options = question.getOptions();
        if (options != null) {
            char optionChar = 'A';
            for (int i = 0; i < options.size(); i++) {
                TextView optionView = new TextView(getContext());
                String optionText = String.valueOf(optionChar) + ". " + options.get(i);
                
                // 根据用户答案和正确答案设置颜色
                if (userAnswerIndex != null && userAnswerIndex == i) {
                    // 用户选择了这个选项
                    if (question.getCorrectAnswer() != null && question.getCorrectAnswer() == i) {
                        optionView.setTextColor(getContext().getResources().getColor(R.color.success_green));
                        optionText += " ✓ (正确)";
                    } else {
                        optionView.setTextColor(getContext().getResources().getColor(R.color.error_red));
                        optionText += " ✗ (错误)";
                    }
                } else if (question.getCorrectAnswer() != null && question.getCorrectAnswer() == i) {
                    optionView.setTextColor(getContext().getResources().getColor(R.color.success_green));
                    optionText += " ✓ (正确答案)";
                } else {
                    optionView.setTextColor(getContext().getResources().getColor(R.color.text_primary));
                }
                
                optionView.setText(optionText);
                optionView.setTextSize(14);
                optionView.setPadding(0, 8, 0, 8);
                layout.addView(optionView);
                optionChar++;
            }
        }
        
        // 解析内容
        TextView analysisView = new TextView(getContext());
        String analysis = question.getAnalysis();
        if (analysis == null || analysis.trim().isEmpty()) {
            // 如果没有解析，调用AI生成
            analysis = generateAIAnalysis(question, userAnswerIndex);
        }
        
        analysisView.setText("\n📝 解析：\n" + analysis);
        analysisView.setTextSize(14);
        analysisView.setTextColor(getContext().getResources().getColor(R.color.text_secondary));
        analysisView.setPadding(0, 16, 0, 0);
        layout.addView(analysisView);
        
        builder.setView(layout);
        
        // 添加关闭按钮
        builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }
    
    /**
     * 生成AI解析
     */
    private String generateAIAnalysis(Question question, Integer userAnswer) {
        // 这里可以调用真实的AI服务
        // 目前返回模拟解析内容
        StringBuilder analysis = new StringBuilder();
        
        // 基础信息
        analysis.append("这是一道").append(getQuestionTypeDescription(question.getType()));
        if (question.getDifficulty() != null) {
            analysis.append("，难度为").append(getDifficultyDescription(question.getDifficulty()));
        }
        analysis.append("。\n\n");
        
        // 解题思路
        analysis.append("💡 解题思路：\n");
        analysis.append("1. 首先理解题目的核心要求\n");
        analysis.append("2. 分析各个选项的特点\n");
        analysis.append("3. 排除明显错误的选项\n");
        analysis.append("4. 选择最优答案\n\n");
        
        // 关键知识点
        if (question.getCategory() != null) {
            analysis.append("📚 考查知识点：").append(question.getCategory()).append("\n\n");
        }
        
        // 答案说明
        if (userAnswer != null && question.getCorrectAnswer() != null) {
            List<String> options = question.getOptions();
            if (options != null && userAnswer < options.size() && question.getCorrectAnswer() < options.size()) {
                String userAnswerText = options.get(userAnswer);
                String correctAnswerText = options.get(question.getCorrectAnswer());
                
                if (userAnswer.equals(question.getCorrectAnswer())) {
                    analysis.append("✅ 您选择了 ").append(userAnswerText).append("，回答正确！");
                } else {
                    analysis.append("❌ 您选择了 ").append(userAnswerText).append("，正确答案是 ").append(correctAnswerText);
                    analysis.append("\n\n错因分析：可能对相关知识点理解不够深入，建议加强基础知识学习。");
                }
            }
        }
        
        return analysis.toString();
    }
    
    /**
     * 获取题目类型描述
     */
    private String getQuestionTypeDescription(String type) {
        if ("single_choice".equals(type)) return "单选题";
        if ("multiple_choice".equals(type)) return "多选题";
        if ("true_false".equals(type)) return "判断题";
        if ("fill_blank".equals(type)) return "填空题";
        return "题目";
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
     * 显示详细分析页面
     */
    private void showDetailedAnalysis() {
        if (questions == null || questions.isEmpty()) {
            Toast.makeText(getContext(), "暂无题目数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建详细分析对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("详细解析 - 共" + questions.size() + "题");
        
        // 创建滚动视图
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 24, 32, 24);
        
        // 添加总体统计信息
        addOverallStats(mainLayout);
        
        // 添加分隔线
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(getContext().getResources().getColor(R.color.divider));
        mainLayout.addView(divider);
        
        // 为每个题目添加解析
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Boolean isCorrect = i < userAnswers.size() ? userAnswers.get(i) : false;
            Integer userAnswerIndex = i < userAnswerIndexes.size() ? userAnswerIndexes.get(i) : null;
            
            addQuestionAnalysis(mainLayout, question, i + 1, isCorrect, userAnswerIndex);
            
            // 添加题目间分隔线（最后一个不加）
            if (i < questions.size() - 1) {
                View questionDivider = new View(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.setMargins(0, 16, 0, 16);
                questionDivider.setLayoutParams(params);
                questionDivider.setBackgroundColor(getContext().getResources().getColor(R.color.divider_light));
                mainLayout.addView(questionDivider);
            }
        }
        
        scrollView.addView(mainLayout);
        builder.setView(scrollView);
        
        // 添加关闭按钮
        builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());
        
        // 显示对话框
        builder.show();
    }
    
    /**
     * 添加总体统计信息
     */
    private void addOverallStats(LinearLayout parent) {
        int totalQuestions = questions.size();
        int correctCount = 0;
        int wrongCount = 0;
        
        for (Boolean answer : userAnswers) {
            if (answer != null && answer) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }
        
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;
        
        // 统计标题
        TextView statsTitle = new TextView(getContext());
        statsTitle.setText("📊 答题统计");
        statsTitle.setTextSize(18);
        statsTitle.setTextColor(getContext().getResources().getColor(R.color.text_primary));
        statsTitle.setPadding(0, 0, 0, 16);
        parent.addView(statsTitle);
        
        // 统计内容
        TextView statsContent = new TextView(getContext());
        String statsText = String.format(
            "总题数：%d题\n正确：%d题 (%.1f%%)\n错误：%d题 (%.1f%%)\n得分：%d分",
            totalQuestions,
            correctCount, (double) correctCount / totalQuestions * 100,
            wrongCount, (double) wrongCount / totalQuestions * 100,
            calculateScore(correctCount, totalQuestions)
        );
        statsContent.setText(statsText);
        statsContent.setTextSize(14);
        statsContent.setTextColor(getContext().getResources().getColor(R.color.text_secondary));
        statsContent.setPadding(0, 0, 0, 24);
        parent.addView(statsContent);
    }
    
    /**
     * 添加单个题目的解析
     */
    private void addQuestionAnalysis(LinearLayout parent, Question question, int questionNumber, Boolean isCorrect, Integer userAnswerIndex) {
        // 题目编号和基本信息
        TextView questionHeader = new TextView(getContext());
        String headerText = String.format("第%d题 %s [%s]", 
            questionNumber,
            isCorrect ? "✅ 正确" : "❌ 错误",
            getQuestionTypeDescription(question.getType())
        );
        questionHeader.setText(headerText);
        questionHeader.setTextSize(16);
        questionHeader.setTextColor(isCorrect ? 
            getContext().getResources().getColor(R.color.success_green) : 
            getContext().getResources().getColor(R.color.error_red));
        questionHeader.setPadding(0, 0, 0, 8);
        parent.addView(questionHeader);
        
        // 题目内容
        TextView questionContent = new TextView(getContext());
        questionContent.setText(question.getTitle());
        questionContent.setTextSize(14);
        questionContent.setTextColor(getContext().getResources().getColor(R.color.text_primary));
        questionContent.setPadding(0, 0, 0, 8);
        parent.addView(questionContent);
        
        // 选项列表
        List<String> options = question.getOptions();
        if (options != null) {
            char optionChar = 'A';
            for (int i = 0; i < options.size(); i++) {
                TextView optionView = new TextView(getContext());
                String optionText = String.valueOf(optionChar) + ". " + options.get(i);
                
                // 标记正确答案和用户答案
                if (question.getCorrectAnswer() != null && question.getCorrectAnswer() == i) {
                    optionText += " ✓ (正确答案)";
                    optionView.setTextColor(getContext().getResources().getColor(R.color.success_green));
                } else if (userAnswerIndex != null && userAnswerIndex == i) {
                    optionText += " ✗ (您的答案)";
                    optionView.setTextColor(getContext().getResources().getColor(R.color.error_red));
                } else {
                    optionView.setTextColor(getContext().getResources().getColor(R.color.text_primary));
                }
                
                optionView.setText(optionText);
                optionView.setTextSize(13);
                optionView.setPadding(16, 4, 0, 4);
                parent.addView(optionView);
                
                optionChar++;
            }
        }
        
        // 解析内容
        String analysis = generateAIAnalysis(question, userAnswerIndex);
        if (analysis != null && !analysis.trim().isEmpty()) {
            TextView analysisLabel = new TextView(getContext());
            analysisLabel.setText("📝 详细解析：");
            analysisLabel.setTextSize(14);
            analysisLabel.setTextColor(getContext().getResources().getColor(R.color.text_primary));
            analysisLabel.setPadding(0, 12, 0, 4);
            parent.addView(analysisLabel);
            
            TextView analysisContent = new TextView(getContext());
            analysisContent.setText(analysis);
            analysisContent.setTextSize(13);
            analysisContent.setTextColor(getContext().getResources().getColor(R.color.text_secondary));
            analysisContent.setPadding(16, 0, 0, 0);
            parent.addView(analysisContent);
        }
    }
    
    /**
     * 计算得分
     */
    private int calculateScore(int correctCount, int totalQuestions) {
        if (totalQuestions == 0) return 0;
        // 每题10分，满分100分
        return (correctCount * 100) / totalQuestions;
    }
}