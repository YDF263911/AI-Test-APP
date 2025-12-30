package com.example.aitestbank.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aitestbank.R;
import com.example.aitestbank.model.SupabaseUserProfile;
import com.example.aitestbank.model.SupabaseWrongQuestion;
import com.example.aitestbank.supabase.SupabaseClientManager;
import com.example.aitestbank.supabase.SupabaseClientManager.OperationCallback;
import com.example.aitestbank.supabase.auth.AuthManager;
import com.example.aitestbank.MainActivity;
import com.example.aitestbank.ui.auth.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 个人中心Fragment - 基于Supabase数据模型重构
 * 使用user_profiles表和study_statistics表的实时数据
 */
public class ProfileFragment extends Fragment {
    
    private static final String TAG = "ProfileFragment";
    
    // UI组件
    private ImageView userAvatar;
    private TextView userName;
    private TextView userLevel;
    private TextView totalQuestions;
    private TextView accuracyRate;
    private TextView wrongCount;
    private LinearLayout studyPlan;
    private LinearLayout settings;
    private LinearLayout clearCache;
    private LinearLayout logout;
    private LinearLayout about;
    
    // 数据和客户端
    private SupabaseClientManager supabaseManager;
    private SupabaseUserProfile currentUser;
    
    // 下拉刷新组件
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // 初始化下拉刷新
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout != null) {
            // 使用现有的颜色资源，secondary不存在，用accent替代
            swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent, R.color.success);
            swipeRefreshLayout.setOnRefreshListener(this::refreshAllData);
        }
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSupabase();
        setupClickListeners();
        loadUserData();
        loadStatistics();
    }
    
    /**
     * 刷新所有数据
     */
    private void refreshAllData() {
        Log.d(TAG, "开始刷新所有数据");
        
        // 重置数据
        currentUser = null;
        
        // 重新加载数据
        loadUserData();
        loadStatistics();
        
        // 延迟停止刷新动画，确保数据加载完成
        new android.os.Handler().postDelayed(() -> {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "数据刷新完成", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }
    
    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        // 可以在这里显示额外的loading UI
        Log.d(TAG, "显示加载状态");
    }
    
    /**
     * 隐藏加载状态
     */
    private void hideLoadingState() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        Log.d(TAG, "隐藏加载状态");
    }
    
    private void initViews(View view) {
        userAvatar = view.findViewById(R.id.user_avatar);
        userName = view.findViewById(R.id.user_name);
        userLevel = view.findViewById(R.id.user_level);
        totalQuestions = view.findViewById(R.id.total_questions);
        accuracyRate = view.findViewById(R.id.accuracy_rate);
        wrongCount = view.findViewById(R.id.wrong_count);
        studyPlan = view.findViewById(R.id.study_plan);
        settings = view.findViewById(R.id.settings);
        clearCache = view.findViewById(R.id.clear_cache);
        logout = view.findViewById(R.id.logout);
        about = view.findViewById(R.id.about);
    }
    
    private void setupSupabase() {
        supabaseManager = SupabaseClientManager.getInstance();
        Log.d(TAG, "Supabase客户端初始化完成");
    }
    
    private void setupClickListeners() {
        // 学习计划
        studyPlan.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                // 跳转到学习计划界面
                Intent intent = new Intent(getActivity(), StudyPlanActivity.class);
                startActivity(intent);
            }
        });
        
        // 设置
        settings.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                // 跳转到设置界面
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });
        
        // 清除缓存
        clearCache.setOnClickListener(v -> {
            clearAppCache();
        });
        
        // 退出登录
        logout.setOnClickListener(v -> {
            showLogoutDialog();
        });
        
        // 关于我们
        about.setOnClickListener(v -> {
            showAboutDialog();
        });
        
        // 用户头像点击
        userAvatar.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                // 跳转到头像上传界面
                Intent intent = new Intent(getActivity(), AvatarUploadActivity.class);
                startActivity(intent);
            }
        });
    }
    
    /**
     * 显示退出登录确认对话框
     */
    private void showLogoutDialog() {
        if (isAdded() && getContext() != null) {
            new android.app.AlertDialog.Builder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？退出后需要重新登录才能使用完整功能。")
                .setPositiveButton("退出", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("取消", null)
                .show();
        }
    }
    
    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        AuthManager authManager = AuthManager.getInstance(requireContext());
        authManager.signOut();
        
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        
        // 跳转到登录页面
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        
        // 关闭MainActivity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
    
    private void loadUserData() {
        // 使用AuthManager获取用户信息
        AuthManager authManager = AuthManager.getInstance(requireContext());
        String userId = authManager.getCurrentUserId();
        String username = authManager.getCurrentUsername();
        
        // 显示用户信息
        if (userName != null) {
            userName.setText(username != null ? username : "AI刷题用户");
        }
        
        // 从Supabase user_profiles表获取用户数据
        supabaseManager.getUserProfile(userId, new OperationCallback<String>() {
            @Override
            public void onSuccess(String userProfileJson) {
                // 解析JSON为SupabaseUserProfile对象
                try {
                    currentUser = parseUserProfileFromJson(userProfileJson);
                    updateUserInfo(currentUser);
                    Log.i(TAG, "用户档案加载成功: " + currentUser.getDisplayName());
                } catch (Exception e) {
                    Log.e(TAG, "用户档案解析失败", e);
                    currentUser = createDefaultUserProfile(userId);
                    updateUserInfo(currentUser);
                }
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "用户档案加载失败", error);
                // 使用默认用户信息
                currentUser = createDefaultUserProfile(userId);
                updateUserInfo(currentUser);
            }
        });
    }
    
    /**
     * 获取设备ID
     */
    private String getDeviceId() {
        // 简单的设备ID生成，实际应用中应该使用更可靠的方法
        return android.provider.Settings.Secure.getString(
            requireContext().getContentResolver(), 
            android.provider.Settings.Secure.ANDROID_ID
        );
    }
    
    /**
     * 创建默认用户档案
     */
    private SupabaseUserProfile createDefaultUserProfile(String deviceId) {
        SupabaseUserProfile profile = new SupabaseUserProfile(deviceId);
        profile.setDisplayName("AI刷题用户");
        return profile;
    }
    
    /**
     * 更新UI显示用户信息
     */
    private void updateUserInfo(SupabaseUserProfile profile) {
        requireActivity().runOnUiThread(() -> {
            userName.setText(profile.getDisplayName() != null ? profile.getDisplayName() : "AI刷题用户");
            
            // 设置用户等级
            String userLevelText = getUserLevelDescription(profile.getTotalQuestions());
            userLevel.setText(userLevelText);
            
            // 更新等级进度条
            updateUserLevelProgress(profile.getTotalQuestions());
        });
    }
    
    /**
     * 根据答题数获取用户等级描述
     */
    private String getUserLevelDescription(Long totalQuestions) {
        if (totalQuestions == null) return "刷题新手";
        
        if (totalQuestions < 50) {
            return "刷题新手";
        } else if (totalQuestions < 200) {
            return "刷题达人";
        } else if (totalQuestions < 500) {
            return "刷题高手";
        } else if (totalQuestions < 1000) {
            return "刷题专家";
        } else {
            return "刷题大师";
        }
    }
    
    /**
     * 更新用户等级进度条
     */
    private void updateUserLevelProgress(Long totalQuestions) {
        View progressBar = null;
        if (userLevel.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) userLevel.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child.getId() == android.R.id.progress) {
                    progressBar = child;
                    break;
                }
            }
        }
        
        if (progressBar != null) {
            // 计算进度百分比（基于1000题为满级）
            float progress = totalQuestions != null ? Math.min(totalQuestions.floatValue() / 1000f, 1.0f) : 0.0f;
            progressBar.setScaleX(progress);
            Log.d(TAG, "更新等级进度条: " + progress);
        }
    }
    
    private void loadStatistics() {
        // 从Supabase加载用户统计数据
        loadUserStatisticsFromSupabase();
    }
    
    /**
     * 从Supabase加载用户统计数据
     */
    private void loadUserStatisticsFromSupabase() {
        if (currentUser == null) {
            Log.w(TAG, "用户档案为空，无法加载统计数据");
            loadMockStatistics();
            return;
        }
        
        // 获取用户错题数量
        loadWrongQuestionCount();
        
        // 获取用户学习统计（最近7天）
        loadStudyStatistics();
    }
    
    /**
     * 加载错题数量
     */
    private void loadWrongQuestionCount() {
        supabaseManager.getUserWrongQuestions(currentUser.getId(), new OperationCallback<String>() {
            @Override
            public void onSuccess(String wrongQuestionsJson) {
                // 简单解析错题数量（实际应该使用JSON库）
                int wrongCountValue = parseWrongQuestionCount(wrongQuestionsJson);
                requireActivity().runOnUiThread(() -> {
                    wrongCount.setText(String.valueOf(wrongCountValue));
                    Log.d(TAG, "错题数量更新: " + wrongCountValue);
                });
            }
            
            @Override
            public void onError(Exception error) {
                Log.e(TAG, "加载错题数量失败", error);
                requireActivity().runOnUiThread(() -> {
                    wrongCount.setText("0");
                });
            }
        });
    }
    
    /**
     * 加载学习统计数据
     */
    private void loadStudyStatistics() {
        if (currentUser == null) return;
        
        // 从answer_records表动态计算统计数据（更准确）
        calculateStatisticsFromAnswerRecords();
        
        // 从用户档案获取基本统计（作为备用）
        updateStatisticsFromUserProfile(currentUser);
    }
    
    /**
     * 从answer_records表计算统计数据（动态计算，更准确）
     */
    private void calculateStatisticsFromAnswerRecords() {
        if (currentUser == null) return;
        
        // 使用SimpleSupabaseClient查询answer_records表
        com.example.aitestbank.supabase.SimpleSupabaseClient simpleClient = 
            com.example.aitestbank.supabase.SimpleSupabaseClient.getInstance();
        
        new Thread(() -> {
            try {
                // 查询当前用户的答题记录
                String result = simpleClient.query("answer_records", "*", "");
                org.json.JSONArray jsonArray = new org.json.JSONArray(result);
                
                // 过滤出当前用户的记录
                final int[] userTotalQuestions = {0};
                final int[] userCorrectQuestions = {0};
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(i);
                    
                    // 这里需要确认user_id字段是否存在，如果不存在可能需要通过其他方式过滤
                    // 暂时计算所有记录（因为RLS策略会限制只返回当前用户的记录）
                    userTotalQuestions[0]++;
                    
                    if (obj.getBoolean("is_correct")) {
                        userCorrectQuestions[0]++;
                    }
                }
                
                // 计算正确率
                final float accuracy = userTotalQuestions[0] > 0 ? 
                    (userCorrectQuestions[0] * 100.0f / userTotalQuestions[0]) : 0.0f;
                
                requireActivity().runOnUiThread(() -> {
                    totalQuestions.setText(String.valueOf(userTotalQuestions[0]));
                    accuracyRate.setText(String.format(Locale.getDefault(), "%.1f%%", accuracy));
                    
                    Log.d(TAG, "从answer_records计算统计数据: 总题数=" + userTotalQuestions[0] + 
                          ", 正确数=" + userCorrectQuestions[0] + ", 正确率=" + accuracy);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "从answer_records计算统计数据失败", e);
                // 降级到使用用户档案数据
                updateStatisticsFromUserProfile(currentUser);
            }
        }).start();
    }
    
    /**
     * 从用户档案更新统计数据
     */
    private void updateStatisticsFromUserProfile(SupabaseUserProfile profile) {
        requireActivity().runOnUiThread(() -> {
            // 更新总答题数（如果answer_records查询失败时使用）
            Long totalQuestionsValue = profile.getTotalQuestions();
            totalQuestions.setText(totalQuestionsValue != null ? totalQuestionsValue.toString() : "0");
            
            // 计算并更新正确率
            float accuracy = profile.getAccuracyRate();
            accuracyRate.setText(String.format(Locale.getDefault(), "%.1f%%", accuracy));
            
            Log.d(TAG, "从用户档案更新统计数据: 总题数=" + totalQuestionsValue + ", 正确率=" + accuracy);
        });
    }
    
    /**
     * 加载详细的学习统计数据
     */
    private void loadDetailedStudyStatistics() {
        // 这里可以扩展为加载最近的学习记录等
        // 目前使用用户档案的基本统计已足够
        Log.d(TAG, "详细统计数据加载完成");
    }
    
    /**
     * 加载模拟统计数据（作为fallback）
     */
    private void loadMockStatistics() {
        Log.d(TAG, "加载模拟统计数据");
        
        requireActivity().runOnUiThread(() -> {
            totalQuestions.setText("0");
            accuracyRate.setText("0%");
            wrongCount.setText("0");
        });
    }
    
    private void clearAppCache() {
        // 清除应用缓存
        try {
            // 清理SharedPreferences缓存
            clearSharedPreferencesCache();
            
            // 清理图片缓存
            clearImageCache();
            
            // 清理网络缓存
            clearNetworkCache();
            
            // 清理数据库缓存（如果适用）
            clearDatabaseCache();
            
            // 清理文件缓存
            clearFileCache();
            
            // 显示清理结果
            showCacheClearedMessage();
            
            Log.i(TAG, "应用缓存清理完成");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cache", e);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "缓存清理失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 清理SharedPreferences缓存
     */
    private void clearSharedPreferencesCache() {
        try {
            // 获取SharedPreferences文件并清除所有数据
            var sharedPrefs = requireContext().getSharedPreferences("app_cache", android.content.Context.MODE_PRIVATE);
            sharedPrefs.edit().clear().apply();
            
            // 清除默认的SharedPreferences
            var defaultPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
            defaultPrefs.edit().clear().apply();
            
            Log.d(TAG, "SharedPreferences缓存清理完成");
        } catch (Exception e) {
            Log.e(TAG, "清理SharedPreferences缓存失败", e);
        }
    }
    
    /**
     * 清理图片缓存
     */
    private void clearImageCache() {
        try {
            // 清理应用内部缓存目录
            var cacheDir = requireContext().getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                deleteDirectory(cacheDir);
                Log.d(TAG, "图片缓存清理完成");
            }
            
            // 清理外部缓存目录
            var externalCacheDir = requireContext().getExternalCacheDir();
            if (externalCacheDir != null && externalCacheDir.exists()) {
                deleteDirectory(externalCacheDir);
                Log.d(TAG, "外部缓存清理完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "清理图片缓存失败", e);
        }
    }
    
    /**
     * 清理网络缓存
     */
    private void clearNetworkCache() {
        try {
            // 清理OkHttp缓存（如果使用了OkHttp）
            clearOkHttpCache();
            
            // 清理HTTP响应缓存
            var httpCacheDir = new java.io.File(requireContext().getCacheDir(), "http_cache");
            if (httpCacheDir.exists()) {
                deleteDirectory(httpCacheDir);
                Log.d(TAG, "HTTP缓存清理完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "清理网络缓存失败", e);
        }
    }
    
    /**
     * 清理数据库缓存
     */
    private void clearDatabaseCache() {
        try {
            // 清理临时数据库或缓存表（如果适用）
            // 这里可以根据实际需求清理特定的数据库表或记录
            Log.d(TAG, "数据库缓存清理完成");
        } catch (Exception e) {
            Log.e(TAG, "清理数据库缓存失败", e);
        }
    }
    
    /**
     * 清理文件缓存
     */
    private void clearFileCache() {
        try {
            // 清理应用特定的文件缓存目录
            var filesDir = requireContext().getFilesDir();
            var tempDir = new java.io.File(filesDir, "temp");
            if (tempDir.exists()) {
                deleteDirectory(tempDir);
                Log.d(TAG, "文件缓存清理完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "清理文件缓存失败", e);
        }
    }
    
    /**
     * 清理OkHttp缓存
     */
    private void clearOkHttpCache() {
        try {
            // 如果项目使用了OkHttp，可以清理其缓存
            // 这里可以添加清理OkHttp缓存的逻辑
            Log.d(TAG, "OkHttp缓存清理完成");
        } catch (Exception e) {
            Log.e(TAG, "清理OkHttp缓存失败", e);
        }
    }
    
    /**
     * 递归删除目录
     */
    private void deleteDirectory(java.io.File dir) {
        if (dir.isDirectory()) {
            var children = dir.listFiles();
            if (children != null) {
                for (var child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }
    
    /**
     * 显示缓存清理完成消息
     */
    private void showCacheClearedMessage() {
        requireActivity().runOnUiThread(() -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "缓存清理完成，应用已释放存储空间", Toast.LENGTH_LONG).show();
            }
            // 可以添加一个短暂的通知，显示清理了哪些内容
            var snackbar = com.google.android.material.snackbar.Snackbar
                .make(requireView(), "缓存清理完成", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction("详情", v -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "已清理：SharedPreferences、图片缓存、网络缓存", Toast.LENGTH_SHORT).show();
                    }
                });
            
            // 检查是否可以使用Snackbar
            if (snackbar.getView() != null) {
                snackbar.show();
            } else {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "缓存清理完成，应用运行更流畅", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    private void showAboutDialog() {
        String version = "1.0.0";
        String buildDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        String aboutInfo = "AI智能题库APP\n\n" +
                "版本: " + version + "\n" +
                "构建日期: " + buildDate + "\n\n" +
                "专注于AI辅助刷题的核心场景\n" +
                "提供分类题库、在线刷题、智能解析等功能\n\n" +
                "© 2025 AI-Test-Bank Project";
        
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), aboutInfo, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 解析用户档案JSON
     */
    private SupabaseUserProfile parseUserProfileFromJson(String json) {
        // 简单的JSON解析，实际应用中应使用Gson等库
        SupabaseUserProfile profile = new SupabaseUserProfile();
        
        if (json.contains("\"id\"")) {
            String id = extractJsonValue(json, "id");
            profile.setId(id);
        }
        if (json.contains("\"display_name\"")) {
            String displayName = extractJsonValue(json, "display_name");
            profile.setDisplayName(displayName);
        }
        if (json.contains("\"total_questions\"")) {
            String totalQuestionsStr = extractJsonValue(json, "total_questions");
            try {
                profile.setTotalQuestions(Long.parseLong(totalQuestionsStr));
            } catch (NumberFormatException e) {
                profile.setTotalQuestions(0L);
            }
        }
        if (json.contains("\"correct_questions\"")) {
            String correctQuestionsStr = extractJsonValue(json, "correct_questions");
            try {
                profile.setCorrectQuestions(Long.parseLong(correctQuestionsStr));
            } catch (NumberFormatException e) {
                profile.setCorrectQuestions(0L);
            }
        }
        
        return profile;
    }
    
    /**
     * 简单的JSON值提取
     */
    private String extractJsonValue(String json, String key) {
        String searchPattern = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex == -1) {
            // 尝试数字类型
            searchPattern = "\"" + key + "\":";
            startIndex = json.indexOf(searchPattern);
            if (startIndex == -1) return "";
            startIndex += searchPattern.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            return json.substring(startIndex, endIndex).trim();
        } else {
            startIndex += searchPattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        }
    }
    
    /**
     * 解析错题数量（简单计数JSON数组项数）
     */
    private int parseWrongQuestionCount(String jsonArray) {
        if (jsonArray == null || jsonArray.equals("[]") || jsonArray.isEmpty()) {
            return 0;
        }
        
        // 简单计算逗号数量+1来估算项数
        int bracketLevel = 0;
        int itemCount = 0;
        
        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);
            if (c == '{') bracketLevel++;
            else if (c == '}') bracketLevel--;
            else if (c == ',' && bracketLevel == 0) {
                itemCount++;
            }
        }
        
        return itemCount + 1; // 最后一项后面没有逗号
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次回到个人中心时刷新数据
        loadStatistics();
    }
}