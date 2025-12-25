package com.example.aitestbank.ui.result;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;
import com.example.aitestbank.model.Question;

import java.util.List;

/**
 * 答题卡适配器 - 显示题目答题状态
 */
public class AnswerCardAdapter extends RecyclerView.Adapter<AnswerCardAdapter.ViewHolder> {
    
    private List<Question> questions;
    private List<Boolean> userAnswers;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(int position, Question question, boolean isCorrect);
    }
    
    public AnswerCardAdapter(List<Question> questions, List<Boolean> userAnswers) {
        this.questions = questions;
        this.userAnswers = userAnswers;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_answer_card, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question question = questions.get(position);
        Boolean isCorrect = userAnswers.get(position);
        
        // 设置题目编号
        holder.numberText.setText(String.valueOf(position + 1));
        
        // 设置状态样式
        if (isCorrect == null) {
            // 未答题
            holder.itemView.setBackgroundResource(R.drawable.bg_answer_unanswered);
            holder.numberText.setTextColor(holder.itemView.getContext().getResources()
                .getColor(R.color.text_secondary));
        } else if (isCorrect) {
            // 正确
            holder.itemView.setBackgroundResource(R.drawable.bg_answer_correct);
            holder.numberText.setTextColor(holder.itemView.getContext().getResources()
                .getColor(R.color.white));
        } else {
            // 错误
            holder.itemView.setBackgroundResource(R.drawable.bg_answer_wrong);
            holder.numberText.setTextColor(holder.itemView.getContext().getResources()
                .getColor(R.color.white));
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, question, isCorrect != null && isCorrect);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.answer_number_text);
        }
    }
}