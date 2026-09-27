package com.itgu.Pojo;

public class ModelAuth {
    private Integer id;
    private String password; // 这里暂时明文存储

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
