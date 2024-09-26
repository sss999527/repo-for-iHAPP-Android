package com.example.weibo_songshangshang.retrofit;


import com.example.weibo_songshangshang.response.HomePageResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiServiceRecommand {
    @GET("/weibo/homePage")
    Call<HomePageResponse>  getHomePage(@Query("current") int current, @Query("size") int size, @Header("Authorization") String token);
}