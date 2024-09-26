package com.example.weibo_songshangshang.activity;


import com.example.weibo_songshangshang.R;
import com.example.weibo_songshangshang.UserSessionManager;
import com.example.weibo_songshangshang.retrofit.ApiService;
import com.example.weibo_songshangshang.response.LoginRequest;
import com.example.weibo_songshangshang.response.LoginResponse;
import com.example.weibo_songshangshang.retrofit.RetrofitClient;
import com.example.weibo_songshangshang.response.SendCodeRequest;
import com.example.weibo_songshangshang.response.SendCodeResponse;
import com.example.weibo_songshangshang.response.UserInfoResponse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private EditText editTextPhone;
    private EditText editTextVerificationCode;
    private Button buttonLogin;
    private TextView buttonGetVerificationCode;
    private Button btBack; // 返回按钮
    private UserSessionManager sessionManager; // 用来管理登录信息
    private boolean isCodeValid = false; // 验证码是否有效
    private Handler handler = new Handler(); // 倒计时
    private String verificationToken = ""; // 存储验证码

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new UserSessionManager(this);

        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextVerificationCode = findViewById(R.id.edit_text_verification_code);
        buttonLogin = findViewById(R.id.button_login);
        buttonGetVerificationCode = findViewById(R.id.text_view_get_verification_code);
        btBack = findViewById(R.id.bt_back);

        editTextPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)}); // 11位手机号长度限制

        editTextPhone.addTextChangedListener(new TextWatcher() { // 手机号输入框设置文本变化监听器
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString(); // 获取文本
                if (!input.matches("[0-9]+")) { // 处理特殊情况
                    editTextPhone.setText(input.replaceAll("[^0-9]", ""));
                    editTextPhone.setSelection(editTextPhone.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 监听输入框内容变化
        editTextPhone.addTextChangedListener(inputWatcher);
        editTextVerificationCode.addTextChangedListener(inputWatcher);

        // 点击返回按钮调用系统默认的返回操作
        btBack.setOnClickListener(v -> onBackPressed());

        // 发验证码按钮点击事件
        buttonGetVerificationCode.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();
            // 获取手机号并检查长度，调用发送验证码请求
            if (phone.length() == 11) {
                requestVerificationCode(phone); // --->>>满足条件发送验证码请求
            } else {
                Toast.makeText(LoginActivity.this, "请输入有效的手机号", Toast.LENGTH_SHORT).show();
            }
        });

        // 登录按钮点击事件
        buttonLogin.setOnClickListener(v -> {
            String phone = editTextPhone.getText().toString().trim();
            String verificationCode = editTextVerificationCode.getText().toString().trim(); // 拿到手机号与验证码
            // 获取手机号和验证码，验证后调用登录逻辑
            if (phone.length() == 11 && !verificationCode.isEmpty()) {
                if (isCodeValid) { // code为200，有效
                    performLogin(phone, verificationCode); // 满足条件调用登录逻辑
                } else {
                    Toast.makeText(LoginActivity.this, "验证码已过期", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "请输入有效的手机号和验证码", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化时禁用登录按钮
        buttonLogin.setEnabled(false); //
//        buttonLogin.setBackgroundResource(R.drawable.disable_btn_background);
    }

    // 更新登录按钮状态的方法
    private void updateLoginButtonState() {
        String phone = editTextPhone.getText().toString().trim();
        String verificationCode = editTextVerificationCode.getText().toString().trim();

        if (phone.length() == 11 && !verificationCode.isEmpty()) {
            buttonLogin.setEnabled(true);
//            buttonLogin.setBackgroundResource(R.drawable.button_background); // 使用可点击背景
        } else {
            buttonLogin.setEnabled(false);
//            buttonLogin.setBackgroundResource(R.drawable.disable_btn_background); // 使用不可点击背景
        }
    }

    // TextWatcher 实现
    private TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateLoginButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void requestVerificationCode(String phone) {
        // --->>>创建ApiService实例
        ApiService apiService = RetrofitClient
                .getClient("https://hotfix-service-prod.g.mi.com") // 返回一个配置好的Retrofit实例，该实例知道所有请求的基础URL
                .create(ApiService.class); // Retrofit根据ApiService接口中的方法定义生成具体的HTTP请求
        SendCodeRequest request = new SendCodeRequest(phone); // 创建SendCodeRequest对象，传入手机号
        Call<SendCodeResponse> call = apiService.sendVerificationCode(request); // 调用接口方法发送请求，并返回一个Call对象
        call.enqueue(new Callback<SendCodeResponse>() { // 发异步请求
            @Override
            public void onResponse(Call<SendCodeResponse> call, Response<SendCodeResponse> response) { // 成功请求时调用
                if (response.isSuccessful() && response.body() != null) {
                    SendCodeResponse responseBody = response.body(); // --->>>获取响应体，放在定义好的bean中
                    if (responseBody.getCode() == 200 && responseBody.isData()) { // 响应码正确且有data(true)
                        Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        isCodeValid = true; // --->>>验证码有效，置成true。在登录按钮点击事件中决定是否调用登录方法
                        startCountdown(); // 倒计时
                    } else {
                        Toast.makeText(LoginActivity.this, "获取验证码失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "获取验证码失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SendCodeResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCountdown() {
        buttonGetVerificationCode.setEnabled(false);
        buttonGetVerificationCode.setTextColor(Color.GRAY); // 禁用并变灰获取验证码按钮
        new Thread(new Runnable() { // 新线程倒计时
            int countdown = 60;

            @Override
            public void run() { // 线程运行的方法
                while (countdown > 0) {
                    final int secondsLeft = countdown; // 获取剩余秒数
                    // --->>>在主线程更新“获取验证码”文本
                    handler.post(() -> buttonGetVerificationCode.setText("获取验证码（" + secondsLeft + "s）"));
                    countdown--;
                    try {
                        Thread.sleep(1000); // 线程睡1s，真正的计时器逻辑的完成
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                handler.post(() -> { // 完成while循环后
                    buttonGetVerificationCode.setEnabled(true);
                    buttonGetVerificationCode.setText("获取验证码"); // 重新可点击，文本被初始化
                    buttonGetVerificationCode.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
                });
            }

        }).start();
    }

    // 执行登录
    private void performLogin(String phone, String verificationCode) {
        ApiService apiService = RetrofitClient
                .getClient("https://hotfix-service-prod.g.mi.com")
                .create(ApiService.class);
        LoginRequest request = new LoginRequest(phone, verificationCode);
        Call<LoginResponse> call = apiService.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body(); // 拿到响应体
                    if (loginResponse.getCode() == 200) {
                        verificationToken = loginResponse.getData(); //获取到验证码保存起来
                        SharedPreferences sharedPreferences = getSharedPreferences("my_token", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();//定义一个editor用来修改SPF中的数据
                        editor.putString("token",verificationToken);//上面定义的editor保存了验证令牌(响应体验证码)并给了一个键对应
                        editor.apply(); //提交更改
                        fetchUserInfo(verificationToken); // --->>>响应码对了的话拿数据并调用方法获取用户信息
                    } else {
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 第三个请求，获得用户信息
    private void fetchUserInfo(String token) {
        //生成一个API实例
        ApiService apiService = RetrofitClient
                .getClient("https://hotfix-service-prod.g.mi.com") //传入URL，调用getclient方法，返回一个配置好的retrofit实例
                .create(ApiService.class);//调用retrofit的create方法，生成代理实例
        //利用这个API实例的.getUserInfo方法,传入token，返回一个call数据
        Call<UserInfoResponse> call = apiService.getUserInfo("Bearer " + token); //Call<UserInfoResponse> 代表一个HTTP请求
        call.enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfoResponse userInfoResponse = response.body(); // 响应体赋值给UserInfoResponse，保存三个用户信息
                    if (userInfoResponse.getCode() == 200) { //从保存的返回体中拿到code并判断是否为200
                        UserInfoResponse.UserData userData = userInfoResponse.getData();
                        sessionManager.login(userData.getUsername(), "1000 粉丝", userData.getAvatar()); // 放到sessionManager储存

                        // 登陆成功，跳转首页。login接收信息后更新isLoggedIn从而更新个人信息profile页面
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, FirstPageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
