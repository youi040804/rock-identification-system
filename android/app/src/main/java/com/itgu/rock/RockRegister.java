package com.itgu.rock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.itgu.rock.databinding.RockRegisterBinding;
import com.itgu.rock.pojo.ApiResponse;
import com.itgu.rock.pojo.LoginResp;
import com.itgu.rock.pojo.UserRegisterDTO;
import com.itgu.rock.tools.RetrofitClient;
import retrofit2.Call;
import retrofit2.Response;
public class RockRegister extends AppCompatActivity {
    private RockRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RockRegisterBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        initListeners();
    }

    /** 初始化按钮点击监听 */
    private void initListeners() {
        binding.getCodeButton.setOnClickListener(v -> sendVerificationCode());

        binding.registerButton.setOnClickListener(v -> {
            hideKeyboard(v);
            registerUser();
        });

        binding.loginButton.setOnClickListener(v -> {
            startActivity(new Intent(RockRegister.this, Login.class));
            finish();
        });

        binding.getRoot().setOnClickListener(v -> hideKeyboard(v));
    }

    /** 发送验证码 */
    private void sendVerificationCode() {
        String email = binding.emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().sendCode(email)
                .enqueue(new retrofit2.Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                            Toast.makeText(RockRegister.this, "验证码发送成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RockRegister.this, "发送失败：" +
                                            (response.body() != null ? response.body().getMessage() : response.message()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(RockRegister.this, "请求异常：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** 注册用户 */
    private void registerUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String username = binding.nicknameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String code = binding.codeEditText.getText().toString().trim();

        if (!validateInputs(email, password, username, phone, code)) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        UserRegisterDTO userDTO = new UserRegisterDTO(username, password, email, phone, code);

        RetrofitClient.getApiService().userRegisterAPI(userDTO)
                .enqueue(new retrofit2.Callback<ApiResponse<LoginResp>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResp>> call, Response<ApiResponse<LoginResp>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int code = response.body().getCode();
                            String message = response.body().getMessage();

                            if (code == 200) { // 注册成功
                                Toast.makeText(RockRegister.this, "注册成功", Toast.LENGTH_SHORT).show();
                                LoginResp loginResp = response.body().getData(); // 拿到返回的用户信息（含 id）
                                saveUserData(loginResp);
                                Log.d("RegisterDebug", "loginResp = " + loginResp);

                                //saveUserData(userDTO);
                                // 直接跳回登录界面
                                startActivity(new Intent(RockRegister.this, Login.class));
                                finish();
                            } else { // 注册失败，保留输入
                                Toast.makeText(RockRegister.this, "注册失败：" + message, Toast.LENGTH_SHORT).show();
                            }
                        } else { // 请求成功，但返回体为空或异常
                            Toast.makeText(RockRegister.this, "注册失败：" + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResp>> call, Throwable t) {
                        Toast.makeText(RockRegister.this, "网络请求失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** 输入验证 */
    private boolean validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input.isEmpty()) return false;
        }
        return true;
    }

    /** 保存用户信息到 SharedPreferences */
    private void saveUserData(LoginResp user) {
        Integer id = user.getId();
        if (id != null) {

            SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String cleanToken = sp.getString("Token", "").trim();

            sp.edit()
                    .putInt("userId", user.getId())
                    .putString("Username", user.getUsername())
                    .putString("Email", user.getEmail())
                    .putString("Phone", user.getPhone())
                    .putString("Password", user.getPassword())
                    .putString("Token", cleanToken)
                    //.putString("Token", user.getToken())
                    .apply();
        }
//        } else {
//            Log.e("SaveUserData", "userId is null, cannot save to SharedPreferences!");
//        }
    }


    /** 隐藏键盘 */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
