package com.example.aitestbank.supabase.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

/**
 * 用户认证管理器 - 简化版，注册后直接登录
 */
public class AuthManager {
    
    private static final String TAG = "AuthManager";
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_GUEST = "is_guest";
    
    private static AuthManager instance;
    private Context context;
    private SharedPreferences prefs;
    
    // Supabase配置
    private static final String SUPABASE_URL = "https://jypjsjbkspmsutmdvelq.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4";
    
    /**
     * 认证回调接口
     */
    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }
    
    /**
     * 用户注册并直接登录
     */
    public void signUp(String email, String password, String username, AuthCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "开始注册: " + email);
                
                // 生成用户ID
                String userId = UUID.randomUUID().toString();
                Log.d(TAG, "生成用户ID: " + userId);
                
                // 创建用户配置数据
                JSONObject userProfile = new JSONObject();
                userProfile.put("id", userId);
                userProfile.put("username", username);
                userProfile.put("email", email);
                userProfile.put("password", password); // 注意：实际应用中应该加密存储
                // 使用ISO格式的当前时间，适配PostgreSQL的TIMESTAMPTZ
                userProfile.put("created_at", java.time.Instant.now().toString());
                
                // 检查邮箱是否已注册
                String checkUrl = SUPABASE_URL + "/rest/v1/user_profiles?email=eq." + email;
                Log.d(TAG, "检查邮箱URL: " + checkUrl);
                String checkResponse = makeGetRequest(checkUrl);
                Log.d(TAG, "邮箱检查响应: " + checkResponse);
                
                if (!checkResponse.equals("[]") && !checkResponse.isEmpty()) {
                    Log.w(TAG, "邮箱已被注册: " + email);
                    callback.onError("该邮箱已被注册");
                    return;
                }
                
                // 插入用户配置到数据库
                String createUrl = SUPABASE_URL + "/rest/v1/user_profiles";
                Log.d(TAG, "创建用户URL: " + createUrl);
                Log.d(TAG, "用户数据: " + userProfile.toString());
                
                String createResponse = makePostRequest(createUrl, userProfile.toString());
                Log.d(TAG, "创建用户响应: " + createResponse);
                
                if (createResponse != null) {
                    // 注册成功，直接登录
                    Log.d(TAG, "注册成功，保存用户信息");
                    saveUserInfo(userId, email, username, false);
                    callback.onSuccess("注册成功，已自动登录");
                } else {
                    Log.e(TAG, "注册失败：响应为空");
                    callback.onError("注册失败，请重试");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Sign up error", e);
                callback.onError("注册错误: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 用户登录
     */
    public void signIn(String email, String password, AuthCallback callback) {
        new Thread(() -> {
            try {
                // 查询用户信息
                String queryUrl = SUPABASE_URL + "/rest/v1/user_profiles?email=eq." + email;
                String response = makeGetRequest(queryUrl);
                
                if (response.equals("[]") || response.isEmpty()) {
                    callback.onError("该邮箱未注册");
                    return;
                }
                
                JSONArray userArray = new JSONArray(response);
                JSONObject user = userArray.getJSONObject(0);
                
                // 验证密码
                String storedPassword = user.optString("password", "");
                if (!storedPassword.equals(password)) {
                    callback.onError("密码错误");
                    return;
                }
                
                // 登录成功
                String userId = user.getString("id");
                String username = user.getString("username");
                
                saveUserInfo(userId, email, username, false);
                callback.onSuccess("登录成功");
                
            } catch (Exception e) {
                Log.e(TAG, "Sign in error", e);
                callback.onError("网络错误，请检查连接");
            }
        }).start();
    }
    
    /**
     * 游客登录
     */
    public void signInAsGuest(AuthCallback callback) {
        new Thread(() -> {
            try {
                // 为游客生成唯一的用户ID
                String guestUserId = "guest_" + UUID.randomUUID().toString();
                String guestEmail = guestUserId + "@guest.aitestbank.com";
                String guestUsername = "游客用户";
                
                // 保存游客信息
                saveUserInfo(guestUserId, guestEmail, guestUsername, true);
                
                // 确保用户配置文件存在
                ensureUserProfileExists(guestUserId, guestUsername, guestEmail);
                
                callback.onSuccess("游客模式登录成功");
                
            } catch (Exception e) {
                Log.e(TAG, "Guest sign in error", e);
                callback.onError("游客登录失败");
            }
        }).start();
    }
    
    /**
     * 退出登录
     */
    public void signOut() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_IS_GUEST)
            .apply();
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 检查是否为游客
     */
    public boolean isGuest() {
        return prefs.getBoolean(KEY_IS_GUEST, false);
    }
    
    /**
     * 获取当前用户ID
     */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
    
    /**
     * 获取当前用户邮箱
     */
    public String getCurrentUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * 获取当前用户名
     */
    public String getCurrentUsername() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    
    /**
     * 保存用户信息
     */
    private void saveUserInfo(String userId, String email, String username, boolean isGuest) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, username)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, isGuest)
            .apply();
    }
    
    /**
     * 确保用户配置文件存在
     */
    private void ensureUserProfileExists(String userId, String username, String email) {
        new Thread(() -> {
            try {
                // 构建用户配置数据
                JSONObject userProfile = new JSONObject();
                userProfile.put("id", userId);
                userProfile.put("username", username);
                userProfile.put("email", email);
                // 使用ISO格式的当前时间，适配PostgreSQL的TIMESTAMPTZ
                userProfile.put("created_at", java.time.Instant.now().toString());
                
                // 检查用户配置文件是否已存在
                String checkUrl = SUPABASE_URL + "/rest/v1/user_profiles?id=eq." + userId;
                String checkResponse = makeGetRequest(checkUrl);
                
                if (checkResponse.equals("[]") || checkResponse.isEmpty()) {
                    // 用户配置文件不存在，创建新的
                    String createUrl = SUPABASE_URL + "/rest/v1/user_profiles";
                    makePostRequest(createUrl, userProfile.toString());
                    Log.d(TAG, "Created user profile for: " + userId);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to ensure user profile exists", e);
            }
        }).start();
    }
    
    /**
     * 发送GET请求
     */
    private String makeGetRequest(String urlString) throws Exception {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("GET");
        connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        
        int responseCode = connection.getResponseCode();
        java.io.InputStream inputStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    /**
     * 发送POST请求
     */
    private String makePostRequest(String urlString, String data) throws Exception {
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Prefer", "return=minimal");
        connection.setDoOutput(true);
        
        // 发送数据
        try (java.io.OutputStream os = connection.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        java.io.InputStream inputStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}