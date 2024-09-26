package com.example.weibo_songshangshang.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weibo_songshangshang.R;
import com.example.weibo_songshangshang.activity.LoginActivity;
import com.example.weibo_songshangshang.recycler.PostAdapter;
import com.example.weibo_songshangshang.response.HomePageResponse;
import com.example.weibo_songshangshang.response.PostInfo;
import com.example.weibo_songshangshang.retrofit.ApiServiceRecommand;
import com.example.weibo_songshangshang.retrofit.RecommendRetrofit;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;


public class RecommendFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<PostInfo> postList;
    private ApiServiceRecommand apiService;
    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean refreshing = false;
    private TextView tvNoMoreData;
    private TextView tvNoNetwork;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvNoMoreData = view.findViewById(R.id.tv_no_more_data);
        tvNoNetwork = view.findViewById(R.drawable.nonetwork);

        //初始化列表与适配器，设置上布局管理器与适配器
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, getContext());
        recyclerView.setAdapter(postAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //获取API实例用于网络需求
        apiService = RecommendRetrofit
                .getRetrofitInstance()
                .create(ApiServiceRecommand.class); //定义请求

        //下拉调用refreshPosts方法
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && dy > 0 && !refreshing) {
                    loadMorePosts(); //上拉触发loadmorePosts方法实现上拉加载更多
                }
            }
        });

        refreshPosts();

        return view;
    }

    private void refreshPosts() {
        currentPage = 1;
        postList.clear();
        refreshing = true; //设置刷新状态为真
        fetchPosts();
    }

    private void loadMorePosts() {
        // 不需要实现该方法，因为不需要无限添加数据
        // 可以不做任何操作或者显示提示消息
        Toast.makeText(getContext(), "无更多内容", Toast.LENGTH_SHORT).show();
    }

    private void fetchPosts() {
        String token = ""; // 获取实际的Token
        // 发请求代码
        apiService.getHomePage(currentPage, pageSize, token).enqueue(new Callback<HomePageResponse>() {
            @Override
            public void onResponse(Call<HomePageResponse> call, Response<HomePageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HomePageResponse weiboResponse = response.body();

                    // 打乱
                    if (weiboResponse.getCode() == 200) {
                        List<PostInfo> newPostList = weiboResponse.getData().getRecords();
                        // 先缓存新数据
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                        editor.putString("postList", new Gson().toJson(newPostList));
                        editor.apply();

                        // 将已有数据和新数据分开打乱
                        List<PostInfo> tempOldPostList = new ArrayList<>(postList);
                        List<PostInfo> tempNewPostList = new ArrayList<>(newPostList);

                        Collections.shuffle(tempOldPostList);
                        Collections.shuffle(tempNewPostList);

                        // 合并打乱后的数据
                        List<PostInfo> tempPostList = new ArrayList<>();
                        tempPostList.addAll(tempOldPostList);
                        tempPostList.addAll(tempNewPostList);

                        // 更新整个列表
                        postList.clear();
                        postList.addAll(tempPostList);
                        postAdapter.notifyDataSetChanged();
                        tvNoMoreData.setVisibility(newPostList.isEmpty() ? View.VISIBLE : View.GONE);


                    } else if (weiboResponse.getCode() == 204) {
                        Toast.makeText(getContext(), "无更多内容", Toast.LENGTH_SHORT).show();
                    } else if (weiboResponse.getCode() == 403) {
                        // Token过期或无效，处理逻辑
                        handleTokenExpiration();
                    }
                } else {
                    tvNoNetwork.setVisibility(View.VISIBLE);
                }
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false; //刷新状态为false，避免重复调用陷入死循环
            }

            @Override
            public void onFailure(Call<HomePageResponse> call, Throwable t) {
                tvNoNetwork.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false;
            }
        });
    }

    //token过期，跳转到登录页页面
    private void handleTokenExpiration() {
        // 删除本地存储的token
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE).edit();
        editor.remove("token");
        editor.apply();

        // 将登录状态标记为未登录
        // 根据你的应用逻辑，可能需要使用SharedPreferences或者其他方式来标记登录状态

        // 跳转到登录页面
        // 根据你的应用逻辑，启动登录页面Activity或者Fragment，并提示用户重新登录
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        startActivity(loginIntent);
        getActivity().finish(); // 可选，关闭当前页面或清理堆栈
    }
}
