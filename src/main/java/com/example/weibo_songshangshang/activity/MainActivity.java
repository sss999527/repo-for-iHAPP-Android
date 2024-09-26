package com.example.weibo_songshangshang.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.example.weibo_songshangshang.fragment.MyFragment;
import com.example.weibo_songshangshang.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 检查用户是否已经同意过
                SharedPreferences sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                boolean hasAgreed = sharedPreferences.getBoolean("agreed", false); // 默认为false

                if (!hasAgreed) {
                    View view = findViewById(R.id.main_activity);
                    view.setBackgroundColor(Color.GRAY);
                    TextView tv_guess = findViewById(R.id.textViewSaySomething);
                    tv_guess.setTextColor(Color.BLACK);

                    // 用户尚未同意，加载并显示MyFragment
                    Fragment fragment = new MyFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.fragment_container, fragment);
                    fragmentTransaction.commit();
                } else {
                    // 用户已经同意，跳转到FirstPageActivity
                    Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 1000);
    }
}
