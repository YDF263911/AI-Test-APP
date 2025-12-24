package com.example.aitestbank;

import android.app.Application;

import com.example.aitestbank.utils.CacheUtils;
import com.example.aitestbank.supabase.SimpleSupabaseClient;

/**
 * 应用程序主类 - 初始化全局组件
 */
public class AITestBankApplication extends Application {
    
    private static AITestBankApplication instance;
    
    // Supabase配置 - AI-Test-Bank项目
    private static final String SUPABASE_URL = "https://jypjsjbkspmsutmdvelq.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4";
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // 初始化缓存工具
        CacheUtils.init(this);
        
        // 初始化Supabase客户端
        SimpleSupabaseClient supabaseClient = SimpleSupabaseClient.getInstance();
        supabaseClient.initialize(this, SUPABASE_URL, SUPABASE_ANON_KEY);
    }
    
    public static AITestBankApplication getInstance() {
        return instance;
    }
}