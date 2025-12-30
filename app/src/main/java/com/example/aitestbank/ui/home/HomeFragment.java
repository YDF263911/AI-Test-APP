package com.example.aitestbank.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.aitestbank.supabase.SimpleSupabaseClient;
import com.example.aitestbank.ui.adapter.CategoryAdapter;
import com.example.aitestbank.ui.question.QuestionActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页Fragment - 题库分类展示
 */
public class HomeFragment extends Fragment {
    
    private static final String TAG = "HomeFragment";
    private RecyclerView categoryRecyclerView;
    private TextView totalQuestionsText;
    private TextView accuracyRateText;
    private TextView wrongCountText;
    
    private CategoryAdapter categoryAdapter;
    private SimpleSupabaseClient supabaseClient;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        initSupabase();
        loadCategories();
        loadStatistics();
    }
    
    private void initViews(View view) {
        categoryRecyclerView = view.findViewById(R.id.category_recycler_view);
        totalQuestionsText = view.findViewById(R.id.tv_total_questions);
        accuracyRateText = view.findViewById(R.id.tv_accuracy);
        wrongCountText = view.findViewById(R.id.tv_wrong_count);
        
        // 设置统计数据的默认值和点击事件
        if (totalQuestionsText != null) {
            totalQuestionsText.setOnClickListener(v -> refreshData());
        }
        
        // 设置三个功能按钮的点击事件
        setupFunctionButtons(view);
    }
    
    private void initSupabase() {
        supabaseClient = SimpleSupabaseClient.getInstance();
        // SimpleSupabaseClient已经在Application中初始化过了
    }
    
    private void setupRecyclerView() {
        // 使用网格布局，每行2列
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categoryAdapter = new CategoryAdapter();
        categoryRecyclerView.setAdapter(categoryAdapter);
        
        // 设置分类点击事件
        categoryAdapter.setOnCategoryClickListener(category -> {
            // 跳转到题目列表页面
            Intent intent = new Intent(getActivity(), QuestionListActivity.class);
            intent.putExtra("category", category.getName());
            startActivity(intent);
        });
    }
    
    private void loadCategories() {
        // 首先尝试从Supabase加载真实数据
        loadCategoriesFromSupabase();
    }
    
    private void loadCategoriesFromSupabase() {
        new Thread(() -> {
            try {
                // 先测试连接
                boolean connectionOk = supabaseClient.testConnection();
                if (!connectionOk) {
                    throw new IOException("Supabase connection test failed");
                }
                
                // 查询questions表获取所有数据，然后在本地按category分组统计
                String result = supabaseClient.query("questions", "category", "limit=1000");
                Log.d(TAG, "Questions from Supabase: " + result);
                
                List<CategoryAdapter.Category> categories = parseCategoriesFromSupabase(result);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping UI update");
                    return;
                }
                
                if (categories != null && !categories.isEmpty()) {
                    // 成功获取到Supabase数据，更新UI
                    getActivity().runOnUiThread(() -> {
                        // 再次检查Fragment状态
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }
                        categoryAdapter.setCategories(categories);
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "成功加载 " + categories.size() + " 个分类", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Supabase数据为空，使用模拟数据
                    Log.w(TAG, "No categories found in Supabase, using mock data");
                    getActivity().runOnUiThread(() -> {
                        // 再次检查Fragment状态
                        if (!isAdded() || getActivity() == null) {
                            return;
                        }
                        loadMockCategories();
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load categories from Supabase", e);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping error UI update");
                    return;
                }
                
                // 网络错误或数据为空，使用模拟数据
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "暂无云端数据，显示示例数据", Toast.LENGTH_SHORT).show();
                    }
                    loadMockCategories();
                });
            }
        }).start();
    }
    
    /**
     * 获取分类的中文显示名称
     */
    private String getCategoryDisplayName(String category) {
        if (category == null || category.isEmpty()) {
            return "未分类";
        }
        
        // 分类映射表 - 与错题详情页面保持一致
        java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
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
        categoryMap.put("行测数量关系", "行测数量关系"); // 添加模拟数据中的分类
        categoryMap.put("数据结构", "数据结构"); // 添加模拟数据中的分类
        categoryMap.put("算法设计", "算法设计"); // 添加模拟数据中的分类
        
        return categoryMap.getOrDefault(category, category);
    }
    
    private List<CategoryAdapter.Category> parseCategoriesFromSupabase(String jsonResult) {
        try {
            List<CategoryAdapter.Category> categories = new ArrayList<>();
            
            // 解析JSON结果并按category分组统计
            JSONArray jsonArray = new JSONArray(jsonResult);
            java.util.Map<String, Integer> categoryCountMap = new java.util.HashMap<>();
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String category = obj.getString("category");
                categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0) + 1);
            }
            
            // 转换为Category列表，使用中文显示名称
            int index = 1;
            for (java.util.Map.Entry<String, Integer> entry : categoryCountMap.entrySet()) {
                String displayName = getCategoryDisplayName(entry.getKey());
                categories.add(new CategoryAdapter.Category(
                    String.valueOf(index++),
                    displayName,  // 使用转换后的中文名称
                    entry.getValue()
                ));
            }
            
            return categories;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse categories JSON", e);
            return null;
        }
    }
    
    private void loadMockCategories() {
        // 模拟数据作为fallback - 使用中文名称
        List<CategoryAdapter.Category> mockCategories = new ArrayList<>();
        mockCategories.add(new CategoryAdapter.Category("1", "Java基础", 150));
        mockCategories.add(new CategoryAdapter.Category("2", "行测数量关系", 200));
        mockCategories.add(new CategoryAdapter.Category("3", "Android开发", 120));
        mockCategories.add(new CategoryAdapter.Category("4", "数据结构与算法", 180));
        mockCategories.add(new CategoryAdapter.Category("5", "算法设计", 100));
        mockCategories.add(new CategoryAdapter.Category("6", "计算机网络", 90));
        
        categoryAdapter.setCategories(mockCategories);
    }
    
    private void loadStatistics() {
        // 从Supabase加载真实统计数据
        loadStatisticsFromSupabase();
    }
    
    private void loadStatisticsFromSupabase() {
        new Thread(() -> {
            try {
                // 1. 获取总题数
                int totalQuestions = getTotalQuestionsCount();
                
                // 2. 获取答题记录计算正确率
                double accuracyRate = getAccuracyRate();
                
                // 3. 获取错题数量
                int wrongCount = getWrongQuestionsCount();
                
                // 4. 获取学习天数
                int studyDays = getStudyDays();
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping statistics UI update");
                    return;
                }
                
                // 更新UI
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (totalQuestionsText != null) {
                        totalQuestionsText.setText(String.valueOf(totalQuestions));
                    }
                    if (accuracyRateText != null) {
                        accuracyRateText.setText(String.format("%.1f%%", accuracyRate));
                    }
                    if (wrongCountText != null) {
                        wrongCountText.setText(String.valueOf(wrongCount));
                    }
                    // 更新学习天数
                    TextView studyDaysText = getView() != null ? getView().findViewById(R.id.tv_study_days) : null;
                    if (studyDaysText != null) {
                        studyDaysText.setText(String.valueOf(studyDays));
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to load statistics from Supabase", e);
                
                // 检查Fragment是否仍然有效
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment is not attached, skipping error UI update");
                    return;
                }
                
                // 使用默认数据
                getActivity().runOnUiThread(() -> {
                    // 再次检查Fragment状态
                    if (!isAdded() || getActivity() == null) {
                        return;
                    }
                    
                    if (totalQuestionsText != null) {
                        totalQuestionsText.setText("0");
                    }
                    if (accuracyRateText != null) {
                        accuracyRateText.setText("0%");
                    }
                    if (wrongCountText != null) {
                        wrongCountText.setText("0");
                    }
                    TextView studyDaysText = getView() != null ? getView().findViewById(R.id.tv_study_days) : null;
                    if (studyDaysText != null) {
                        studyDaysText.setText("0");
                    }
                    
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "统计数据加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    private int getTotalQuestionsCount() {
        try {
            // 查询questions表总数
            String result = supabaseClient.query("questions", "count", "");
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) {
                return jsonArray.getJSONObject(0).getInt("count");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get total questions count", e);
        }
        return 0;
    }
    
    private double getAccuracyRate() {
        try {
            // 获取当前用户ID
            com.example.aitestbank.supabase.auth.AuthManager authManager =
                com.example.aitestbank.supabase.auth.AuthManager.getInstance(getContext());
            String userId = authManager.getCurrentUserId();

            // 查询当前用户的答题记录
            String filter = "user_id=eq." + userId;
            String result = supabaseClient.query("answer_records", "is_correct", filter);
            JSONArray jsonArray = new JSONArray(result);

            if (jsonArray.length() == 0) {
                return 0.0;
            }

            int totalAnswers = jsonArray.length();
            int correctAnswers = 0;

            for (int i = 0; i < totalAnswers; i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getBoolean("is_correct")) {
                    correctAnswers++;
                }
            }

            double accuracy = totalAnswers > 0 ? (correctAnswers * 100.0 / totalAnswers) : 0.0;
            Log.d(TAG, "计算正确率 (userId=" + userId + "): " + totalAnswers + "/" + correctAnswers + " = " + accuracy + "%");
            return accuracy;

        } catch (Exception e) {
            Log.e(TAG, "Failed to calculate accuracy rate", e);
        }
        return 0.0;
    }
    
    private int getWrongQuestionsCount() {
        try {
            // 获取当前用户ID
            com.example.aitestbank.supabase.auth.AuthManager authManager =
                com.example.aitestbank.supabase.auth.AuthManager.getInstance(getContext());
            String userId = authManager.getCurrentUserId();

            // 查询当前用户错题本中未掌握的错题数量
            String filter = "user_id=eq." + userId + "&is_mastered=eq.false";
            String result = supabaseClient.query("wrong_questions", "count", filter);
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) {
                int count = jsonArray.getJSONObject(0).getInt("count");
                Log.d(TAG, "获取错题数量 (userId=" + userId + "): " + count);
                return count;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get wrong questions count", e);
        }
        return 0;
    }
    
    private int getStudyDays() {
        try {
            // 获取当前用户ID
            com.example.aitestbank.supabase.auth.AuthManager authManager =
                com.example.aitestbank.supabase.auth.AuthManager.getInstance(getContext());
            String userId = authManager.getCurrentUserId();

            // 查询当前用户的学习天数（从user_profiles表）
            String filter = "id=eq." + userId;
            String result = supabaseClient.query("user_profiles", "study_days", filter);
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) {
                int studyDays = jsonArray.getJSONObject(0).optInt("study_days", 0);
                Log.d(TAG, "获取学习天数 (userId=" + userId + "): " + studyDays);
                return studyDays;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get study days", e);
        }
        return 0;
    }
    
    private void refreshData() {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "刷新数据中...", Toast.LENGTH_SHORT).show();
        }
        loadCategoriesFromSupabase();
    }
    
    private void setupFunctionButtons(View view) {
        // 随机练习按钮
        View randomPracticeCard = view.findViewById(R.id.random_practice_card);
        if (randomPracticeCard != null) {
            randomPracticeCard.setOnClickListener(v -> {
                if (!isAdded() || getActivity() == null) return;
                
                // 跳转到随机练习模式
                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                intent.putExtra("mode", "random");
                intent.putExtra("title", "随机练习");
                startActivity(intent);
            });
        }
        
        // 错题复习按钮
        View wrongQuestionCard = view.findViewById(R.id.wrong_question_card);
        if (wrongQuestionCard != null) {
            wrongQuestionCard.setOnClickListener(v -> {
                if (!isAdded() || getActivity() == null) return;
                
                // 检查是否有错题
                if (getWrongQuestionsCount() > 0) {
                    // 跳转到错题复习模式
                    Intent intent = new Intent(getActivity(), QuestionActivity.class);
                    intent.putExtra("mode", "wrong");
                    intent.putExtra("title", "错题复习");
                    startActivity(intent);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "暂无错题，先去刷题吧！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        // AI智能解析按钮
        View aiAnalysisCard = view.findViewById(R.id.ai_analysis_card);
        if (aiAnalysisCard != null) {
            aiAnalysisCard.setOnClickListener(v -> {
                if (!isAdded() || getActivity() == null) return;
                
                // 跳转到AI解析介绍页面或直接开始答题
                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                intent.putExtra("mode", "normal");
                intent.putExtra("title", "AI智能解析");
                intent.putExtra("enableAI", true);
                startActivity(intent);
                
                if (getContext() != null) {
                    Toast.makeText(getContext(), "AI解析功能已开启，答题后即可体验", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次回到首页时刷新数据
        loadStatistics();
    }
}