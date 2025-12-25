package com.example.aitestbank.ui.result;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        if (getArguments() != null) {
            // 从Arguments获取题目和答案数据
            questions = getArguments().getParcelableArrayList("questions");
            List<Integer> answers = getArguments().getIntegerArrayList("user_answers");
            
            if (answers != null) {
                userAnswers = new ArrayList<>();
                for (Integer answer : answers) {
                    userAnswers.add(answer >= 0); // 假设正确答案索引>=0，未答为-1
                }
            } else {
                // 如果没有答案数据，生成默认数据
                userAnswers = new ArrayList<>();
                if (questions != null) {
                    for (int i = 0; i < questions.size(); i++) {
                        userAnswers.add(i % 3 == 0); // 模拟：每3题答对1题
                    }
                }
            }
        } else {
            // 测试数据
            generateTestData();
        }
    }
    
    private void generateTestData() {
        questions = new ArrayList<>();
        userAnswers = new ArrayList<>();
        
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
    }
    
    private void setupAnswerCard() {
        answerCardAdapter = new AnswerCardAdapter(questions, userAnswers);
        answerCardRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        answerCardRecyclerView.setAdapter(answerCardAdapter);
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
        timeText.setText("用时：" + (int)(Math.random() * 10 + 5) + "分钟"); // 模拟用时
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
        analyzeButton.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "正在生成详细分析...", Toast.LENGTH_SHORT).show();
                // TODO: 跳转到详细分析页面
            }
        });
        
        // 答题卡点击事件
        answerCardAdapter.setOnItemClickListener((position, question, isCorrect) -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), 
                    "第" + (position + 1) + "题 - " + (isCorrect ? "正确" : "错误"), 
                    Toast.LENGTH_SHORT).show();
                // TODO: 跳转到该题目的详细解析
            }
        });
    }
}