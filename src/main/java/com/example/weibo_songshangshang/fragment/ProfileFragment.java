package com.example.weibo_songshangshang.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.weibo_songshangshang.R;
import com.example.weibo_songshangshang.UserSessionManager;
import com.example.weibo_songshangshang.activity.FirstPageActivity;
import com.example.weibo_songshangshang.activity.LoginActivity;


public class ProfileFragment extends Fragment {

    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private TextView textViewFollowers;
    private TextView textViewLogout;
    private TextView textViewInfo; // 登陆后查看

    private UserSessionManager sessionManager; //UserSessionManager对象，用于管理用户的会话状态，如检查用户是否已登录，获取用户名和粉丝数。

    //Fragment的视图被创建时调用onCreateView方法
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 用LayoutInflater inflater显示布局
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        sessionManager = new UserSessionManager(getContext());

        //初始化视图组件
        imageViewAvatar = view.findViewById(R.id.image_view_avatar); //头像
        textViewUsername = view.findViewById(R.id.text_view_username); //用户名
        textViewFollowers = view.findViewById(R.id.text_view_followers); //粉丝
        textViewLogout = view.findViewById(R.id.log_out); //退出登录
        textViewInfo = view.findViewById(R.id.text_view_info); //登陆查看/没有新的动态

        updateProfile(); //调用updateProfile方法，更新个人信息显示

        //点击头像跳转登录页loginActivity
        imageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionManager.isLoggedIn()) { //确保没登陆，防止重复登录，因为该方法默认false，置成true无法执行
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        //退出登录
        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(); //调用退出方法
            }
        });

        return view;
    }

    // 三个方法，更新个人信息、登出与确保方法最新
    void updateProfile() {
        if (sessionManager.isLoggedIn()) {
//            imageViewAvatar.setImageResource(R.drawable.login_2);
            Glide.with(this)
                    .load(sessionManager.getAvatar())
                    .into(imageViewAvatar); // 更新头像
            textViewUsername.setText(sessionManager.getUsername());
            textViewFollowers.setText(sessionManager.getFollowers());
            textViewInfo.setText("你没有新的动态哦~");
            Log.d("传回来的数据", "是" + sessionManager.getUsername() + "," + sessionManager.getFollowers());
            textViewLogout.setVisibility(View.VISIBLE);

        } else {
            imageViewAvatar.setImageResource(R.drawable.login_1);
            textViewUsername.setText("请先登录");
            textViewFollowers.setText("点击头像去登录");
            textViewLogout.setVisibility(View.GONE);
        }
    }

    //退出方法，点击退出登录文本时调用
    private void logout() {
        sessionManager.logout(); //通过会话管理对象执行登出
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        updateProfile(); //--->>>重新更新显示，此时loggedIn重新回到false
        Intent intent = new Intent(getActivity(), FirstPageActivity.class);
        //--->>>改变activity启动时的行为
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfile();
    }
}