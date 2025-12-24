package com.example.aitestbank.utils;

/**
 * 通用操作回调接口
 * 用于异步操作的统一回调处理
 */
public interface OperationCallback<T> {
    
    /**
     * 操作成功时调用
     * @param result 操作结果
     */
    void onSuccess(T result);
    
    /**
     * 操作失败时调用
     * @param error 错误信息
     */
    void onError(Exception error);
}