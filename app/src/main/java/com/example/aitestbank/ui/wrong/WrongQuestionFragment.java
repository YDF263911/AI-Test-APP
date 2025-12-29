package com.example.aitestbank.ui.wrong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

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
    private Spinner filterSpinner;
    private Button clearMastered;
    private View emptyState;
    private TextView reminderStatus;
    private com.google.android.material.switchmaterial.SwitchMaterial reminderSwitch;
    
    // 错题分析组件
    private TextView weakKnowledgePoints;
    private TextView categoryDistribution;
    private TextView recommendedPractice;
    private TextView analysisUpdateTime;
    
    // 数据和客户端
    private SimpleSupabaseClient supabaseClient;
    private WrongQuestionAdapter wrongQuestionAdapter;
    private List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestions = new ArrayList<>();
    
    // 错题分析数据
    private Map<String, Integer> categoryWrongCounts = new HashMap<>();
    private Map<String, Integer> knowledgePointErrors = new HashMap<>();
    private Map<Integer, Integer> difficultyDistribution = new HashMap<>();
    
    // 科目筛选数据
    private List<String> subjectList = new ArrayList<>();
    private String selectedSubject = "";
    
    // 广播接收器
    private BroadcastReceiver wrongQuestionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_WRONG_QUESTION_UPDATED".equals(intent.getAction())) {
                loadWrongQuestions(); // 重新加载列表
                loadStatistics();
                loadWrongQuestionAnalysis(); // 重新加载分析数据
            }
        }
    };

    // 排序类型常量
    private static final int SORT_BY_TIME_DESC = 0; // 按时间倒序
    private static final int SORT_BY_TIME_ASC = 1;  // 按时间正序
    private static final int SORT_BY_SUBJECT = 2;   // 按科目
    private static final int SORT_BY_MASTERY = 3;  // 按掌握状态（未掌握在前）
    
    // SharedPreferences 键名
    private static final String PREFS_NAME = "WrongQuestionPrefs";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";

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
        loadWrongQuestionAnalysis();
    }
    
private void initViews(View view) {
    wrongQuestionRecyclerView = view.findViewById(R.id.wrong_question_recycler_view);
    wrongCountText = view.findViewById(R.id.wrong_count_text);
    masteredCount = view.findViewById(R.id.mastered_count);
    reviewRate = view.findViewById(R.id.review_rate);
    filterSpinner = view.findViewById(R.id.filter_spinner);
    clearMastered = view.findViewById(R.id.clear_mastered);
    emptyState = view.findViewById(R.id.empty_state);
    reminderSwitch = view.findViewById(R.id.reminder_switch);
    
    // 初始化错题分析组件
    weakKnowledgePoints = view.findViewById(R.id.weak_knowledge_points);
    categoryDistribution = view.findViewById(R.id.category_distribution);
    recommendedPractice = view.findViewById(R.id.recommended_practice);
    analysisUpdateTime = view.findViewById(R.id.analysis_update_time);
    
    // 初始化复习提醒开关状态
    loadReminderSettings();
    
    // 设置复习提醒开关监听器
    reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        saveReminderSettings(isChecked);
        showReminderStatusToast(isChecked);
    });
    
    // 设置其他点击监听器
    setupClickListeners();
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
        // Spinner 的 entries 已在 XML 中通过 android:entries="@array/wrong_question_filter_options" 绑定
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterWrongQuestions(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // 什么都不选时保持原列表
            }
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
                
                // 获取分类和科目信息
                String category = obj.optString("category", "未分类");
                String subject = obj.optString("subject", "未分类");
                String displayCategory = getCategoryDisplayName(category);
                String displaySubject = getSubjectDisplayName(subject);
                
                // 获取时间戳
                long createdAt = obj.optLong("created_at", System.currentTimeMillis());
                long updatedAt = obj.optLong("updated_at", System.currentTimeMillis());
                
                WrongQuestionAdapter.WrongQuestionItem wrongQuestion = new WrongQuestionAdapter.WrongQuestionItem(
                    obj.getString("id"),
                    questionPreview,
                    obj.optInt("review_count", 1), // 使用正确的字段名
                    timeStr,
                    displayCategory,
                    obj.optBoolean("is_mastered", false),
                    obj.optInt("difficulty", 3),
                    createdAt,
                    updatedAt,
                    displayCategory,
                    displaySubject
                );
                
                wrongQuestionList.add(wrongQuestion);
            }
            
            return wrongQuestionList;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse wrong questions JSON", e);
            return new ArrayList<>();
        }
    }
    

    
    /**
     * 获取分类的中文显示名称
     */
    private String getCategoryDisplayName(String category) {
        if (category == null || category.isEmpty()) {
            return "未分类";
        }
        
        // 首先尝试从数据库的分类表中获取
        String displayName = getCategoryFromDatabase(category);
        if (displayName != null) {
            return displayName;
        }
        
        // 如果数据库中没有，使用硬编码的映射表作为后备
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("campus_recruitment", "校园招聘");
        categoryMap.put("Java基础", "Java基础");
        categoryMap.put("Python", "Python");
        categoryMap.put("操作系统", "操作系统");
        categoryMap.put("数据库/SQL", "数据库/SQL");
        categoryMap.put("数据结构与算法", "数据结构与算法");
        categoryMap.put("计算机网络", "计算机网络");
        categoryMap.put("Android开发", "Android开发");
        categoryMap.put("Java框架", "Java框架");
        categoryMap.put("前端框架", "前端框架");
        categoryMap.put("工具", "开发工具");
        categoryMap.put("数据库", "数据库");
        categoryMap.put("编程基础", "编程基础");
        categoryMap.put("网络", "网络");
        categoryMap.put("civil_service", "公务员考试");
        categoryMap.put("postgraduate", "考研");
        
        return categoryMap.getOrDefault(category, category);
    }
    
    /**
     * 从数据库查询分类名称
     */
    private String getCategoryFromDatabase(String categoryId) {
        try {
            // 异步查询数据库分类表
            String result = supabaseClient.query("question_categories", "name", "id=eq." + categoryId);
            if (result != null && !result.isEmpty() && !result.equals("[]")) {
                org.json.JSONArray jsonArray = new org.json.JSONArray(result);
                if (jsonArray.length() > 0) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(0);
                    return obj.optString("name", categoryId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query category from database: " + categoryId, e);
        }
        return null;
    }
    
    /**
     * 获取科目的中文显示名称
     */
    private String getSubjectDisplayName(String subject) {
        if (subject == null || subject.isEmpty()) {
            return "未分类";
        }
        
        // 首先尝试从数据库的科目表中获取
        String displayName = getSubjectFromDatabase(subject);
        if (displayName != null) {
            return displayName;
        }
        
        // 如果数据库中没有，使用硬编码的映射表作为后备
        Map<String, String> subjectMap = new HashMap<>();
        subjectMap.put("programming", "编程语言");
        subjectMap.put("algorithm", "算法与数据结构");
        subjectMap.put("database", "数据库");
        subjectMap.put("network", "计算机网络");
        subjectMap.put("operating_system", "操作系统");
        subjectMap.put("verbal_reasoning", "言语理解");
        subjectMap.put("quantitative_reasoning", "数量关系");
        subjectMap.put("logical_reasoning", "判断推理");
        subjectMap.put("mathematics", "数学");
        subjectMap.put("english", "英语");
        subjectMap.put("politics", "政治");
        
        return subjectMap.getOrDefault(subject, subject);
    }
    
    /**
     * 从数据库查询科目名称
     */
    private String getSubjectFromDatabase(String subjectId) {
        try {
            // 异步查询数据库科目表
            String result = supabaseClient.query("subjects", "name", "id=eq." + subjectId);
            if (result != null && !result.isEmpty() && !result.equals("[]")) {
                org.json.JSONArray jsonArray = new org.json.JSONArray(result);
                if (jsonArray.length() > 0) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(0);
                    return obj.optString("name", subjectId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query subject from database: " + subjectId, e);
        }
        return null;
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
                filteredList.addAll(getRecentWrongQuestions(7));
                break;
            case 4: // 最近一月
                filteredList.addAll(getRecentWrongQuestions(30));
                break;
            case 5: // 按科目筛选
                showSubjectSelectionDialog();
                return; // 不立即筛选，等待用户选择
        }
        
        wrongQuestionAdapter.setWrongQuestions(filteredList);
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "筛选出 " + filteredList.size() + " 道错题", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 获取最近N天的错题
     */
    private List<WrongQuestionAdapter.WrongQuestionItem> getRecentWrongQuestions(int days) {
        List<WrongQuestionAdapter.WrongQuestionItem> recentList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long timeThreshold = currentTime - (days * 24L * 60 * 60 * 1000);
        
        for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
            // 使用新的时间字段进行筛选
            if (item.getCreatedAt() >= timeThreshold) {
                recentList.add(item);
            }
        }
        
        return recentList;
    }
    
    /**
     * 显示科目选择对话框
     */
    private void showSubjectSelectionDialog() {
        if (subjectList.isEmpty()) {
            loadSubjectList();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择科目");
        
        // 添加"全部"选项
        String[] subjectsWithAll = new String[subjectList.size() + 1];
        subjectsWithAll[0] = "全部科目";
        for (int i = 0; i < subjectList.size(); i++) {
            subjectsWithAll[i + 1] = subjectList.get(i);
        }
        
        builder.setItems(subjectsWithAll, (dialog, which) -> {
            if (which == 0) {
                // 选择"全部科目"
                selectedSubject = "";
                filterBySubject("");
            } else {
                // 选择具体科目
                selectedSubject = subjectsWithAll[which];
                filterBySubject(selectedSubject);
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 按科目筛选错题
     */
    private void filterBySubject(String subject) {
        List<WrongQuestionAdapter.WrongQuestionItem> filteredList = new ArrayList<>();
        
        if (subject.isEmpty()) {
            // 显示全部错题
            filteredList.addAll(wrongQuestions);
        } else {
            // 按科目筛选
            for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
                if (subject.equals(item.getSubject())) {
                    filteredList.add(item);
                }
            }
        }
        
        wrongQuestionAdapter.setWrongQuestions(filteredList);
        if (isAdded() && getContext() != null) {
            String message = subject.isEmpty() ? "显示全部错题" : "筛选科目: " + subject;
            Toast.makeText(getContext(), message + " (" + filteredList.size() + "道错题)", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 加载科目列表
     */
    private void loadSubjectList() {
        // 从数据库获取科目列表
        new Thread(() -> {
            try {
                // 这里应该从Supabase获取科目数据，暂时使用硬编码
                subjectList.clear();
                subjectList.add("编程语言");
                subjectList.add("算法与数据结构");
                subjectList.add("数据库");
                subjectList.add("计算机网络");
                subjectList.add("操作系统");
                subjectList.add("言语理解");
                subjectList.add("数量关系");
                subjectList.add("判断推理");
                subjectList.add("数学");
                subjectList.add("英语");
                subjectList.add("政治");
                
                // 根据实际数据库中的错题科目进行去重
                Set<String> existingSubjects = new HashSet<>();
                for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
                    if (item.getSubject() != null && !item.getSubject().isEmpty()) {
                        existingSubjects.add(item.getSubject());
                    }
                }
                
                // 只保留实际存在的科目
                subjectList.retainAll(existingSubjects);
                
            } catch (Exception e) {
                Log.e(TAG, "加载科目列表失败", e);
            }
        }).start();
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

    private void sortWrongQuestions(int sortType) {
        if (wrongQuestions == null || wrongQuestions.isEmpty()) {
            return;
        }

        switch (sortType) {
            case SORT_BY_TIME_DESC:
                // 时间倒序：最新的在前
                wrongQuestions.sort((o1, o2) -> o2.getLastWrongDate().compareTo(o1.getLastWrongDate()));
                break;
            case SORT_BY_TIME_ASC:
                // 时间正序：最早的在前
                wrongQuestions.sort(Comparator.comparing(WrongQuestionAdapter.WrongQuestionItem::getLastWrongDate));
                break;
            case SORT_BY_SUBJECT:
                // 按学科字母顺序
                wrongQuestions.sort(Comparator.comparing(WrongQuestionAdapter.WrongQuestionItem::getKnowledgePoint));
                break;
            case SORT_BY_MASTERY:
                // 未掌握的在前
                wrongQuestions.sort((o1, o2) -> Boolean.compare(o1.isMastered(), o2.isMastered()));
                break;
            default:
                wrongQuestions.sort((o1, o2) -> o2.getLastWrongDate().compareTo(o1.getLastWrongDate()));
                break;
        }

        wrongQuestionAdapter.setWrongQuestions(wrongQuestions);
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "已排序", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteMasteredQuestionsFromSupabase(List<WrongQuestionAdapter.WrongQuestionItem> masteredQuestions) {
        new Thread(() -> {
            try {
                // 批量删除已掌握的错题记录
                // 注意：filter参数需要是完整的过滤条件，如 "id=eq.123"
                for (WrongQuestionAdapter.WrongQuestionItem question : masteredQuestions) {
                    String filter = "id=eq." + question.getId();
                    String result = supabaseClient.delete("wrong_questions", filter);
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
        // 注册广播接收器
        IntentFilter filter = new IntentFilter("ACTION_WRONG_QUESTION_UPDATED");
        if (getContext() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getContext().registerReceiver(wrongQuestionUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                getContext().registerReceiver(wrongQuestionUpdateReceiver, filter);
            }
        }
        // 每次回到错题本时刷新数据
        loadStatistics();
        // 同步数据到云端
        syncDataToCloud();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // 注销广播接收器
        if (getContext() != null) {
            getContext().unregisterReceiver(wrongQuestionUpdateReceiver);
        }
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
    
    /**
     * 从SharedPreferences加载复习提醒设置
     */
    private void loadReminderSettings() {
        if (getContext() == null) return;
        
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false); // 默认关闭
        reminderSwitch.setChecked(isEnabled);
    }
    
    /**
     * 保存复习提醒设置到SharedPreferences
     */
    private void saveReminderSettings(boolean isEnabled) {
        if (getContext() == null) return;
        
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMINDER_ENABLED, isEnabled);
        editor.putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * 显示复习提醒状态变更的提示
     */
    private void showReminderStatusToast(boolean isEnabled) {
        if (!isAdded() || getContext() == null) return;
        
        String message = isEnabled ? "已开启复习提醒" : "已关闭复习提醒";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 获取复习提醒是否开启
     */
    private boolean isReminderEnabled() {
        if (getContext() == null) return false;
        
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false);
    }
    
    /**
     * 获取上次同步时间
     */
    private long getLastSyncTime() {
        if (getContext() == null) return 0;
        
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0);
    }
    
    /**
     * 加载错题分析数据
     */
    private void loadWrongQuestionAnalysis() {
        new Thread(() -> {
            try {
                // 获取当前用户ID
                com.example.aitestbank.supabase.auth.AuthManager authManager = 
                    com.example.aitestbank.supabase.auth.AuthManager.getInstance(requireContext());
                String userId = authManager.getCurrentUserId();
                
                // 查询当前用户的所有错题数据
                String result = supabaseClient.query("wrong_questions", "*", 
                    "user_id=eq." + userId + "&is_mastered=eq.false&order=created_at.desc");
                
                List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestions = parseWrongQuestionsFromSupabase(result);
                
                // 分析错题数据
                analyzeWrongQuestions(wrongQuestions);
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load wrong question analysis", e);
                // 分析失败时显示默认信息
                requireActivity().runOnUiThread(() -> {
                    updateAnalysisUI("分析失败，请重试", "暂无数据", "暂无推荐");
                });
            }
        }).start();
    }
    
    /**
     * 分析错题数据
     */
    private void analyzeWrongQuestions(List<WrongQuestionAdapter.WrongQuestionItem> wrongQuestions) {
        if (wrongQuestions == null || wrongQuestions.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                updateAnalysisUI("暂无错题，无需分析", "所有分类都表现良好", "建议保持当前学习节奏");
            });
            return;
        }
        
        // 分析分类分布
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (WrongQuestionAdapter.WrongQuestionItem item : wrongQuestions) {
            String category = item.getKnowledgePoint();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        
        // 找出错题最多的分类（薄弱环节）
        String weakestCategory = findWeakestCategory(categoryCounts);
        String categoryDistributionText = buildCategoryDistributionText(categoryCounts);
        String recommendationText = buildRecommendationText(categoryCounts, weakestCategory);
        
        // 更新UI
        requireActivity().runOnUiThread(() -> {
            updateAnalysisUI(weakestCategory, categoryDistributionText, recommendationText);
        });
    }
    
    /**
     * 找出最薄弱的分类
     */
    private String findWeakestCategory(Map<String, Integer> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            return "暂无薄弱知识点";
        }
        
        String weakestCategory = null;
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                weakestCategory = entry.getKey();
            }
        }
        
        return weakestCategory != null ? "最薄弱：" + weakestCategory : "暂无明确薄弱环节";
    }
    
    /**
     * 构建分类分布文本
     */
    private String buildCategoryDistributionText(Map<String, Integer> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            return "暂无错题数据";
        }
        
        StringBuilder sb = new StringBuilder();
        int total = categoryCounts.values().stream().mapToInt(Integer::intValue).sum();
        
        // 按错题数量排序
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(categoryCounts.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 只显示前3个分类
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            if (count >= 3) break;
            
            int percentage = (int) Math.round((entry.getValue() * 100.0) / total);
            sb.append("• ").append(entry.getKey()).append(": ").append(percentage).append("%\n");
            count++;
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 构建推荐练习文本
     */
    private String buildRecommendationText(Map<String, Integer> categoryCounts, String weakestCategory) {
        if (categoryCounts.isEmpty()) {
            return "建议先完成一些练习，积累错题数据";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 根据错题分布给出针对性建议
        if (categoryCounts.size() == 1) {
            // 只有一个分类有错题
            String category = categoryCounts.keySet().iterator().next();
            sb.append("建议集中练习 ").append(category).append(" 相关题目");
        } else {
            // 多个分类有错题
            sb.append("建议优先级：\n");
            
            // 按错题数量排序
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(categoryCounts.entrySet());
            sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedEntries) {
                if (count >= 3) break;
                sb.append(count + 1).append(". ").append(entry.getKey()).append("\n");
                count++;
            }
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 更新错题分析UI
     */
    private void updateAnalysisUI(String weakPoints, String distribution, String recommendation) {
        if (weakKnowledgePoints != null) {
            weakKnowledgePoints.setText(weakPoints);
        }
        
        if (categoryDistribution != null) {
            categoryDistribution.setText(distribution);
        }
        
        if (recommendedPractice != null) {
            recommendedPractice.setText(recommendation);
        }
        
        if (analysisUpdateTime != null) {
            String time = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            analysisUpdateTime.setText(time + " 更新");
        }
    }

}