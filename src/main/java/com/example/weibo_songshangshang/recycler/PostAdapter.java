package com.example.weibo_songshangshang.recycler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.weibo_songshangshang.R;
import com.example.weibo_songshangshang.UserSessionManager;
import com.example.weibo_songshangshang.activity.ImageViewerActivity;
import com.example.weibo_songshangshang.activity.VideoActivity;
import com.example.weibo_songshangshang.fragment.ProfileFragment;
import com.example.weibo_songshangshang.response.PostInfo;
import com.example.weibo_songshangshang.retrofit.ApiService;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<PostInfo> postList;
    private Context context;
    private UserSessionManager sessionManager; //用户会话管理器
    private ApiService apiService;//请求接口

    private static final String TAG = PostAdapter.class.getSimpleName();
    public PostAdapter(List<PostInfo> postList, Context context) {
        this.postList = postList;
        this.context = context;
        this.sessionManager = new UserSessionManager(context); // 初始化UserSessionManager
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://hotfix-service-prod.g.mi.com")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class); //配置retrofit与api类用来完成网络需求

    }

    private String getToken() { //方法，从SharedPreferences取出存储的令牌
        SharedPreferences sharedPreferences = context.getSharedPreferences("my_token", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,
                parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostInfo post = postList.get(position); //拿到整个数据
        Glide.with(context)
                .load(post.getAvatar())
                .transform(new CircleCrop()) // 裁剪圆
                .into(holder.ivAvatar); //glide加载头像到holder

        // 绑定数据到视图
        holder.tvUsername.setText(post.getUsername());
        holder.tvTitle.setText(post.getTitle()); //加载用户名与标题


        // 处理删除按钮点击事件
        holder.imgDelete.setOnClickListener(v -> {
            postList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, postList.size());
            // 执行删除操作
        });

        // 判断并处理视频和图片的显示
        if (post.getVideoUrl() != null) {
            // 加载视频封面图
            Glide.with(holder.itemView.getContext())
                    .load(post.getPoster())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            int width = resource.getIntrinsicWidth();
                            int height = resource.getIntrinsicHeight();

                            // 根据宽高比调整ImageView的布局参数
                            ViewGroup.LayoutParams params = holder.ivPoster.getLayoutParams();
                            if (width > height) {
                                // 宽大于高，设置为横屏显示
                                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                holder.ivPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                // 高大于宽，设置为竖屏显示
                                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                holder.ivPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                            holder.ivPoster.setLayoutParams(params);
                            holder.ivPoster.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(holder.ivPoster); //加载视频封面

            holder.ivPoster.setVisibility(View.VISIBLE);
            holder.ivPlay.setVisibility(View.VISIBLE); //封面与暂停按钮均可见
            holder.ivPoster.setOnClickListener(v -> {
                // 启动视频播放
                holder.videoView.setVisibility(View.VISIBLE);
                holder.ivPoster.setVisibility(View.GONE);
                holder.ivPlay.setVisibility(View.GONE); //启动后均不可见
                holder.videoView.setVideoPath(post.getVideoUrl());
                holder.videoView.setMediaController(new MediaController(holder.itemView.getContext()));
                holder.videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0); // 设置静音
                    holder.videoView.start();
                });
                holder.videoView.setOnCompletionListener(mp -> {
                    holder.videoView.start(); // 循环播放
                });
            });

            holder.flMediaContainer.setVisibility(View.VISIBLE);


        } else if (post.getImages() != null && !post.getImages().isEmpty()) {
            // 处理图片点击事件
            holder.flMediaContainer.setVisibility(View.VISIBLE);
            holder.flMediaContainer.removeAllViews(); //图片内容设为可见并清空所有子视图内容

            if (post.getImages().size() == 1) {
                ImageView imageView = new ImageView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( //创建实例化布局参数
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8); //四个方向边距
                imageView.setLayoutParams(params); //将该布局参数应用到imageview视图当中去
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//图片均匀缩放

                Glide.with(holder.itemView.getContext())
                        .load(post.getImages().get(0))
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                int width = resource.getIntrinsicWidth();
                                int height = resource.getIntrinsicHeight();
                                float ratio = (float) width / height;
                                if (ratio > 1) {
                                    imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                } else {
                                    imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                                }
                                imageView.setImageDrawable(resource);
                                holder.flMediaContainer.addView(imageView);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        }); //根据宽高比例设置显示模式


                //实现大图显示
                imageView.setOnClickListener(v -> handleImageClick( //点击调用handleImageClick方法
                        context, post.getImages(), 0, post.getUsername(), post.getAvatar())); //默认单张图片index为0


            } else {
                // 多张图九宫格显示，通过循环加载多张图片
                holder.imageGrid.setVisibility(View.VISIBLE); //显示图片网格容器
                holder.imageGrid.removeAllViews(); //清除其他容器
                holder.imageGrid.setColumnCount(3); //列数设置为3

                for (int i = 0; i < post.getImages().size(); i++) {
                    String imageUrl = post.getImages().get(i);
                    ImageView imageView = new ImageView(holder.itemView.getContext());
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = holder.imageGrid.getWidth() / 3;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
                    params.setMargins(4, 4, 4, 4);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(holder.itemView.getContext())
                            .load(imageUrl)
                            .into(imageView);
                    holder.imageGrid.addView(imageView);

                    // 大图显示
                    final int index = i;
                    imageView.setOnClickListener(v -> handleImageClick(context, post.getImages(), index, post.getUsername(), post.getAvatar()));
                }
                holder.flMediaContainer.addView(holder.imageGrid); //媒体容器加载grid视图
            }
        } else {
            holder.flMediaContainer.setVisibility(View.GONE);
        }


        // 点赞按钮点击事件，变色+动画逻辑实现
        holder.imgLike.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin(); //若未登录必须调用方法跳转登陆页面
                return;
            }

            int adapterPosition = holder.getAdapterPosition(); //获取当前位置给adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) { //确保位置有效
                PostInfo currentPost = postList.get(adapterPosition); //拿到数据
                boolean isLiked = currentPost.isLikeFlag();
                currentPost.setLikeFlag(!isLiked); //--->>>如果未被点赞则点赞，否也类似
                currentPost.setLikeCount(currentPost.getLikeCount() + (isLiked ? -1 : 1));
                holder.tvLike.setText(String.valueOf(currentPost.getLikeCount()));//更新点赞数

                holder.tvLike.setTextColor(ContextCompat.getColor(context, currentPost.isLikeFlag() ? R.color.red : R.color.black));
                holder.imgLike.setImageResource(currentPost.isLikeFlag() ? R.drawable.liked : R.drawable.like);//文本、图片变红

                // 稍微放大缩小比例获得效果
                ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(holder.imgLike, "scaleX", isLiked ? 0.7f : 1.3f, 1f);
                ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(holder.imgLike, "scaleY", isLiked ? 0.7f : 1.3f, 1f);
                AnimatorListenerAdapter resetRotationListener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        holder.imgLike.setRotationY(0f);
                    }
                };
                if (isLiked) {
                    scaleXAnimator.setDuration(1000).start();
                    scaleYAnimator.setDuration(1000).start();
                } else {
                    ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(holder.imgLike, "rotationY", 0f, 360f);
                    rotationAnimator.addListener(resetRotationListener);
                    scaleXAnimator.setDuration(1000).start();
                    scaleYAnimator.setDuration(1000).start();
                    rotationAnimator.setDuration(1000).start();
                }

                String token = "Bearer " + getToken(); // 获取Token
                ApiService.LikeRequest request = new ApiService.LikeRequest(currentPost.getId());
                if (isLiked) {
                    unlikePost(token, request, holder, currentPost);
                } else {
                    likePost(token, request, holder, currentPost);
                }

                // TODO: Perform the network request to update the like status on the server
            }
        });

        // 评论按钮toast
        holder.imgComment.setOnClickListener(v -> {
            String message = "点击第N条数据评论按钮";
            Toast.makeText(holder.itemView.getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void likePost(String token, ApiService.LikeRequest request, PostViewHolder holder, PostInfo currentPost) {
        //调用API的点赞接口
        apiService.likePost(token, request).enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.isData()) {
                        // 点赞成功
                        currentPost.setLikeFlag(true);//置设置点赞状态为true

                        holder.tvLike.setText(String.valueOf(currentPost.getLikeCount()));//点赞数
                        holder.tvLike.setTextColor(ContextCompat.getColor(context, R.color.red));//点赞数数字变红
                        holder.imgLike.setImageResource(R.drawable.liked);//红心
                    } else {
                        // 点赞失败，恢复原状态

                        Toast.makeText(context, "点赞失败：" + apiResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 点赞失败，恢复原状态

                    Toast.makeText(context, "点赞失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                // 点赞失败，恢复原状态
                currentPost.setLikeFlag(false);

                holder.tvLike.setText(String.valueOf(currentPost.getLikeCount()));
                holder.tvLike.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.imgLike.setImageResource(R.drawable.like);
                Toast.makeText(context, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlikePost(String token, ApiService.LikeRequest request, PostViewHolder holder, PostInfo currentPost) {
        // 调用取消点赞接口，传递token和请求体
        apiService.unlikePost(token, request).enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.isData()) {
                        // 取消点赞成功
                        currentPost.setLikeFlag(false);

                        holder.tvLike.setText(String.valueOf(currentPost.getLikeCount()));
                        holder.tvLike.setTextColor(ContextCompat.getColor(context, R.color.black));
                        holder.imgLike.setImageResource(R.drawable.like);
                    } else {
                        // 取消点赞失败，恢复原状态
                        currentPost.setLikeFlag(true);

                        holder.tvLike.setText(String.valueOf(currentPost.getLikeCount()));
                        holder.tvLike.setTextColor(ContextCompat.getColor(context, R.color.red));
                        holder.imgLike.setImageResource(R.drawable.liked);
                        Toast.makeText(context, "取消点赞失败：" + apiResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 取消点赞失败，恢复原状态

                    Toast.makeText(context, "取消点赞失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                // 取消点赞失败，恢复原状态

                Toast.makeText(context, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        if (context instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();

            Toast.makeText(context, "请先登录账户！", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageClick(Context context, List<String> images, int index, String username, String avatar) {
        Log.d(TAG, "onBindViewHolder: 跳过去");
        ImageViewerActivity.start(context, images, index, username, avatar);
        Log.d(TAG, "onBindViewHolder: 跳过去了");

    }



    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvTitle;
        FrameLayout flMediaContainer; // 使用 FrameLayout 显示媒体
        ImageView ivPoster; // 显示视频封面图
        ImageView imgDelete;
        TextView tvLike;
        TextView tvComment;
        ImageView imgLike;
        ImageView imgComment;
        VideoView videoView; // 显示视频
        ImageView singleImage;
        GridLayout imageGrid;
        ImageView ivPlay;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTitle = itemView.findViewById(R.id.tv_title);
            flMediaContainer = itemView.findViewById(R.id.media_container);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            imgDelete = itemView.findViewById(R.id.btn_delete);
            tvLike = itemView.findViewById(R.id.tv_like);
            imgLike = itemView.findViewById(R.id.img_like);
            tvComment = itemView.findViewById(R.id.tv_comment);
            imgComment = itemView.findViewById(R.id.img_comment);
            videoView = itemView.findViewById(R.id.video_view);
            singleImage = itemView.findViewById(R.id.single_image);
            imageGrid = itemView.findViewById(R.id.image_grid);
            ivPlay = itemView.findViewById(R.id.iv_play_button);
        }
    }
}