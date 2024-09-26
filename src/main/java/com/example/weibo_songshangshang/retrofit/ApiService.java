package com.example.weibo_songshangshang.retrofit;


import com.example.weibo_songshangshang.response.HomePageResponse;
import com.example.weibo_songshangshang.response.LoginRequest;
import com.example.weibo_songshangshang.response.LoginResponse;
import com.example.weibo_songshangshang.response.SendCodeRequest;
import com.example.weibo_songshangshang.response.SendCodeResponse;
import com.example.weibo_songshangshang.response.UserInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

// 定义Retrofit服务接口，用于声明API请求
public interface ApiService { // login与postAdapter调用

    // 发送验证码请求
    @POST("/weibo/api/auth/sendCode") // 指定POST请求的URL路径
    Call<SendCodeResponse> sendVerificationCode(@Body SendCodeRequest request); // 方法发送SendCodeRequest对象，返回Call对象封装的SendCodeResponse类型响应

    // 登录请求
    @POST("/weibo/api/auth/login") // POST请求的登录接口URL
    Call<LoginResponse> login(@Body LoginRequest request); // 发送LoginRequest对象，返回封装LoginResponse类型的响应Call对象

    // 获取用户信息请求
    @GET("/weibo/api/user/info") // GET请求的用户信息接口URL
    Call<UserInfoResponse> getUserInfo(@Header("Authorization") String token); // 添加"Authorization"请求头，值为token，返回UserInfoResponse类型的响应Call对象

    @POST("/weibo/like/up")
    Call<ApiResponse> likePost(@Header("Authorization") String token, @Body LikeRequest request);

    @POST("/weibo/like/down")
    Call<ApiResponse> unlikePost(@Header("Authorization") String token, @Body LikeRequest request);

    class LikeRequest {
        private long id;

        public LikeRequest(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    class ApiResponse {
        private int code;
        private String msg;
        private boolean data;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public boolean isData() {
            return data;
        }
    }


}

