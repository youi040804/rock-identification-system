
package com.itgu.rock;

import com.itgu.rock.tools.ApiConfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.PopupMenu;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.itgu.rock.databinding.UserfeedbackBinding;
import com.itgu.rock.tools.FileUtils;
import com.itgu.rock.tools.HttpService;
import com.itgu.rock.tools.SelectPhotoContract;
import com.itgu.rock.tools.TakeCameraUri;

import java.io.File;
import java.io.IOException;
public class UserFeed extends AppCompatActivity {

    private UserfeedbackBinding binding;
    private Uri selectedImageUri;
    private String token;


    // 从相册选择图片
    private final ActivityResultLauncher<Intent> selectPhotoLauncher = registerForActivityResult(
            new SelectPhotoContract(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivSelectImage.setImageURI(uri);
                    binding.llImagePickerOverlay.setVisibility(View.GONE); // 隐藏占位图 Overlay
                }
            });

    // 拍照
    private final ActivityResultLauncher<Object> takePhotoLauncher = registerForActivityResult(
            new TakeCameraUri(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivSelectImage.setImageURI(uri);
                    binding.llImagePickerOverlay.setVisibility(View.GONE); // 隐藏占位图 Overlay
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UserfeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 从 SharedPreferences 获取 token
        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = sp.getString("Token", null);

        binding.flImagePicker.setOnClickListener(this::showImagePickerMenu);
        binding.llImagePickerOverlay.setOnClickListener(this::showImagePickerMenu);

        binding.btnSubmit.setOnClickListener(v -> submitFeedback());
        binding.btnReturn.setOnClickListener(v -> {
            startActivity(new Intent(UserFeed.this, RockRecognize.class));
            finish();
        });
    }

    private void showImagePickerMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 1, 0, "从相册中选择");
        popup.getMenu().add(0, 2, 1, "拍照");
        popup.getMenu().add(0, 3, 2, "取消");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: chooseFromGallery(); return true;
                case 2: takePhoto(); return true;
                case 3: popup.dismiss(); return true;
            }
            return false;
        });
        popup.show();
    }

    private void chooseFromGallery() {
        selectPhotoLauncher.launch(null);
    }

    private void takePhoto() {
        takePhotoLauncher.launch(null);
    }

    private boolean checkBeforeSubmit() {
        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = sp.getString("Token", "");
        if (token == null ||token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etRockName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入岩石名称", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void submitFeedback() {
        if (!checkBeforeSubmit()) return;

        File imageFile;
        try {
            imageFile = FileUtils.getTempFileFromUri(this, selectedImageUri);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
            return;
        }

        String uploadUrl = ApiConfig.url("feedback/submit");

        HttpService httpService = new HttpService(token);
        httpService.uploadFeedback(uploadUrl, imageFile, binding.etRockName.getText().toString().trim(),
                new HttpService.Callback() {
                    @Override
                    public void onSuccess(String response) {
                        runOnUiThread(UserFeed.this::resetForm);
                    }

                    @Override
                    public void onFailure(IOException e) {
                        runOnUiThread(() -> Toast.makeText(UserFeed.this,
                                "反馈提交失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void resetForm() {
        Toast.makeText(this, "反馈提交成功", Toast.LENGTH_SHORT).show();
        binding.etRockName.setText("");
        selectedImageUri = null;

        binding.ivSelectImage.setImageDrawable(null);           // 清空图片
        binding.llImagePickerOverlay.setVisibility(View.VISIBLE); // 显示占位图 Overlay
    }
}
