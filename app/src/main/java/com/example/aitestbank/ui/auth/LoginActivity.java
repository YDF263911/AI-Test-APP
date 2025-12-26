package com.example.aitestbank.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aitestbank.MainActivity;
import com.example.aitestbank.R;
import com.example.aitestbank.supabase.auth.AuthManager;
import com.google.android.material.button.MaterialButton;

/**
 * 用户登录Activity
 */
public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    
    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegisterLink, tvForgotPassword, tvGuestLogin;
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        try {
            // 初始化认证管理器
            authManager = AuthManager.getInstance(this);
            Log.d(TAG, "AuthManager初始化成功");
            
            // 检查是否已登录
            if (authManager.isLoggedIn()) {
                Log.d(TAG, "用户已登录，跳转到主界面");
                navigateToMain();
                return;
            }
            
            initViews();
            setupClickListeners();
            Log.d(TAG, "LoginActivity初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "LoginActivity初始化失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterLink = findViewById(R.id.tv_register_link);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvGuestLogin = findViewById(R.id.tv_guest_login);
    }
    
    private void setupClickListeners() {
        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> attemptLogin());
        
        // 注册链接点击事件
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // 忘记密码点击事件
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "忘记密码功能开发中...", Toast.LENGTH_SHORT).show();
        });
        
        // 游客登录点击事件
        tvGuestLogin.setOnClickListener(v -> {
            loginAsGuest();
        });
    }
    
    /**
     * 尝试登录
     */
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱地址");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("请输入有效的邮箱地址");
            return;
        }
        
        // 显示加载状态
        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");
        
        // 执行登录
        authManager.signIn(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("登录");
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("登录");
                    Toast.makeText(LoginActivity.this, "登录失败: " + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Login error: " + error);
                });
            }
        });
    }
    
    /**
     * 游客登录
     */
    private void loginAsGuest() {
        btnLogin.setEnabled(false);
        btnLogin.setText("游客登录中...");
        
        authManager.signInAsGuest(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("登录");
                    Toast.makeText(LoginActivity.this, "游客模式登录成功", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("登录");
                    Toast.makeText(LoginActivity.this, "游客登录失败: " + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Guest login error: " + error);
                });
            }
        });
    }
    
    /**
     * 跳转到主界面
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 从注册页面返回时清空密码
        etPassword.setText("");
    }
}