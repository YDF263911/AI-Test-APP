package com.example.aitestbank.ui.wrong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aitestbank.R;
import com.example.aitestbank.supabase.SimpleSupabaseClient;
import com.example.aitestbank.ui.adapter.WrongQuestionAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 错题本Fragment
 */
public class WrongQuestionFragment extends Fragment {
    
    private static final String TAG = "WrongQuestionFragment";
    
    // UI组件
    private RecyclerView wrongQuestionRecyclerView;
    private TextView wrongCountText;
    private TextView masteredCount;
    private TextView reviewRate;
    private AutoCompleteTextView filterSpinner;
    private TextView clearMastered;
    private View emptyState;
    
    // 数据和客户端
    private SimpleSupabaseClient supabaseClient;
    private WrongQuestionAdapter wrongQuestionAdapter;
    private List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestions = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wrong_question, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSupabase();
        setupRecyclerView();
        setupFilterSpinner();
        setupClickListeners();
        loadWrongQuestions();
        loadStatistics();
    }
    
    private void initViews(View view) {
        wrongQuestionRecyclerView = view.findViewById(R.id.wrong_question_recycler_view);
        wrongCountText = view.findViewById(R.id.wrong_count_text);
        masteredCount = view.findViewById(R.id.mastered_count);
        reviewRate = view.findViewById(R.id.review_rate);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        clearMastered = view.findViewById(R.id.clear_mastered);
        emptyState = view.findViewById(R.id.empty_state);
    }
    
    private void setupSupabase() {
        supabaseClient = SimpleSupabaseClient.getInstance();
    }
    
    private void setupRecyclerView() {
        wrongQuestionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wrongQuestionAdapter = new WrongQuestionAdapter();
        wrongQuestionRecyclerView.setAdapter(wrongQuestionAdapter);
        
        // 设置错题列表项点击事件
        wrongQuestionAdapter.setOnWrongQuestionClickListener(new WrongQuestionAdapter.OnWrongQuestionClickListener() {
            @Override
            public void onWrongQuestionClick(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
                // 跳转到错题详情页面
                Intent intent = new Intent(getActivity(), WrongQuestionDetailActivity.class);
                intent.putExtra("wrong_question_id", wrongQuestion.getId());
                startActivity(intent);
            }
            
            @Override
            public void onDeleteWrongQuestion(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
                deleteWrongQuestion(wrongQuestion);
            }
            
            @Override
            public void onMarkMastered(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
                markAsMastered(wrongQuestion);
            }
        });
    }
    
    private void setupFilterSpinner() {
        // 设置筛选条件
        String[] filterOptions = {"全部错题", "未掌握", "已掌握", "最近一周", "最近一月"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, filterOptions);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemClickListener((parent, view, position, id) -> {
            filterWrongQuestions(position);
        });
    }
    
    private void setupClickListeners() {
        // 清除已掌握按钮
        clearMastered.setOnClickListener(v -> {
            clearMasteredItems();
        });
    }
    
    private void loadWrongQuestions() {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "正在加载错题...", Toast.LENGTH_SHORT).show();
        }
        // 从Supabase加载错题数据
        loadWrongQuestionsFromSupabase();
    }
    
    private void loadWrongQuestionsFromSupabase() {
        new Thread(() -> {
            try {
                // 查询wrong_questions表获取错题数据
                String result = supabaseClient.query("wrong_questions", "*", "limit=10");
                Log.d(TAG, "Wrong questions from Supabase: " + result);
                
                List<WrongQuestionAdapter.WrongQuestionItem> loadedWrongQuestions = parseWrongQuestionsFromSupabase(result);
                
                requireActivity().runOnUiThread(() -> {
                    if (loadedWrongQuestions != null && !loadedWrongQuestions.isEmpty()) {
                        wrongQuestions.clear();
                        wrongQuestions.addAll(loadedWrongQuestions);
                        wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
                        updateEmptyState();
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "成功加载 " + wrongQuestions.size() + " 道错题", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 没有错题数据，显示空状态
                        loadMockWrongQuestions();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load wrong questions from Supabase", e);
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "网络错误，显示示例错题", Toast.LENGTH_SHORT).show();
                    }
                    loadMockWrongQuestions();
                });
            }
        }).start();
    }
    
    private List<WrongQuestionAdapter.WrongQuestionItem> parseWrongQuestionsFromSupabase(String jsonResult) {
        try {
            List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestionList = new ArrayList<>();
            
            JSONArray jsonArray = new JSONArray(jsonResult);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                String questionPreview = obj.getString("title");
                if (questionPreview.length() > 50) {
                    questionPreview = questionPreview.substring(0, 50) + "...";
                }
                
                WrongQuestionAdapter.WrongQuestionItem wrongQuestion = new WrongQuestionAdapter.WrongQuestionItem(
                    obj.getString("id"),
                    questionPreview,
                    obj.optInt("wrong_count", 1),
                    "2023-12-17 14:30", // 模拟时间
                    obj.optString("category", "未分类"),
                    obj.optBoolean("is_mastered", false)
                );
                
                wrongQuestionList.add(wrongQuestion);
            }
            
            return wrongQuestionList;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse wrong questions JSON", e);
            return null;
        }
    }
    
    private void loadMockWrongQuestions() {
        // 模拟错题数据作为fallback
        wrongQuestions.clear();
        
        WrongQuestionAdapter.WrongQuestionItem wq1 = new WrongQuestionAdapter.WrongQuestionItem(
            "1", "下列哪个是Java的基本数据类型？String、Integer、int、ArrayList", 3,
            "2023-12-17 14:30", "Java基础", false);
            
        WrongQuestionAdapter.WrongQuestionItem wq2 = new WrongQuestionAdapter.WrongQuestionItem(
            "2", "Android中Activity的生命周期包括哪些方法？", 2,
            "2023-12-16 10:15", "Android开发", false);
            
        WrongQuestionAdapter.WrongQuestionItem wq3 = new WrongQuestionAdapter.WrongQuestionItem(
            "3", "数据结构中的二叉树有哪些遍历方式？", 1,
            "2023-12-15 16:45", "数据结构", true); // 已掌握
        
        wrongQuestions.add(wq1);
        wrongQuestions.add(wq2);
        wrongQuestions.add(wq3);
        
        wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
        updateEmptyState();
    }
    
    private void loadStatistics() {
        // 加载统计数据（暂时使用静态数据，后续从Supabase获取）
        if (wrongCountText != null) {
            wrongCountText.setText("24");
        }
        if (masteredCount != null) {
            masteredCount.setText("8");
        }
        if (reviewRate != null) {
            reviewRate.setText("75%");
        }
    }
    
    private void updateEmptyState() {
        if (wrongQuestions.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            wrongQuestionRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            wrongQuestionRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void filterWrongQuestions(int filterType) {
        List<WrongQuestionAdapter.WrongQuestionItem> filteredList = new ArrayList<>();
        
        switch (filterType) {
            case 0: // 全部错题
                filteredList.addAll(wrongQuestions);
                break;
            case 1: // 未掌握
                for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
                    if (!item.isMastered()) {
                        filteredList.add(item);
                    }
                }
                break;
            case 2: // 已掌握
                for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
                    if (item.isMastered()) {
                        filteredList.add(item);
                    }
                }
                break;
            case 3: // 最近一周
            case 4: // 最近一月
                // 这里简化处理，实际应该根据时间筛选
                filteredList.addAll(wrongQuestions);
                break;
        }
        
        wrongQuestionAdapter.setWrongQuestions(filteredList);
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "筛选出 " + filteredList.size() + " 道错题", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteWrongQuestion(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        // 从内存中删除
        wrongQuestions.remove(wrongQuestion);
        wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
        updateEmptyState();
        
        // 从Supabase中删除记录
        deleteWrongQuestionFromSupabase(wrongQuestion);
        
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "已删除错题", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteWrongQuestionFromSupabase(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        new Thread(() -> {
            try {
                // 根据题目ID删除错题记录
                String result = supabaseClient.delete("wrong_questions", wrongQuestion.getId());
                Log.d(TAG, "删除错题记录结果: " + result);
            } catch (Exception e) {
                Log.e(TAG, "删除错题记录失败", e);
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "删除失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    private void markAsMastered(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        // 更新内存中的状态
        wrongQuestion.setMastered(true);
        wrongQuestionAdapter.notifyDataSetChanged();
        
        // 更新Supabase中的记录
        updateWrongQuestionInSupabase(wrongQuestion);
        
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "标记为已掌握", Toast.LENGTH_SHORT).show();
        }
        loadStatistics(); // 刷新统计数据
    }
    
    private void updateWrongQuestionInSupabase(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        new Thread(() -> {
            try {
                // 更新错题记录
                JSONObject updateData = new JSONObject();
                updateData.put("is_mastered", wrongQuestion.isMastered());
                updateData.put("last_wrong_time", System.currentTimeMillis());
                
                String result = supabaseClient.update("wrong_questions", wrongQuestion.getId(), updateData.toString());
                Log.d(TAG, "更新错题记录结果: " + result);
            } catch (Exception e) {
                Log.e(TAG, "更新错题记录失败", e);
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "更新失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    private void clearMasteredItems() {
        List<WrongQuestionAdapter.WrongQuestionItem> masteredQuestions = new ArrayList<>();
        List<WrongQuestionAdapter.WrongQuestionItem> remainingQuestions = new ArrayList<>();
        
        for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
            if (item.isMastered()) {
                masteredQuestions.add(item);
            } else {
                remainingQuestions.add(item);
            }
        }
        
        int removedCount = masteredQuestions.size();
        
        if (removedCount == 0) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "没有已掌握的错题", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        // 从内存中删除
        wrongQuestions.clear();
        wrongQuestions.addAll(remainingQuestions);
        wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
        updateEmptyState();
        
        // 从Supabase中批量删除已掌握的错题
        deleteMasteredQuestionsFromSupabase(masteredQuestions);
        
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "已清除 " + removedCount + " 道已掌握错题", Toast.LENGTH_SHORT).show();
        }
        loadStatistics(); // 刷新统计数据
    }
    
    private void deleteMasteredQuestionsFromSupabase(List<WrongQuestionAdapter.WrongQuestionItem> masteredQuestions) {
        new Thread(() -> {
            try {
                // 批量删除已掌握的错题记录
                for (WrongQuestionAdapter.WrongQuestionItem question : masteredQuestions) {
                    String result = supabaseClient.delete("wrong_questions", question.getId());
                    Log.d(TAG, "删除已掌握错题记录结果: " + result);
                }
            } catch (Exception e) {
                Log.e(TAG, "批量删除已掌握错题记录失败", e);
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "删除失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次回到错题本时刷新数据
        loadStatistics();
    }
}