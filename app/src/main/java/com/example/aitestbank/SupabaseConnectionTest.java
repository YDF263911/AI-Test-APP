package com.example.aitestbank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aitestbank.supabase.SimpleSupabaseClient;

import okhttp3.*;

import java.io.IOException;

/**
 * 独立的Supabase连接测试Activity
 */
public class SupabaseConnectionTest extends AppCompatActivity {
    
    private static final String TAG = "SupabaseTest";
    private static final String SUPABASE_URL = "https://jypjsjbkspmsutmdvelq.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4";
    
    private TextView statusText;
    private TextView resultText;
    private Button testBasicButton;
    private Button testWithKeyButton;
    private Button testQueryButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 简单的布局
        statusText = new TextView(this);
        resultText = new TextView(this);
        testBasicButton = new Button(this);
        testWithKeyButton = new Button(this);
        testQueryButton = new Button(this);
        
        statusText.setText("准备测试Supabase连接...");
        resultText.setText("等待测试结果...");
        testBasicButton.setText("测试服务端连接");
        testWithKeyButton.setText("测试API密钥连接");
        testQueryButton.setText("测试数据查询");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        
        layout.addView(statusText, params);
        layout.addView(testBasicButton, params);
        layout.addView(testWithKeyButton, params);
        layout.addView(testQueryButton, params);
        layout.addView(resultText, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
        
        setContentView(layout);
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        testBasicButton.setOnClickListener(v -> testBasicConnection());
        testWithKeyButton.setOnClickListener(v -> testConnectionWithKey());
        testQueryButton.setOnClickListener(v -> testDataQuery());
    }
    
    private void testBasicConnection() {
        statusText.setText("测试Supabase服务端连接...");
        resultText.setText("连接测试中...");
        
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                // 测试基础的服务器健康检查端点
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/")
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .get()
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    String result = response.body() != null ? response.body().string() : "无响应";
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (success) {
                            statusText.setText("✅ 服务端连接成功");
                            resultText.setText("Supabase服务端响应正常\n\nURL: " + SUPABASE_URL + "\n\n响应状态: " + response.code() + "\n\nAPI端点可访问");
                            showToast("服务端连接成功！");
                        } else {
                            statusText.setText("❌ 服务端连接失败");
                            resultText.setText("服务端连接失败\n\n状态码: " + response.code() + "\n错误信息: " + response.message() + "\n\n可能原因：\n1. 网络连接问题\n2. 服务器不可达\n3. URL配置错误");
                            showToast("服务端连接失败！");
                        }
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "连接测试异常", e);
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    statusText.setText("❌ 连接异常");
                    resultText.setText("连接发生异常：" + e.getMessage());
                    showToast("连接异常：" + e.getMessage());
                });
            }
        }).start();
    }
    
    private void testConnectionWithKey() {
        statusText.setText("测试API密钥连接...");
        resultText.setText("API密钥测试中...");
        
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/")
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .get()
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    String result = response.body() != null ? response.body().string() : "无响应";
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (success) {
                            statusText.setText("✅ API密钥连接成功");
                            resultText.setText("API密钥验证成功\n\n响应状态: " + response.code() + "\n\nAPI密钥有效");
                            showToast("API密钥连接成功！");
                        } else {
                            statusText.setText("❌ API密钥连接失败");
                            resultText.setText("API密钥验证失败\n\n状态码: " + response.code() + "\n错误信息: " + response.message() + "\n\n请检查API密钥是否正确");
                            showToast("API密钥连接失败！");
                        }
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "API密钥测试异常", e);
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    statusText.setText("❌ API密钥测试异常");
                    resultText.setText("API密钥测试发生异常：" + e.getMessage());
                    showToast("API密钥测试异常：" + e.getMessage());
                });
            }
        }).start();
    }
    
    private void testDataQuery() {
        statusText.setText("测试数据查询...");
        resultText.setText("查询测试中...");
        
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/questions?select=*&limit=5")
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .get()
                        .build();
                
                try (Response response = client.newCall(request).execute()) {
                    boolean success = response.isSuccessful();
                    String result = response.body() != null ? response.body().string() : "无响应";
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (success) {
                            statusText.setText("✅ 数据查询成功");
                            resultText.setText("数据查询成功！\n\n查询了questions表\n\n状态码: " + response.code() + "\n\n返回数据:\n" + result);
                            showToast("数据查询成功！");
                        } else {
                            statusText.setText("❌ 数据查询失败");
                            resultText.setText("数据查询失败\n\n状态码: " + response.code() + "\n错误信息: " + response.message() + "\n\n可能原因：\n1. 数据表不存在\n2. 没有执行SQL脚本\n3. 权限问题");
                            showToast("数据查询失败！");
                        }
                    });
                }
                
            } catch (Exception e) {
                Log.e(TAG, "数据查询测试异常", e);
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    statusText.setText("❌ 数据查询异常");
                    resultText.setText("数据查询发生异常：" + e.getMessage());
                    showToast("数据查询异常：" + e.getMessage());
                });
            }
        }).start();
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}