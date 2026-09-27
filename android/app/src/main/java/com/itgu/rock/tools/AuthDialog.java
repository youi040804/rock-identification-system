
package com.itgu.rock.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.itgu.rock.ModleCtro;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 弹出密码验证对话框
 */
public class AuthDialog {

    public static void show(Context context, NetworkUtils networkUtils) {
        // 创建输入框
        EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        input.setHint("验证密码");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("请输入密码以继续模型管理操作")
                .setView(input)
                .setPositiveButton("确定", (d, which) -> {
                    String password = input.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 调接口校验
                    Map<String, String> body = new HashMap<>();
                    body.put("password", password);

                    body.put("password", password);

                    Call<Map<String, Object>> call = networkUtils.getModelAuthCheck(body);


                    String token = networkUtils.GetToken();  // 获取保存的 token


                    call.enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {


                            if (response.isSuccessful() && response.body() != null) {
                                Object successObj = response.body().get("success");
                                boolean success = successObj != null && (Boolean) successObj;

                                if (success) {
                                    Toast.makeText(context, "密码正确", Toast.LENGTH_SHORT).show();
                                    // 跳转到 ModleCtro
                                    Intent intent = new Intent(context, ModleCtro.class);
                                    context.startActivity(intent);
                                } else {
                                    Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "验证失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(context, "网络错误，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("取消", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }
}
