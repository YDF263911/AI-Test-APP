package com.example.aitestbank.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.aitestbank.service.ApiService;
import com.example.aitestbank.service.DeepSeekService;

import java.util.concurrent.TimeUnit;

/**
 * 网络工具类 - 配置网络请求和检查网络状态
 */
public class NetworkUtils {
    
    private static final String BASE_URL = "https://api.aitestbank.com/"; // 替换为实际API地址
    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1/";
    private static Retrofit retrofit = null;
    private static Retrofit deepSeekRetrofit = null;
    private static com.example.aitestbank.service.ApiService apiService = null;
    private static com.example.aitestbank.service.DeepSeekService deepSeekService = null;
    
    /**
     * 检查网络连接状态
     */
    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * 检查是否连接WiFi
     */
    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * 获取Retrofit实例
     */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // 日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // OkHttp客户端配置
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
            
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
    
    /**
     * 获取API服务实例
     */
    public static com.example.aitestbank.service.ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(com.example.aitestbank.service.ApiService.class);
        }
        return apiService;
    }
    
    /**
     * 获取DeepSeek Retrofit实例（带认证）
     */
    public static Retrofit getDeepSeekRetrofit() {
        if (deepSeekRetrofit == null) {
            // 日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // 认证拦截器
            okhttp3.Interceptor authInterceptor = chain -> {
                okhttp3.Request originalRequest = chain.request();
                okhttp3.Request.Builder requestBuilder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer sk-14d2ef0524cd478b882e255358bc2c26")
                    .header("Content-Type", "application/json");
                
                return chain.proceed(requestBuilder.build());
            };
            
            // OkHttp客户端配置
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
            
            deepSeekRetrofit = new Retrofit.Builder()
                .baseUrl(DEEPSEEK_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return deepSeekRetrofit;
    }
    
    /**
     * 获取DeepSeek服务实例
     */
    public static com.example.aitestbank.service.DeepSeekService getDeepSeekService() {
        if (deepSeekService == null) {
            deepSeekService = getDeepSeekRetrofit().create(com.example.aitestbank.service.DeepSeekService.class);
        }
        return deepSeekService;
    }
    
    /**
     * 获取网络类型描述
     */
    public static String getNetworkType(Context context) {
        if (context == null) {
            return "未知";
        }
        
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                switch (networkInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        return "WiFi";
                    case ConnectivityManager.TYPE_MOBILE:
                        return "移动网络";
                    default:
                        return "其他";
                }
            }
        }
        return "未连接";
    }
}