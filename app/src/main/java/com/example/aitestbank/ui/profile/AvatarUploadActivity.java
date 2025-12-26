package com.example.aitestbank.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aitestbank.R;

import java.util.ArrayList;

public class AvatarUploadActivity extends AppCompatActivity {
    
    private static final String TAG = "AvatarUploadActivity";
    private static final int REQUEST_CODE_CAMERA = 1001;
    private static final int REQUEST_CODE_GALLERY = 1002;
    
    private ImageView ivAvatarPreview;
    private TextView tvSelectPhoto;
    private Button btnTakePhoto;
    private Button btnChooseFromGallery;
    private Button btnSave;
    private Button btnCancel;
    
    private Uri selectedImageUri;
    
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
                // 这里应该上传图片到服务器
                Toast.makeText(this, "头像更新成功", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
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
}