package com.example.weibo_songshangshang.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.weibo_songshangshang.ImageAdapter;
import com.example.weibo_songshangshang.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvPageCount;
    private ImageView btnDownload;

    private List<String> imageUrls;
    private int currentIndex;
    private String username;
    private String avatarUrl;

    private static final String TAG = ImageViewerActivity.class.getSimpleName();
    public static void start(Context context, List<String> imageUrls, int index, String username, String avatarUrl) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
        intent.putExtra("index", index);
        intent.putExtra("username", username);
        intent.putExtra("avatarUrl", avatarUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        viewPager = findViewById(R.id.viewPager);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvPageCount = findViewById(R.id.tv_page_indicator);
        btnDownload = findViewById(R.id.btn_download);

        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        currentIndex = getIntent().getIntExtra("index", 0);
        username = getIntent().getStringExtra("username");
        avatarUrl = getIntent().getStringExtra("avatarUrl");

        Glide.with(this)
                .load(avatarUrl)
                .transform(new CircleCrop())
                .into(ivAvatar);
        tvUsername.setText(username);
        tvPageCount.setText((currentIndex + 1) + "/" + imageUrls.size());

        ImageAdapter adapter = new ImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                tvPageCount.setText((position + 1) + "/" + imageUrls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        btnDownload.setOnClickListener(v -> downloadImage(imageUrls.get(currentIndex)));

        findViewById(R.id.image_viewer_container).setOnClickListener(v -> finish());
    }

    private void downloadImage(String imageUrl) {
        new Thread(() -> {
            try {
                Bitmap bitmap = Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();

                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WeiboZhouliang");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
                File file = new File(directory, fileName);
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                // 通知图库更新
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                runOnUiThread(() -> Toast.makeText(this, "图片下载完成，请到相册查看", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "图片下载失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}