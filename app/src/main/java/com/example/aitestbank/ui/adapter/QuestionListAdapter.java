package com.example.aitestbank.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目列表适配器 - 用于显示题目列表
 */
public class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.QuestionViewHolder> {
    
    private List<Question> questions = new ArrayList<>();
    private OnQuestionClickListener onQuestionClickListener;
    
    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_list, parent, false);
        return new QuestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.bind(question);
        
        holder.itemView.setOnClickListener(v -> {
            if (onQuestionClickListener != null) {
                onQuestionClickListener.onQuestionClick(question);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return questions.size();
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnQuestionClickListener(OnQuestionClickListener listener) {
        this.onQuestionClickListener = listener;
    }
    
    public interface OnQuestionClickListener {
        void onQuestionClick(Question question);
    }
    
    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        
        private TextView questionNumber;
        private TextView questionTitle;
        private TextView questionType;
        private TextView questionDifficulty;
        
        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumber = itemView.findViewById(R.id.question_number);
            questionTitle = itemView.findViewById(R.id.question_title);
            questionType = itemView.findViewById(R.id.question_type);
            questionDifficulty = itemView.findViewById(R.id.question_difficulty);
        }
        
        public void bind(Question question) {
            questionNumber.setText(String.valueOf(getAdapterPosition() + 1));
            
            // 限制题目标题长度
            String title = question.getTitle();
            if (title.length() > 50) {
                title = title.substring(0, 50) + "...";
            }
            questionTitle.setText(title);
            
            questionType.setText(getQuestionTypeText(question.getType()));
            questionDifficulty.setText(getDifficultyStars(question.getDifficulty()));
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
}