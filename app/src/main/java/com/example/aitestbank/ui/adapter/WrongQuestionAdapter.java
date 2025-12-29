package com.example.aitestbank.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 错题列表适配器
 */
public class WrongQuestionAdapter extends RecyclerView.Adapter<WrongQuestionAdapter.WrongQuestionViewHolder> {
    
    private List<WrongQuestionItem> wrongQuestions = new ArrayList<>();
    private OnWrongQuestionClickListener onWrongQuestionClickListener;
    
    public interface OnWrongQuestionClickListener {
        void onWrongQuestionClick(WrongQuestionItem wrongQuestion);
        void onDeleteWrongQuestion(WrongQuestionItem wrongQuestion);
        void onMarkMastered(WrongQuestionItem wrongQuestion);
    }
    
    public void setOnWrongQuestionClickListener(OnWrongQuestionClickListener listener) {
        this.onWrongQuestionClickListener = listener;
    }
    
    public void setWrongQuestions(List<WrongQuestionItem> wrongQuestions) {
        this.wrongQuestions = wrongQuestions;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public WrongQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wrong_question, parent, false);
        return new WrongQuestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WrongQuestionViewHolder holder, int position) {
        WrongQuestionItem wrongQuestion = wrongQuestions.get(position);
        holder.bind(wrongQuestion);
    }
    
    @Override
    public int getItemCount() {
        return wrongQuestions.size();
    }
    
    class WrongQuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView questionPreview;
        private TextView wrongCount;
        private TextView lastWrongDate;
        private TextView masteryStatusText;
        private TextView subjectText;
        private TextView difficultyText;
        private MaterialButton actionButton;
        private View itemView;

        public WrongQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            questionPreview = itemView.findViewById(R.id.question_preview);
            wrongCount = itemView.findViewById(R.id.wrong_count);
            lastWrongDate = itemView.findViewById(R.id.last_review_time);
            masteryStatusText = itemView.findViewById(R.id.mastery_status_text);
            subjectText = itemView.findViewById(R.id.subject_text);
            difficultyText = itemView.findViewById(R.id.difficulty_text);
            actionButton = itemView.findViewById(R.id.action_button);
        }

        public void bind(WrongQuestionItem wrongQuestion) {
            questionPreview.setText(wrongQuestion.getQuestionPreview());
            wrongCount.setText("错误" + wrongQuestion.getWrongCount() + "次");
            lastWrongDate.setText(formatLastWrongDate(wrongQuestion.getLastWrongDate()));
            
            // 设置掌握状态
            if (wrongQuestion.isMastered()) {
                masteryStatusText.setText("已掌握");
                masteryStatusText.setTextColor(itemView.getContext().getColor(R.color.success_green));
                itemView.setAlpha(0.7f);
            } else {
                masteryStatusText.setText("未掌握");
                masteryStatusText.setTextColor(itemView.getContext().getColor(R.color.white));
                itemView.setAlpha(1f);
            }
            
            // 设置学科和难度
            subjectText.setText(wrongQuestion.getKnowledgePoint());
            difficultyText.setText(getDifficultyText(wrongQuestion.getDifficulty()));

            itemView.setOnClickListener(v -> {
                if (onWrongQuestionClickListener != null) {
                    onWrongQuestionClickListener.onWrongQuestionClick(wrongQuestion);
                }
            });

            // 长按删除功能
            itemView.setOnLongClickListener(v -> {
                if (onWrongQuestionClickListener != null) {
                    onWrongQuestionClickListener.onDeleteWrongQuestion(wrongQuestion);
                    return true;
                }
                return false;
            });

            // 操作按钮点击事件（切换掌握状态）
            actionButton.setOnClickListener(v -> {
                if (onWrongQuestionClickListener != null) {
                    onWrongQuestionClickListener.onMarkMastered(wrongQuestion);
                }
            });
        }
        
        private String formatLastWrongDate(String date) {
            // 简化处理，实际应该根据时间差显示"今天"、"昨天"、"X天前"等
            return date;
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
    }
    
    /**
     * 错题数据模型
     */
    public static class WrongQuestionItem {
        private String id;
        private String questionPreview;
        private int wrongCount;
        private String lastWrongDate;
        private String knowledgePoint;
        private boolean isMastered;
        private int difficulty;
        private long createdAt; // 创建时间戳（毫秒）
        private long updatedAt; // 更新时间戳（毫秒）
        private String category; // 分类
        private String subject; // 科目
        
        public WrongQuestionItem(String id, String questionPreview, int wrongCount, 
                                String lastWrongDate, String knowledgePoint, boolean isMastered) {
            this.id = id;
            this.questionPreview = questionPreview;
            this.wrongCount = wrongCount;
            this.lastWrongDate = lastWrongDate;
            this.knowledgePoint = knowledgePoint;
            this.isMastered = isMastered;
            this.difficulty = 3; // 默认中等难度
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = System.currentTimeMillis();
            this.category = "未分类";
            this.subject = "未分类";
        }
        
        public WrongQuestionItem(String id, String questionPreview, int wrongCount, 
                                String lastWrongDate, String knowledgePoint, boolean isMastered, int difficulty) {
            this.id = id;
            this.questionPreview = questionPreview;
            this.wrongCount = wrongCount;
            this.lastWrongDate = lastWrongDate;
            this.knowledgePoint = knowledgePoint;
            this.isMastered = isMastered;
            this.difficulty = difficulty;
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = System.currentTimeMillis();
            this.category = "未分类";
            this.subject = "未分类";
        }
        
        public WrongQuestionItem(String id, String questionPreview, int wrongCount, 
                                String lastWrongDate, String knowledgePoint, boolean isMastered, 
                                int difficulty, long createdAt, long updatedAt, String category, String subject) {
            this.id = id;
            this.questionPreview = questionPreview;
            this.wrongCount = wrongCount;
            this.lastWrongDate = lastWrongDate;
            this.knowledgePoint = knowledgePoint;
            this.isMastered = isMastered;
            this.difficulty = difficulty;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.category = category;
            this.subject = subject;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public String getQuestionPreview() { return questionPreview; }
        public int getWrongCount() { return wrongCount; }
        public String getLastWrongDate() { return lastWrongDate; }
        public String getKnowledgePoint() { return knowledgePoint; }
        public boolean isMastered() { return isMastered; }
        public int getDifficulty() { return difficulty; }
        public long getCreatedAt() { return createdAt; }
        public long getUpdatedAt() { return updatedAt; }
        public String getCategory() { return category; }
        public String getSubject() { return subject; }
        public void setMastered(boolean mastered) { isMastered = mastered; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
        public void setCategory(String category) { this.category = category; }
        public void setSubject(String subject) { this.subject = subject; }
    }
}