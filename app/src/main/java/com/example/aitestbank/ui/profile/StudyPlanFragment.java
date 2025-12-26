package com.example.aitestbank.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aitestbank.R;
import com.example.aitestbank.model.StudyPlan;

/**
 * 学习计划Fragment - 简化版本，避免布局ID冲突
 */
public class StudyPlanFragment extends Fragment {
    
    private static final String TAG = "StudyPlanFragment";
    
    // UI组件 - 只保留存在的组件
    private ProgressBar dailyProgress;
    
    // 学习计划数据
    private StudyPlan studyPlan;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_plan, container, false);
        initViews(view);
        initData();
        setupUI();
        return view;
    }
    
    private void initViews(View view) {
        // 只初始化确实存在的视图组件
        try {
            dailyProgress = view.findViewById(R.id.daily_progress);
            Log.d(TAG, "成功找到daily_progress组件");
        } catch (Exception e) {
            Log.w(TAG, "某些视图组件未找到: " + e.getMessage());
        }
    }
    
    private void initData() {
        // 初始化学习计划数据
        studyPlan = new StudyPlan("user123", 50); // userId, dailyGoal
        studyPlan.setTodayCompleted(35);
        studyPlan.setConsecutiveDays(7);
        studyPlan.setTotalStudyDays(30);
    }
    
    private void setupUI() {
        // 设置基本UI - 简化版本
        if (dailyProgress != null) {
            int progress = studyPlan.getTodayProgress();
            dailyProgress.setProgress(progress);
        }
        
        // 显示提示信息
        if (getContext() != null) {
            Toast.makeText(getContext(), "学习计划功能开发中，敬请期待！", Toast.LENGTH_SHORT).show();
        }
    }
}