package com.itgu.dto;
//####这个用来替换pojo包下的result.java文件

public class ApiResponse<T> {
    private int code;       // 状态码
    private String message; // 提示信息
    private T data;         // 具体数据

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    //code=200 快速返回成功
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    //code=500 快速返回失败
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(500, message, data);
    }

    // getter & setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
