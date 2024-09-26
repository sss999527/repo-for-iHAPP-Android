package com.example.weibo_songshangshang.response;

import java.util.List;

public class HomePageResponse {
    // HomePageResponse类用于封装首页数据的响应结果。它包含几个关键字段和内部类来表示具体的数据结构。
    private int code;
    private String msg;
    private Page data; //数据是一个内部类Page的实例，具体内容下面定义了

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Page getData() {
        return data;
    }

    public void setData(Page data) {
        this.data = data;
    }

    public static class Page {
        private List<PostInfo> records;
        private int total;
        private int size;
        private int current;
        private int pages;

        public List<PostInfo> getRecords() {
            return records;
        } //一个微博数据类的列表，里面存储头像、用户名等信息

        public void setRecords(List<PostInfo> records) {
            this.records = records;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }
    }
}