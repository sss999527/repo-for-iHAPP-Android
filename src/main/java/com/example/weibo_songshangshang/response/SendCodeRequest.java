package com.example.weibo_songshangshang.response;

public class SendCodeRequest {
    private String phone;

    public SendCodeRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
