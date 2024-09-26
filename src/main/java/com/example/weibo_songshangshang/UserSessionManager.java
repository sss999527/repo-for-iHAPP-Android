package com.example.weibo_songshangshang;


import android.content.Context;
import android.content.SharedPreferences;

// 用户会话管理类，用于处理用户的登录状态及相关信息的存储与读取
public class UserSessionManager {
    // SharedPreferences文件名及存储键名的常量定义
    private static final String PREFERENCES_NAME = "UserSession"; // SharedPreferences文件名
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; // 用户是否已登录的标志
    private static final String KEY_USERNAME = "username"; // 存储用户名的键
    private static final String KEY_FOLLOWERS = "followers"; // 存储关注者数量的键
    private static final String KEY_AVATAR = "avatar"; // 存储用户头像URL的键

    // SharedPreferences实例和Editor实例，用于读写数据
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // 构造函数，接收Context用于初始化SharedPreferences
    public UserSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE); // 初始化SharedPreferences
        editor = sharedPreferences.edit(); // 获取编辑器实例以便写入数据
    }

    // 登录方法，存储用户登录状态和信息
    public void login(String username, String followers, String avatarUrl) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true); // 设置用户已登录标志为true
        editor.putString(KEY_USERNAME, username); // 存储用户名
        editor.putString(KEY_FOLLOWERS, followers); // 存储关注者数量
        editor.putString(KEY_AVATAR, avatarUrl); // 存储头像URL
        editor.apply(); // 提交更改
    }

    // 登出方法，清除所有用户相关的session数据
    public void logout() {
        editor.clear(); // 清除所有数据
        editor.apply(); // 提交更改
    }

    // 检查用户是否已经登录
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    } // 默认返回false，即未登录

    // 获取用户名
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    } // 默认返回空字符串

    // 获取关注者数量
    public String getFollowers() {
        return sharedPreferences.getString(KEY_FOLLOWERS, "");
    } // 默认返回空字符串

    // 获取用户头像URL
    public String getAvatar() {
        return sharedPreferences.getString(KEY_AVATAR, "");
    } // 默认返回空字符串
}


//第二版
//2
//2
//2
//2
//import android.content.Context;
//import android.content.SharedPreferences;
//
//public class UserSessionManager {
//
//    private SharedPreferences sharedPreferences;
//    private SharedPreferences.Editor editor;
//    private static final String PREF_NAME = "user_session";
//    private static final String KEY_TOKEN = "token";
//    private static final String KEY_USERNAME = "username";
//    private static final String KEY_FOLLOWERS = "followers";
//
//    public UserSessionManager(Context context) {
//        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        editor = sharedPreferences.edit();
//    }
//
//    public void saveToken(String token) {
//        editor.putString(KEY_TOKEN, token);
//        editor.apply();
//    }
//
//    public String getToken() {
//        return sharedPreferences.getString(KEY_TOKEN, null);
//    }
//
//    public boolean isLoggedIn() {
//        return getToken() != null;
//    }
//
//    public void logout() {
//        editor.clear();
//        editor.apply();
//    }
//
//    public void saveUsername(String username) {
//        editor.putString(KEY_USERNAME, username);
//        editor.apply();
//    }
//
//    public String getUsername() {
//        return sharedPreferences.getString(KEY_USERNAME, null);
//    }
//
//    public void saveFollowers(String followers) {
//        editor.putString(KEY_FOLLOWERS, followers);
//        editor.apply();
//    }
//
//    public String getFollowers() {
//        return sharedPreferences.getString(KEY_FOLLOWERS, null);
//    }
//}






