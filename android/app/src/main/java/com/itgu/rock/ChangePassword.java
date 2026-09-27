package com.itgu.rock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.pojo.ApiResponse;
import com.itgu.rock.tools.ApiService;
import com.itgu.rock.tools.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSubmit;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSubmit = findViewById(R.id.btn_submit);
        ivBack = findViewById(R.id.iv_back);
        //保存
        btnSubmit.setOnClickListener(v -> changePassword());
        // 点击返回按钮 -> 回到 Settings
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePassword.this, Settings.class);
            startActivity(intent);
            finish();
        });

    }

    private void changePassword() {
        String oldPwd = etOldPassword.getText().toString().trim();
        String newPwd = etNewPassword.getText().toString().trim();
        String confirmPwd = etConfirmPassword.getText().toString().trim();

        // 前端校验
        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用后端接口
        sendChangePasswordRequest(oldPwd, newPwd);
    }
    private void sendChangePasswordRequest(String oldPwd, String newPwd) {

        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sp.getString("Token", "").trim();

        ApiService api = RetrofitClient.getApiService(token);
        Call<ApiResponse> call = api.changePassword(oldPwd, newPwd);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    Toast.makeText(ChangePassword.this, "修改成功", Toast.LENGTH_SHORT).show();
                    finish(); // 返回设置页面
                } else {
                    Toast.makeText(ChangePassword.this,
                            response.body() != null ? response.body().getMessage() : "修改失败",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ChangePassword.this, "请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
