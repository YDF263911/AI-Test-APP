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
 * 题库分类适配器
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener onCategoryClickListener;
    
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
    
    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.onCategoryClickListener = listener;
    }
    
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryName;
        private TextView questionCount;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            questionCount = itemView.findViewById(R.id.question_count);
        }
        
        public void bind(Category category) {
            categoryName.setText(category.getName());
            questionCount.setText(category.getQuestionCount() + " 道题");
            
            itemView.setOnClickListener(v -> {
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryClick(category);
                }
            });
        }
    }
    
    /**
     * 题库分类数据模型
     */
    public static class Category {
        private String id;
        private String name;
        private int questionCount;
        
        public Category(String id, String name, int questionCount) {
            this.id = id;
            this.name = name;
            this.questionCount = questionCount;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getQuestionCount() { return questionCount; }
    }
}