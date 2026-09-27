package com.itgu.rock.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.itgu.rock.pojo.ModelParams;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelParameterManager {

    public static void modify(Context context, String lr, String bs, String ep) {
        // 1. 参数校验
        if (lr.isEmpty() || bs.isEmpty() || ep.isEmpty()) {
            Toast.makeText(context, "请输入完整参数！", Toast.LENGTH_SHORT).show();
            return;
        }

        ModelParams params;
        try {
            params = new ModelParams(
                    Double.parseDouble(lr),
                    Integer.parseInt(bs),   // 批量大小
                    Integer.parseInt(ep)    // 迭代次数
            );
        } catch (NumberFormatException e) {
            Toast.makeText(context, "参数格式不正确！", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ModelParameterManager", "修改参数 => 学习率: " + lr + ", 批量大小: " + bs + ", 迭代次数: " + ep);

       // 2. 发请求
        SharedPreferences sp = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = sp.getString("Token", null);

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<ResponseBody> call = apiService.updateParams(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "修改成功！", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        String errorStr = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "修改失败：" + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "请求错误：" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
