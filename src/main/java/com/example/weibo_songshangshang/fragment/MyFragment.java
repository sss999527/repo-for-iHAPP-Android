package com.example.weibo_songshangshang.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.weibo_songshangshang.R;
import com.example.weibo_songshangshang.activity.FirstPageActivity;

public class MyFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my, container, false);

        // 找到TextView控件
        TextView textView1 = view.findViewById(R.id.agree);
        TextView textView2 = view.findViewById(R.id.disagree);
        // 设置点击事件
        textView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 同意
                    if (getActivity() != null) {
                        // 保存用户状态
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("agreed", true);
                        editor.apply();

                        Intent intent = new Intent(getActivity(), FirstPageActivity.class);
                        startActivity(intent);
                        // 设置Activity转场动画
                        getActivity().overridePendingTransition(R.anim.shouye_in, R.anim.shouye_out);
                        getActivity().finish();
                    }
                }
            });

        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不同意
                getActivity().finish();
            }
        });


        //以下为隐私政策点击后显示toast
        TextView textView = view.findViewById(R.id.sm);

        String text = "欢迎使用iH微博，我们将严格遵守相关法律和隐私政策保护您的个人隐私，请您阅读并同意《用户协议》与《隐私政策》";
        SpannableString spannableString = new SpannableString(text);

// 设置用户协议的样式和点击事件
        int userAgreementStart = text.indexOf("《用户协议》");
        int userAgreementEnd = userAgreementStart + "《用户协议》".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(getContext(), "查看用户协议", Toast.LENGTH_SHORT).show();
            }
        }, userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// 设置隐私政策的样式和点击事件（类似用户协议）
        int privacyPolicyStart = text.indexOf("《隐私政策》");
        int privacyPolicyEnd = privacyPolicyStart + "《隐私政策》".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(getContext(), "查看隐私政策", Toast.LENGTH_SHORT).show();
            }
        }, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// 应用到TextView并设置MovementMethod
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        // 返回加载的视图
        return view;
    }


}
