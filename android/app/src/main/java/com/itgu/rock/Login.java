package com.itgu.rock;

import com.itgu.rock.tools.ApiConfig;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.itgu.rock.databinding.RockLoginBinding;
import com.itgu.rock.pojo.ApiResponse;
import com.itgu.rock.pojo.LoginRequestDTO;
import com.itgu.rock.pojo.LoginResp;
import com.itgu.rock.tools.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class Login extends AppCompatActivity {

    private RockLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RockLoginBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        initListeners();
    }

    /** 初始化按钮点击监听 */
    private void initListeners() {
        // 点击空白隐藏键盘
        binding.getRoot().setOnClickListener(v -> hideKeyboard(v));

        // 跳转注册页面
        binding.mainTitle.registerButton.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, RockRegister.class));
            finish();
        });

        // 登录按钮点击
        binding.mainBtnLogin.setOnClickListener(v -> attemptLogin());
    }

    /** 尝试登录 */
    private void attemptLogin() {
        String username = binding.inputLayout.usernameText.getText().toString().trim();
        String password = binding.inputLayout.passwordText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // 构造请求体（根据后端接口要求）
        LoginRequestDTO loginDTO = new LoginRequestDTO(username, password);

        RetrofitClient.getApiService().userLoginAPI(loginDTO)
                .enqueue(new Callback<ApiResponse<LoginResp>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResp>> call, Response<ApiResponse<LoginResp>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getCode() == 200) {
                                onLoginSuccess(response.body().getData());
                            } else {
                                Toast.makeText(Login.this, "登录失败: " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "登录失败: " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResp>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(Login.this, "登录失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** 登录成功处理 */
    private void onLoginSuccess(LoginResp data) {

        Toast.makeText(this, "登录成功", Toast.LENGTH_LONG).show();
        String fullAvatarUrl = data.getAvatarUrl() != null ? ApiConfig.url(data.getAvatarUrl()) : null;
        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        sp.edit()
                .putString("Token", data.getToken())
                .putString("Username", data.getUsername())
                .putString("Email", data.getEmail())
                .putString("Phone", data.getPhone())
                .putInt("UserId", data.getId())
                .putString("Gender",data.getGender())
                .putString("AvatarUrl", fullAvatarUrl)
                .apply();
        startActivity(new Intent(Login.this, RockRecognize.class));
        finish();
    }

    /** 显示或隐藏进度条 */
    private void showLoading(boolean isLoading) {
        binding.layoutProgress.getRoot().setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.inputLayout.getRoot().setVisibility(isLoading ? View.GONE : View.VISIBLE);

        if (isLoading) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(binding.layoutProgress.progressBar2, "rotation", 0f, 360f);
            animation.setDuration(1000);
            animation.setRepeatCount(ObjectAnimator.INFINITE);
            animation.setInterpolator(new LinearInterpolator());
            animation.start();
        }
    }

    /** 隐藏键盘 */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
