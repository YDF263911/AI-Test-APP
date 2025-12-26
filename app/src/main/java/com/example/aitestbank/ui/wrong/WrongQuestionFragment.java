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
import com.example.aitestbank.utils.OperationCallback;

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
    private TextView reminderStatus;
    private com.google.android.material.button.MaterialButton setReminderButton;
    
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
                showDeleteConfirmationDialog(wrongQuestion);
            }
            
            @Override
            public void onMarkMastered(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
                toggleMasteryStatus(wrongQuestion);
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
            showClearMasteredConfirmationDialog();
        });
        
        // 刷新按钮
        View fabReviewAll = getView().findViewById(R.id.fab_review_all);
        if (fabReviewAll != null) {
            fabReviewAll.setOnClickListener(v -> {
                refreshData();
            });
        }
        
        // 空状态中的开始刷题按钮
        View btnStartPractice = getView().findViewById(R.id.btn_start_practice);
        if (btnStartPractice != null) {
            btnStartPractice.setOnClickListener(v -> {
                // 跳转到刷题界面
                Toast.makeText(getContext(), "跳转到刷题界面", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void showClearMasteredConfirmationDialog() {
        int masteredCount = getMasteredCount();
        if (masteredCount == 0) {
            Toast.makeText(getContext(), "没有已掌握的错题", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("清除已掌握错题")
            .setMessage("确定要清除所有已掌握的错题吗？共" + masteredCount + "道错题。")
            .setPositiveButton("清除", (dialog, which) -> {
                clearMasteredItems();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private int getMasteredCount() {
        int count = 0;
        for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
            if (item.isMastered()) {
                count++;
            }
        }
        return count;
    }
    
    private void refreshData() {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "正在刷新数据...", Toast.LENGTH_SHORT).show();
        }
        loadWrongQuestions();
        loadStatistics();
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
                // 获取当前用户ID
                com.example.aitestbank.supabase.auth.AuthManager authManager = 
                    com.example.aitestbank.supabase.auth.AuthManager.getInstance(requireContext());
                String userId = authManager.getCurrentUserId();
                
                // 查询当前用户的错题数据，按创建时间倒序排列
                String result = supabaseClient.query("wrong_questions", "*", 
                    "user_id=eq." + userId + "&order=created_at.desc&limit=50");
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
                        wrongQuestions.clear();
                        wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
                        updateEmptyState();
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "暂无错题记录", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load wrong questions from Supabase", e);
                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "网络错误，无法加载错题数据", Toast.LENGTH_SHORT).show();
                    }
                    wrongQuestions.clear();
                    wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
                    updateEmptyState();
                });
            }
        }).start();
    }
    
    private List<WrongQuestionAdapter.WrongQuestionItem> parseWrongQuestionsFromSupabase(String jsonResult) {
        try {
            List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestionList = new ArrayList<>();
            
            if (jsonResult == null || jsonResult.isEmpty() || jsonResult.equals("[]")) {
                return wrongQuestionList;
            }
            
            JSONArray jsonArray = new JSONArray(jsonResult);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                // 使用正确的字段名
                String questionPreview = obj.optString("question_title", "无标题");
                if (questionPreview.length() > 50) {
                    questionPreview = questionPreview.substring(0, 50) + "...";
                }
                
                // 获取时间信息
                String timeStr = "未知时间";
                if (obj.has("created_at")) {
                    try {
                        long timestamp = obj.getLong("created_at");
                        timeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(timestamp));
                    } catch (Exception e) {
                        // 如果时间戳格式不对，尝试其他格式
                        timeStr = obj.optString("created_at", "未知时间");
                    }
                }
                
                WrongQuestionAdapter.WrongQuestionItem wrongQuestion = new WrongQuestionAdapter.WrongQuestionItem(
                    obj.getString("id"),
                    questionPreview,
                    obj.optInt("review_count", 1), // 使用正确的字段名
                    timeStr,
                    obj.optString("category", "未分类"),
                    obj.optBoolean("is_mastered", false)
                );
                
                wrongQuestionList.add(wrongQuestion);
            }
            
            return wrongQuestionList;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse wrong questions JSON", e);
            return new ArrayList<>();
        }
    }
    

    
    private void loadStatistics() {
        // 从Supabase加载统计数据
        new Thread(() -> {
            try {
                // 获取当前用户ID
                com.example.aitestbank.supabase.auth.AuthManager authManager = 
                    com.example.aitestbank.supabase.auth.AuthManager.getInstance(requireContext());
                String userId = authManager.getCurrentUserId();
                
                // 查询当前用户错题总数
                String totalResult = supabaseClient.query("wrong_questions", "count", 
                    "user_id=eq." + userId);
                JSONArray totalArray = new JSONArray(totalResult);
                int totalCount = totalArray.length() > 0 ? totalArray.getJSONObject(0).optInt("count", 0) : 0;
                
                // 查询当前用户已掌握的错题数
                String masteredResult = supabaseClient.query("wrong_questions", "count", 
                    "user_id=eq." + userId + "&is_mastered=eq.true");
                JSONArray masteredArray = new JSONArray(masteredResult);
                int masteredCountValue = masteredArray.length() > 0 ? masteredArray.getJSONObject(0).optInt("count", 0) : 0;
                
                // 计算复习率
                int reviewRateValue = totalCount > 0 ? (masteredCountValue * 100 / totalCount) : 0;
                
                final int finalTotalCount = totalCount;
                final int finalMasteredCount = masteredCountValue;
                final int finalReviewRate = reviewRateValue;
                
                requireActivity().runOnUiThread(() -> {
                    if (wrongCountText != null) {
                        wrongCountText.setText(String.valueOf(finalTotalCount));
                    }
                    if (masteredCount != null) {
                        masteredCount.setText(String.valueOf(finalMasteredCount));
                    }
                    if (reviewRate != null) {
                        reviewRate.setText(finalReviewRate + "%");
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load statistics from Supabase", e);
                // 如果加载失败，显示空数据
                requireActivity().runOnUiThread(() -> {
                    if (wrongCountText != null) {
                        wrongCountText.setText("0");
                    }
                    if (masteredCount != null) {
                        masteredCount.setText("0");
                    }
                    if (reviewRate != null) {
                        reviewRate.setText("0%");
                    }
                });
            }
        }).start();
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
    
    private void toggleMasteryStatus(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        // 切换掌握状态
        boolean newStatus = !wrongQuestion.isMastered();
        wrongQuestion.setMastered(newStatus);
        wrongQuestionAdapter.notifyDataSetChanged();
        
        // 更新Supabase中的记录
        updateWrongQuestionInSupabase(wrongQuestion);
        
        if (isAdded() && getContext() != null) {
            String message = newStatus ? "标记为已掌握" : "标记为未掌握";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        loadStatistics(); // 刷新统计数据
    }
    
    private void showDeleteConfirmationDialog(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("删除错题")
            .setMessage("确定要删除这道错题吗？此操作不可撤销。")
            .setPositiveButton("删除", (dialog, which) -> {
                deleteWrongQuestion(wrongQuestion);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void showMasteryToggleDialog(WrongQuestionAdapter.WrongQuestionItem wrongQuestion) {
        String message = wrongQuestion.isMastered() ? 
            "确定要将此错题标记为未掌握吗？" : 
            "确定要将此错题标记为已掌握吗？";
            
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("修改掌握状态")
            .setMessage(message)
            .setPositiveButton("确定", (dialog, which) -> {
                toggleMasteryStatus(wrongQuestion);
            })
            .setNegativeButton("取消", null)
            .show();
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
        // 同步数据到云端
        syncDataToCloud();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // 离开界面时确保数据同步
        syncDataToCloud();
    }
    
    private void syncDataToCloud() {
        if (supabaseClient == null || wrongQuestions.isEmpty()) {
            return;
        }
        
        supabaseClient.syncWrongQuestionsToCloud(wrongQuestions, new OperationCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "数据同步成功: " + result);
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "数据同步失败", error);
            }
        });
    }
    
    /**
     * 手动同步数据
     */
    private void manualSyncData() {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "正在同步数据...", Toast.LENGTH_SHORT).show();
        }
        
        syncDataToCloud();
        
        // 同时从云端拉取最新数据
        loadWrongQuestions();
    }
}