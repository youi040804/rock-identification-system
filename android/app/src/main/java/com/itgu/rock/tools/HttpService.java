package com.itgu.rock.tools;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpService {

    private OkHttpClient client;

    private Context context;

    public HttpService(Context context, String token) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(token))
                .build();
    }
    public interface Callback {
        void onSuccess(String response);
        void onFailure(IOException e);
    }


    // 构造函数：传入 token，由 AuthInterceptor 统一处理
    public HttpService(String token) {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(token))  // 使用新的拦截器
                .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
                .readTimeout(30, TimeUnit.SECONDS)     // 读取超时
                .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
                .build();
    }

    // 通用请求执行方法
    private void sendAsyncRequest(Request request, Callback callback) {
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }
        });
    }
    // 上传用户反馈（图片 + 岩石名）
    public void uploadFeedback(String url, File fileimg, String rockName, Callback callback) {
        RequestBody fileBody = RequestBody.create(fileimg, MediaType.parse("multipart/form-data"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", fileimg.getName(), fileBody)
                .addFormDataPart("rockName", rockName)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        sendAsyncRequest(request, callback);
    }

//
    public void sendPutRequest(String url, String jsonBody, Callback callback) {
        // 1. 校验token
        SharedPreferences sp = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sp.getString("Token", null);
        if (token == null || token.isEmpty()) {
            Log.e("HttpService", "token为空，无法发送PUT请求");
            callback.onFailure(new IOException("token为空，请重新登录"));
            return;
        }

        // 2. 构建JSON请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        // 3. 构建PUT请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 添加token认证
                .put(requestBody) // 使用PUT方法
                .build();

        // 4. 发送异步请求
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    callback.onSuccess(responseData);
                } else {
                    callback.onFailure(new IOException("PUT请求失败，状态码：" + response.code()));
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }
        });
    }


    // 上传训练集图片
    public void uploadTrainImage(String url, File fileimg, String rockName, Callback callback) {
        RequestBody fileBody = RequestBody.create(fileimg, MediaType.parse("multipart/form-data"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", fileimg.getName(), fileBody)
                .addFormDataPart("rockName", rockName)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        sendAsyncRequest(request, callback);
    }
        //sendAsyncRequest(request, callback);
    //}

    // 获取反馈列表
    public void getFeedbackList(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        sendAsyncRequest(request, callback);
    }
    // PUT 请求（如删除、更新等）
    public void sendPutRequest(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        sendAsyncRequest(request, callback);
    }

    public void sendRockRecognizeRequest(String url, File file, Callback callback) {

        // 1. 构造图片表单字段，指定字段名为 "image"
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), fileBody) // 字段名 "image" 必须与后端一致
                .build();

        // 2. 创建包含表单数据的请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        sendAsyncRequest(request, callback);
    }


}




