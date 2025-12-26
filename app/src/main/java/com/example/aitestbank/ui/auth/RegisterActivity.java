package com.example.aitestbank.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aitestbank.MainActivity;
import com.example.aitestbank.R;
import com.example.aitestbank.supabase.auth.AuthManager;
import com.google.android.material.button.MaterialButton;

/**
 * 用户注册Activity
 */
public class RegisterActivity extends AppCompatActivity {
    
    private static final String TAG = "RegisterActivity";
    
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbAgree;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;
    private ImageView tvBack;
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        authManager = AuthManager.getInstance(this);
        initViews();
        setupClickListeners();
        setupBackPressedCallback();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbAgree = findViewById(R.id.cb_agree);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);
        tvBack = findViewById(R.id.iv_back);
    }
    
    private void setupClickListeners() {
        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> attemptRegister());
        
        // 登录链接点击事件
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        
        // 返回按钮点击事件
        tvBack.setOnClickListener(v -> {
            finish();
        });
        
        // 用户协议点击事件
        TextView tvAgreement = findViewById(R.id.tv_agreement);
        if (tvAgreement != null) {
            tvAgreement.setOnClickListener(v -> {
                Toast.makeText(this, "用户协议功能开发中...", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    /**
     * 尝试注册
     */
    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱地址");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("请确认密码");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("请输入有效的邮箱地址");
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("密码至少需要6位");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
            return;
        }
        
        if (!cbAgree.isChecked()) {
            Toast.makeText(this, "请同意用户协议和隐私政策", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载状态
        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");
        
        // 执行注册
        try {
            authManager.signUp(email, password, username, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        try {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("注册");
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                            
                            // 注册成功后跳转到主界面
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "跳转错误", e);
                            btnRegister.setEnabled(true);
                            btnRegister.setText("注册");
                            Toast.makeText(RegisterActivity.this, "跳转失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("注册");
                        Toast.makeText(RegisterActivity.this, "注册失败: " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration error: " + error);
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "注册调用错误", e);
            btnRegister.setEnabled(true);
            btnRegister.setText("注册");
            Toast.makeText(this, "注册调用失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 设置返回按钮回调
     */
    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}