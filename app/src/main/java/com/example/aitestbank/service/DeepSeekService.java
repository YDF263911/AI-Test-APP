package com.example.aitestbank.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Map;

/**
 * DeepSeek API服务接口
 */
public interface DeepSeekService {
    
    /**
     * 发送聊天完成请求到DeepSeek API
     * @param requestBody 请求体
     * @return API响应
     */
    @POST("chat/completions")
    Call<DeepSeekResponse> chatCompletion(@Body Map<String, Object> requestBody);
    
    /**
     * DeepSeek API响应模型
     */
    class DeepSeekResponse {
        private String id;
        private String object;
        private long created;
        private String model;
        private Choice[] choices;
        private Usage usage;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }
        
        public long getCreated() { return created; }
        public void setCreated(long created) { this.created = created; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public Choice[] getChoices() { return choices; }
        public void setChoices(Choice[] choices) { this.choices = choices; }
        
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
        
        /**
         * 获取第一个选择的文本内容
         */
        public String getContent() {
            if (choices != null && choices.length > 0) {
                Message message = choices[0].getMessage();
                return message != null ? message.getContent() : "";
            }
            return "";
        }
    }
    
    class Choice {
        private int index;
        private Message message;
        private String finish_reason;
        
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
        
        public String getFinish_reason() { return finish_reason; }
        public void setFinish_reason(String finish_reason) { this.finish_reason = finish_reason; }
    }
    
    class Message {
        private String role;
        private String content;
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
        
        public int getPrompt_tokens() { return prompt_tokens; }
        public void setPrompt_tokens(int prompt_tokens) { this.prompt_tokens = prompt_tokens; }
        
        public int getCompletion_tokens() { return completion_tokens; }
        public void setCompletion_tokens(int completion_tokens) { this.completion_tokens = completion_tokens; }
        
        public int getTotal_tokens() { return total_tokens; }
        public void setTotal_tokens(int total_tokens) { this.total_tokens = total_tokens; }
    }
}