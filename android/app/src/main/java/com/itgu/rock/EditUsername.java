package com.itgu.rock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.tools.ApiService;
import com.itgu.rock.tools.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUsername extends AppCompatActivity {

    private EditText etUsername;
    private Button btnSave;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_username);

        etUsername = findViewById(R.id.et_username);
        btnSave = findViewById(R.id.btn_save);
        ivBack = findViewById(R.id.iv_back);

        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sp.getString("Token", null);
        int userId = sp.getInt("UserId", -1);
        String currentName = sp.getString("Username", "");
        etUsername.setText(currentName);

        // 点击返回按钮 -> 回到 Settings
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditUsername.this, Settings.class);
            startActivity(intent);
            finish();
        });

        // 点击保存按钮
        btnSave.setOnClickListener(v -> {
            String newName = etUsername.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = RetrofitClient.getApiService(token);
            Call<ResponseBody> call = apiService.updateUsername(userId, newName);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        sp.edit().putString("Username", newName).apply();
                        Toast.makeText(EditUsername.this, "昵称修改成功", Toast.LENGTH_SHORT).show();

                        // 返回 Settings 并刷新用户名
                        Intent intent = new Intent(EditUsername.this, Settings.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(EditUsername.this, "修改失败：" + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(EditUsername.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
