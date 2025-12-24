package com.example.aitestbank.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目列表适配器
 */
public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    
    private List<QuestionItem> questions = new ArrayList<>();
    private OnQuestionClickListener onQuestionClickListener;
    
    public interface OnQuestionClickListener {
        void onQuestionClick(QuestionItem question);
        void onQuestionMark(QuestionItem question);
    }
    
    public void setOnQuestionClickListener(OnQuestionClickListener listener) {
        this.onQuestionClickListener = listener;
    }
    
    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionItem question = questions.get(position);
        holder.bind(question);
    }
    
    @Override
    public int getItemCount() {
        return questions.size();
    }
    
    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView questionNumber;
        private TextView questionPreview;
        private TextView difficulty;
        private TextView questionType;
        private View markButton;
        
        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumber = itemView.findViewById(R.id.question_number);
            questionPreview = itemView.findViewById(R.id.question_preview);
            difficulty = itemView.findViewById(R.id.difficulty);
            questionType = itemView.findViewById(R.id.question_type);
            markButton = itemView.findViewById(R.id.mark_button);
        }
        
        public void bind(QuestionItem question) {
            questionNumber.setText("题目 " + (getAdapterPosition() + 1));
            questionPreview.setText(question.getPreview());
            difficulty.setText("难度: " + question.getDifficulty() + "★");
            questionType.setText(getQuestionTypeText(question.getType()));
            
            // 设置标记状态
            if (question.isMarked()) {
                markButton.setBackgroundResource(R.drawable.ic_bookmark_filled);
            } else {
                markButton.setBackgroundResource(R.drawable.ic_bookmark_outline);
            }
            
            itemView.setOnClickListener(v -> {
                if (onQuestionClickListener != null) {
                    onQuestionClickListener.onQuestionClick(question);
                }
            });
            
            markButton.setOnClickListener(v -> {
                if (onQuestionClickListener != null) {
                    onQuestionClickListener.onQuestionMark(question);
                }
            });
        }
        
        private String getQuestionTypeText(String type) {
            switch (type) {
                case "single_choice": return "单选";
                case "multiple_choice": return "多选";
                case "true_false": return "判断";
                case "fill_blank": return "填空";
                default: return "未知";
            }
        }
    }
    
    /**
     * 题目列表项数据模型
     */
    public static class QuestionItem {
        private String id;
        private String preview;
        private String difficulty;
        private String type;
        private boolean isMarked;
        
        public QuestionItem(String id, String preview, String difficulty, String type, boolean isMarked) {
            this.id = id;
            this.preview = preview;
            this.difficulty = difficulty;
            this.type = type;
            this.isMarked = isMarked;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public String getPreview() { return preview; }
        public String getDifficulty() { return difficulty; }
        public String getType() { return type; }
        public boolean isMarked() { return isMarked; }
        public void setMarked(boolean marked) { isMarked = marked; }
    }
}