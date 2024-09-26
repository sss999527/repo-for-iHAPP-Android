package com.example.weibo_songshangshang.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.weibo_songshangshang.fragment.ProfileFragment;
import com.example.weibo_songshangshang.R;
import com.example.weibo_songshangshang.fragment.RecommendFragment;
import com.example.weibo_songshangshang.UserSessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FirstPageActivity extends AppCompatActivity {
    private UserSessionManager sessionManager;
    private Fragment recommendFragment; //推荐fragment
    private Fragment profileFragment; //个人资料fragment
    private Fragment activeFragment; //用于跟踪当前显示的Fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);

        //创建了两个fragment的实例
        recommendFragment = new RecommendFragment();
        profileFragment = new ProfileFragment();
        activeFragment = recommendFragment; //初始时显示recommend页面

        //获取FragmentManager实例，将推荐放进视图，将个人资料放进试图并隐藏分别打标签
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, recommendFragment, "RECOMMEND").commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment).commit();

        //底部导航栏设置点击事件
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) { //实现接口，用户点击某个导航项被调用
                if (item.getItemId() == R.id.navigation_recommend) { //menu中定义id，此处代表点击“推荐”
                    fragmentManager.beginTransaction().hide(activeFragment).show(recommendFragment).commit();
                    activeFragment = recommendFragment;
                    return true; //已被处理
                } else { //点击个人资料
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .show(profileFragment)
                            .commit();
                    activeFragment = profileFragment;
                    return true;
                }
            }
        });
        // 根据是否被选中设置自定义颜色
        bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.selector_bottom_nav));
        bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.selector_bottom_nav));
        bottomNavigationView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
}