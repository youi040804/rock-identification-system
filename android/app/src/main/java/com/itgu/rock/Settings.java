package com.itgu.rock;

import com.itgu.rock.tools.ApiConfig;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.itgu.rock.databinding.ActivitySettingsBinding;
import com.itgu.rock.tools.ApiService;
import com.itgu.rock.tools.FileUtils;
import com.itgu.rock.tools.RetrofitClient;
import com.itgu.rock.tools.SelectPhotoContract;
import com.itgu.rock.tools.TakeCameraUri;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Settings extends AppCompatActivity {

    private ImageView ivAvatar;

    // 1. 注册 ActivityResultLauncher
    private ActivityResultLauncher<Intent> selectPhotoLauncher;
    private ActivityResultLauncher<Object> takeCameraLauncher;
    private ActivitySettingsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ivAvatar = findViewById(R.id.iv_user_avatar);

        // --- 读取本地存储的头像URL并显示 ---
        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String avatarUrl = sp.getString("AvatarUrl", null);
        if (avatarUrl != null) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .into(ivAvatar);
        }
        // 显示用户名
        TextView tvUsername = findViewById(R.id.tv_username);
        String username = sp.getString("Username", "未设置昵称");
        tvUsername.setText(username);
        //显示性别
        TextView tvGender = findViewById(R.id.tv_gender);
        String gender = sp.getString("Gender", "未设置性别");
        tvGender.setText(gender);

        // 点击头像弹框选择

        // 修改性别事件
        LinearLayout llavatar = findViewById(R.id.ll_avatar);
        llavatar.setOnClickListener(v -> showChooseDialog());


        ivAvatar.setOnClickListener(v -> showChooseDialog());
        //返回

        binding.ivBack.setOnClickListener(v ->{
            Intent intent = new Intent(Settings.this, UserCenter.class);
            startActivity(intent);
            finish();
        });

        // 点击昵称行，跳转到修改页面
        LinearLayout ll_edit_username = findViewById(R.id.ll_username);
        ll_edit_username.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, EditUsername.class);
            startActivity(intent);
        });

        // 修改性别事件
        LinearLayout llGender = findViewById(R.id.ll_gender);
        llGender.setOnClickListener(v -> showEditGenderDialog());

        // 跳转到修改密码页面
        LinearLayout llChangePassword = findViewById(R.id.ll_change_password);
        llChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, ChangePassword.class);
            startActivity(intent);
        });


        // 清理缓存逻辑
        LinearLayout llClearCache = findViewById(R.id.ll_clear_cache);
        llClearCache.setOnClickListener(v -> clearAppCacheWithSize());

        //关于我们

        LinearLayout ll_about_us = findViewById(R.id.ll_about_us);
        ll_about_us.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, AboutUsActivity.class);
            startActivity(intent);
        });

        // 注册启动器
        selectPhotoLauncher = registerForActivityResult(new SelectPhotoContract(), uri -> {
            if (uri != null) {
                ivAvatar.setImageURI(uri);
                try {
                    uploadAvatarToServer(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        takeCameraLauncher = registerForActivityResult(new TakeCameraUri(), uri -> {
            if (uri != null) {
                ivAvatar.setImageURI(uri);
                try {
                    uploadAvatarToServer(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });



    }

    private void showChooseDialog() {
        String[] options = {"拍摄图片", "从相册选择"};
        new AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) takeCameraLauncher.launch(null); // 拍照
                    else selectPhotoLauncher.launch(new Intent());  // 选相册
                })
                .show();
    }

    private void uploadAvatarToServer(Uri imageUri) throws IOException {
        File file = FileUtils.getTempFileFromUri(this, imageUri);
        Glide.with(this)
                .load(imageUri)
                .circleCrop() // 圆形裁剪
                .into(ivAvatar);
        RequestBody requestFile = okhttp3.RequestBody.Companion.create(file, okhttp3.MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sp.getString("Token", null);
        int userId = sp.getInt("UserId", -1);

        if (token == null || userId == -1) {
            Toast.makeText(this, "未获取到用户信息", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<ResponseBody> call = apiService.uploadAvatar(body, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String bodyString = response.body().string();
                        JSONObject jsonObject = new JSONObject(bodyString); // 把字符串转成 JSON
                        String filename = jsonObject.getString("filename");
                        String avatarUrl = ApiConfig.url("userAvatar/" + filename);

                        // 保存到 SharedPreferences
                        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        sp.edit().putString("AvatarUrl", avatarUrl).apply();

                        // 刷新 ImageView
                        Glide.with(Settings.this)
                                .load(avatarUrl)
                                .circleCrop()
                                .into(ivAvatar);

                        Toast.makeText(Settings.this, "头像上传成功", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Settings.this, "解析返回结果失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Settings.this, "头像上传失败", Toast.LENGTH_SHORT).show();
                }
            }




            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Settings.this, "上传出错：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showEditGenderDialog() {
        String[] genderOptions = {"男", "女", "保密"};

        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sp.getString("Token", null);
        int userId = sp.getInt("UserId", -1);

        if (token == null || userId == -1) {
            Toast.makeText(this, "未获取到用户信息", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前性别
        String currentGender = sp.getString("Gender", "未设置");
        int checkedItem = 0;
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equals(currentGender)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择性别")
                .setSingleChoiceItems(genderOptions, checkedItem, null)
                .setPositiveButton("确定", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String selectedGender = genderOptions[selectedPosition];

                    // 调用后端接口修改性别
                    ApiService apiService = RetrofitClient.getApiService(token);
                    Call<ResponseBody> call = apiService.updateGender(userId, selectedGender);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                // 更新 SharedPreferences 和界面显示
                                sp.edit().putString("Gender", selectedGender).apply();
                                TextView tvGender = findViewById(R.id.tv_gender);
                                tvGender.setText(selectedGender);
                                Toast.makeText(Settings.this, "性别修改成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Settings.this, "修改失败：" + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(Settings.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private void clearAppCacheWithSize() {
        File cacheDir = getCacheDir();
        File externalCacheDir = getExternalCacheDir();

        long beforeSize = 0;
        if (cacheDir != null) beforeSize += getDirSize(cacheDir);
        if (externalCacheDir != null) beforeSize += getDirSize(externalCacheDir);

        int deletedFiles = 0;
        if (cacheDir != null) deletedFiles += deleteDirFiles(cacheDir);
        if (externalCacheDir != null) deletedFiles += deleteDirFiles(externalCacheDir);

        long afterSize = 0;
        if (cacheDir != null) afterSize += getDirSize(cacheDir);
        if (externalCacheDir != null) afterSize += getDirSize(externalCacheDir);

        long released = beforeSize - afterSize;
        //#####
        Log.d("CacheClear", "清理前缓存大小：" + formatSize(beforeSize));
        Log.d("CacheClear", "清理后缓存大小：" + formatSize(afterSize));
        Log.d("CacheClear", "释放空间：" + formatSize(released));
//##############
        Toast.makeText(this, "缓存已清理，共删除 " + deletedFiles + " 个文件，释放空间 " + formatSize(released), Toast.LENGTH_LONG).show();
    }

    private int deleteDirFiles(File dir) {
        int count = 0;
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        count += deleteDirFiles(child);
                    }
                    if (child.delete()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }


    // 递归计算目录大小
    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        size += getDirSize(f);
                    } else {
                        size += f.length();
                    }
                }
            }
        }
        return size;
    }

    // 格式化大小，变成 KB/MB
    private String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#")
                .format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
