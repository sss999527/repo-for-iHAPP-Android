package com.example.weibo_songshangshang.response;

public class LoginRequest {
    private String phone;
    private String smsCode;

    public LoginRequest(String phone, String smsCode) {
        this.phone = phone;
        this.smsCode = smsCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }
}
