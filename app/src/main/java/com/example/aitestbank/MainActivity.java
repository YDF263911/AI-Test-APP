package com.example.aitestbank;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.aitestbank.ui.home.HomeFragment;
import com.example.aitestbank.ui.question.QuestionFragment;
import com.example.aitestbank.ui.wrong.WrongQuestionFragment;
import com.example.aitestbank.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        setupBottomNavigation();
        
        // 默认显示首页
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
        
        // 保留Supabase测试功能
        findViewById(R.id.supabase_test_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.aitestbank.SupabaseConnectionTest.class);
            startActivity(intent);
        });
        
        // 添加DeepSeek测试功能
        findViewById(R.id.deepseek_test_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.aitestbank.DeepSeekConnectionTest.class);
            startActivity(intent);
        });
    }
    
    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }
    
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_question) {
                replaceFragment(new QuestionFragment());
                return true;
            } else if (itemId == R.id.nav_wrong) {
                replaceFragment(new WrongQuestionFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
        
        // 设置默认选中项
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
    
    private void replaceFragment(Fragment fragment) {
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return; // 避免重复加载同一个Fragment
        }
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        
        currentFragment = fragment;
    }
}