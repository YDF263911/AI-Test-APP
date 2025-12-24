package com.example.aitestbank.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.aitestbank.utils.NetworkUtils;
import com.example.aitestbank.service.ApiService;

/**
 * 数据仓库基类 - 提供通用的数据访问和网络请求方法
 */
public class BaseRepository {
    
    protected Application application;
    protected ApiService apiService;
    
    public BaseRepository(Application application) {
        this.application = application;
        this.apiService = NetworkUtils.getApiService();
    }
    
    /**
     * 执行网络请求并返回LiveData
     */
    protected <T> LiveData<T> executeRequest(Call<T> call) {
        MutableLiveData<T> result = new MutableLiveData<>();
        
        // 检查网络连接
        if (!NetworkUtils.isNetworkConnected(application)) {
            Log.e("BaseRepository", "网络未连接");
            result.setValue(null);
            return result;
        }
        
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body());
                    Log.d("BaseRepository", "网络请求成功: " + call.request().url());
                } else {
                    Log.e("BaseRepository", "网络请求失败: " + response.code() + " " + response.message());
                    result.setValue(null);
                }
            }
            
            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Log.e("BaseRepository", "网络请求异常: " + t.getMessage());
                result.setValue(null);
            }
        });
        
        return result;
    }
    
    /**
     * 执行网络请求（带回调）
     */
    protected <T> void executeRequest(Call<T> call, ApiCallback<T> callback) {
        // 检查网络连接
        if (!NetworkUtils.isNetworkConnected(application)) {
            callback.onError("网络未连接");
            return;
        }
        
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d("BaseRepository", "网络请求成功: " + call.request().url());
                } else {
                    String errorMsg = "请求失败: " + response.code() + " " + response.message();
                    callback.onError(errorMsg);
                    Log.e("BaseRepository", errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<T> call, Throwable t) {
                String errorMsg = "请求异常: " + t.getMessage();
                callback.onError(errorMsg);
                Log.e("BaseRepository", errorMsg);
            }
        });
    }
    
    /**
     * API回调接口
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
    
    /**
     * 检查网络状态
     */
    protected boolean isNetworkAvailable() {
        return NetworkUtils.isNetworkConnected(application);
    }
    
    /**
     * 获取网络类型
     */
    protected String getNetworkType() {
        return NetworkUtils.getNetworkType(application);
    }
}