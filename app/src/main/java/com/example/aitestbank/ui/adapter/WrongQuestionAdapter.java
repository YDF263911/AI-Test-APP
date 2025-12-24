package com.example.aitestbank.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
        // private TextView knowledgePoint;
        // private ImageButton deleteButton;
        // private View masteredButton;
        // private View notMasteredButton;

        public WrongQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionPreview = itemView.findViewById(R.id.question_preview);
            wrongCount = itemView.findViewById(R.id.wrong_count);
            lastWrongDate = itemView.findViewById(R.id.last_review_time);
            // knowledgePoint = itemView.findViewById(R.id.knowledge_point);
            // deleteButton = itemView.findViewById(R.id.delete_button);
            // masteredButton = itemView.findViewById(R.id.mastered_button);
            // notMasteredButton = itemView.findViewById(R.id.not_mastered_button);
        }

        public void bind(WrongQuestionItem wrongQuestion) {
            questionPreview.setText(wrongQuestion.getQuestionPreview());
            wrongCount.setText("错误次数: " + wrongQuestion.getWrongCount());
            lastWrongDate.setText("最近错误: " + wrongQuestion.getLastWrongDate());
            // knowledgePoint.setText("考点: " + wrongQuestion.getKnowledgePoint());

            // if (wrongQuestion.isMastered()) {
            //     masteredButton.setVisibility(View.VISIBLE);
            //     notMasteredButton.setVisibility(View.GONE);
            // } else {
            //     masteredButton.setVisibility(View.GONE);
            //     notMasteredButton.setVisibility(View.VISIBLE);
            // }

            itemView.setOnClickListener(v -> {
                if (onWrongQuestionClickListener != null) {
                    onWrongQuestionClickListener.onWrongQuestionClick(wrongQuestion);
                }
            });

            // 删除按钮点击事件
            // if (deleteButton != null) {
            //     deleteButton.setOnClickListener(v -> {
            //         if (onWrongQuestionClickListener != null) {
            //             onWrongQuestionClickListener.onDeleteWrongQuestion(wrongQuestion);
            //         }
            //     });
            // }

            // 已掌握按钮点击事件
            // if (masteredButton != null) {
            //     masteredButton.setOnClickListener(v -> {
            //         if (onWrongQuestionClickListener != null) {
            //             onWrongQuestionClickListener.onMarkMastered(wrongQuestion);
            //         }
            //     });
            // }
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
        
        public WrongQuestionItem(String id, String questionPreview, int wrongCount, 
                                String lastWrongDate, String knowledgePoint, boolean isMastered) {
            this.id = id;
            this.questionPreview = questionPreview;
            this.wrongCount = wrongCount;
            this.lastWrongDate = lastWrongDate;
            this.knowledgePoint = knowledgePoint;
            this.isMastered = isMastered;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public String getQuestionPreview() { return questionPreview; }
        public int getWrongCount() { return wrongCount; }
        public String getLastWrongDate() { return lastWrongDate; }
        public String getKnowledgePoint() { return knowledgePoint; }
        public boolean isMastered() { return isMastered; }
        public void setMastered(boolean mastered) { isMastered = mastered; }
    }
}