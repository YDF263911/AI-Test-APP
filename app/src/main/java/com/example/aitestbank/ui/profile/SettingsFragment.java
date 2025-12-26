package com.example.aitestbank.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aitestbank.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {
    
    private static final String TAG = "SettingsFragment";
    
    // UI组件
    private Switch notificationSwitch;
    private Switch darkModeSwitch;
    private TextView cacheSizeText;
    private TextView versionText;
    
    // 设置状态（简化版）
    private boolean notificationsEnabled = true;
    private boolean darkModeEnabled = false;
    
    // 根视图
    private View rootView;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(rootView);
        initData();
        setupClickListeners();
        return rootView;
    }
    
    private void initViews(View view) {
        notificationSwitch = view.findViewById(R.id.notification_switch);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        cacheSizeText = view.findViewById(R.id.cache_size_text);
        versionText = view.findViewById(R.id.version_text);
        
        // 设置版本号
        versionText.setText("v1.0.0");
    }
    
    private void initData() {
        // 加载设置状态（默认启用）
        notificationSwitch.setChecked(notificationsEnabled);
        darkModeSwitch.setChecked(darkModeEnabled);
        
        // 计算缓存大小
        calculateCacheSize();
    }
    
    private void setupClickListeners() {
        // 通知开关
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notificationsEnabled = isChecked;
            Toast.makeText(getContext(), isChecked ? "已开启通知" : "已关闭通知", Toast.LENGTH_SHORT).show();
        });
        
        // 深色模式开关
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            darkModeEnabled = isChecked;
            Toast.makeText(getContext(), isChecked ? "已切换到深色模式" : "已切换到浅色模式", Toast.LENGTH_SHORT).show();
            // 这里可以重启Activity或应用主题
        });
        
        // 清除缓存
        rootView.findViewById(R.id.clear_cache_item).setOnClickListener(v -> {
            showClearCacheDialog();
        });
        
        // 隐私政策
        rootView.findViewById(R.id.privacy_policy_item).setOnClickListener(v -> {
            showPrivacyPolicy();
        });
        
        // 用户协议
        rootView.findViewById(R.id.user_agreement_item).setOnClickListener(v -> {
            showUserAgreement();
        });
        
        // 意见反馈
        rootView.findViewById(R.id.feedback_item).setOnClickListener(v -> {
            showFeedbackDialog();
        });
        
        // 检查更新
        rootView.findViewById(R.id.check_update_item).setOnClickListener(v -> {
            checkForUpdates();
        });
        
        // 关于我们
        rootView.findViewById(R.id.about_item).setOnClickListener(v -> {
            showAboutDialog();
        });
    }
    
    /**
     * 计算缓存大小
     */
    private void calculateCacheSize() {
        try {
            // 这里可以实现真实的缓存大小计算
            long cacheSize = getAppCacheSize();
            String sizeText = formatFileSize(cacheSize);
            cacheSizeText.setText(sizeText);
        } catch (Exception e) {
            cacheSizeText.setText("计算中...");
        }
    }
    
    /**
     * 获取应用缓存大小（简化实现）
     */
    private long getAppCacheSize() {
        // 实际项目中应该遍历各个缓存目录计算真实大小
        return 25 * 1024 * 1024; // 模拟25MB
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * 显示清除缓存确认对话框
     */
    private void showClearCacheDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("清除缓存")
            .setMessage("确定要清除应用缓存吗？这将删除临时文件和历史记录。")
            .setPositiveButton("确定", (dialog, which) -> {
                clearAppCache();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 清除应用缓存
     */
    private void clearAppCache() {
        try {
            // 清除SharedPreferences缓存
            // 清除图片缓存
            // 清除网络缓存
            // 这里调用ProfileFragment中的清除缓存方法
            
            Toast.makeText(getContext(), "缓存清除成功", Toast.LENGTH_SHORT).show();
            calculateCacheSize(); // 重新计算缓存大小
        } catch (Exception e) {
            Toast.makeText(getContext(), "缓存清除失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示隐私政策
     */
    private void showPrivacyPolicy() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("隐私政策")
            .setMessage("我们重视您的隐私保护：\n\n" +
                    "• 您的个人信息仅用于账户管理\n" +
                    "• 答题数据用于个性化学习推荐\n" +
                    "• 我们不会向第三方分享您的个人信息\n" +
                    "• 您可以随时删除账户和相关数据\n\n" +
                    "详细政策请访问我们的官网。")
            .setPositiveButton("确定", null)
            .show();
    }
    
    /**
     * 显示用户协议
     */
    private void showUserAgreement() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("用户协议")
            .setMessage("欢迎使用AI刷题APP：\n\n" +
                    "• 本软件仅供学习交流使用\n" +
                    "• 请遵守相关法律法规\n" +
                    "• 不得用于商业用途\n" +
                    "• 如有问题请联系客服\n\n" +
                    "使用即表示同意本协议。")
            .setPositiveButton("确定", null)
            .show();
    }
    
    /**
     * 显示意见反馈对话框
     */
    private void showFeedbackDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("意见反馈")
            .setMessage("如遇到问题或有改进建议，请联系我们：\n\n邮箱：support@aitestbank.com\nQQ群：123456789\n\n感谢您的支持！")
            .setPositiveButton("复制邮箱", (dialog, which) -> {
                // 复制邮箱到剪贴板
                Toast.makeText(getContext(), "邮箱地址已复制", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 检查更新
     */
    private void checkForUpdates() {
        Toast.makeText(getContext(), "正在检查更新...", Toast.LENGTH_SHORT).show();
        // 实际项目中应该调用应用商店API或服务器检查更新
        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(getContext(), "当前已是最新版本", Toast.LENGTH_SHORT).show();
        }, 1500);
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("关于我们")
            .setMessage("AI刷题APP v1.0.0\n\n" +
                    "一款智能化的考试练习应用\n\n" +
                    "功能特色：\n" +
                    "• AI智能组卷\n" +
                    "• 错题自动收集\n" +
                    "• 个性化学习计划\n" +
                    "• 多维度学习统计\n\n" +
                    "© 2025 AI Test Bank Team")
            .setPositiveButton("确定", null)
            .show();
    }
}