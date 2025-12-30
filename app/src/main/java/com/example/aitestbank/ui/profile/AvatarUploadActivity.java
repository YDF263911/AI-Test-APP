package com.example.aitestbank.ui.profile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aitestbank.R;
import com.example.aitestbank.supabase.auth.AuthManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AvatarUploadActivity extends AppCompatActivity {
    
    private static final String TAG = "AvatarUploadActivity";
    private static final int REQUEST_CODE_CAMERA = 1001;
    private static final int REQUEST_CODE_GALLERY = 1002;
    private static final String SUPABASE_URL = "https://jypjsjbkspmsutmdvelq.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp5cGpzamJrc3Btc3V0bWR2ZWxxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NzExODMsImV4cCI6MjA4MTQ0NzE4M30.gISbgOu7vUtdAPLpiriRrcOOqWuUljSh6SVIlQgtvA4";
    
    private ImageView ivAvatarPreview;
    private TextView tvSelectPhoto;
    private Button btnTakePhoto;
    private Button btnChooseFromGallery;
    private Button btnSave;
    private Button btnCancel;
    
    private Uri selectedImageUri;
    private File tempImageFile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_upload);
        
        // 设置ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("更换头像");
        }
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        ivAvatarPreview = findViewById(R.id.iv_avatar_preview);
        tvSelectPhoto = findViewById(R.id.tv_select_photo);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnChooseFromGallery = findViewById(R.id.btn_choose_from_gallery);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // 默认显示当前头像
        // ivAvatarPreview.setImageResource(R.drawable.default_avatar);
    }
    
    private void setupClickListeners() {
        // 拍照
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        });
        
        // 从相册选择
        btnChooseFromGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_GALLERY);
        });
        
        // 保存
        btnSave.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadAvatarToSupabase(selectedImageUri);
            } else {
                Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 取消
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA:
                    // 处理拍照结果
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        // 这里简化处理，实际应该获取拍摄的图片
                        selectedImageUri = data.getData();
                        ivAvatarPreview.setImageURI(selectedImageUri);
                        tvSelectPhoto.setText("已选择照片");
                    }
                    break;
                    
                case REQUEST_CODE_GALLERY:
                    // 处理相册选择结果
                    selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        ivAvatarPreview.setImageURI(selectedImageUri);
                        tvSelectPhoto.setText("已选择照片");
                    }
                    break;
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    /**
     * 上传头像到Supabase Storage
     */
    private void uploadAvatarToSupabase(Uri imageUri) {
        new Thread(() -> {
            try {
                // 1. 将Uri转换为File
                File imageFile = copyUriToFile(imageUri);
                
                if (imageFile == null || !imageFile.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "图片处理失败", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                // 2. 上传到Supabase Storage
                String fileName = "avatar_" + AuthManager.getInstance(this).getCurrentUserId() + "_" + System.currentTimeMillis() + ".jpg";
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/avatars/" + fileName;
                
                java.net.URL url = new java.net.URL(uploadUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                connection.setRequestProperty("Content-Type", "image/jpeg");
                connection.setDoOutput(true);
                
                // 上传文件
                try (FileOutputStream fileOut = new FileOutputStream(imageFile);
                     java.io.InputStream fileIn = new java.io.FileInputStream(imageFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                        connection.getOutputStream().write(buffer, 0, bytesRead);
                    }
                }
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode >= 200 && responseCode < 300) {
                    // 3. 获取公开URL
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/avatars/" + fileName;
                    
                    // 4. 更新user_profiles表
                    updateUserAvatar(publicUrl);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "上传失败: " + responseCode, Toast.LENGTH_SHORT).show());
                }
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "上传头像失败", e);
                runOnUiThread(() -> Toast.makeText(this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    
    /**
     * 将Uri复制到临时文件
     */
    private File copyUriToFile(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            
            if (inputStream == null) {
                return null;
            }
            
            // 创建临时文件
            File tempDir = getCacheDir();
            String fileName = "temp_avatar_" + System.currentTimeMillis() + ".jpg";
            File tempFile = new File(tempDir, fileName);
            
            // 复制文件
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            tempImageFile = tempFile;
            return tempFile;
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "复制文件失败", e);
            return null;
        }
    }
    
    /**
     * 更新用户头像URL
     */
    private void updateUserAvatar(String avatarUrl) {
        new Thread(() -> {
            try {
                String userId = AuthManager.getInstance(this).getCurrentUserId();
                
                // 使用SimpleSupabaseClient更新
                com.example.aitestbank.supabase.SimpleSupabaseClient supabaseClient = 
                    com.example.aitestbank.supabase.SimpleSupabaseClient.getInstance();
                
                org.json.JSONObject updateData = new org.json.JSONObject();
                updateData.put("avatar_url", avatarUrl);
                
                supabaseClient.update("user_profiles", updateData, "id=eq." + userId);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "头像更新成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                });
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "更新用户头像失败", e);
                runOnUiThread(() -> Toast.makeText(this, "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}