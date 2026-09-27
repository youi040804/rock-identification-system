package com.itgu.rock;

import com.itgu.rock.tools.ApiConfig;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.itgu.rock.adapter.ImageAdapter;
import com.itgu.rock.databinding.AddtrainBinding;
import com.itgu.rock.tools.HttpService;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTrain extends AppCompatActivity {

    private AddtrainBinding binding;
    private List<File> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;

    private ActivityResultLauncher<Object> takePhotoLauncher;

    private ExecutorService uploadExecutor;
    private final int MAX_IMAGES = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddtrainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 RecyclerView
        imageAdapter = new ImageAdapter(this, selectedImages);
        binding.rvImages.setAdapter(imageAdapter);
        binding.rvImages.setLayoutManager(new GridLayoutManager(this, 3));

        // 设置添加图片监听
        imageAdapter.setOnAddClickListener(new ImageAdapter.OnAddClickListener() {
            @Override
            public void onTakePhoto() {
                takePhoto();
            }

            @Override
            public void onPickImages() {
               // pickImages();
                checkPermissionsAndPickImages();
            }
        });

        // 注册 takePhotoLauncher（你原来的拍照方法）
        takePhotoLauncher = registerForActivityResult(
                new com.itgu.rock.tools.TakeCameraUri(),
                uri -> {
                    if (uri != null) {
                        if (selectedImages.size() >= MAX_IMAGES) {
                            Toast.makeText(this, "最多只能选择9张图片", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            addImageFile(uri);
                            imageAdapter.notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "获取照片失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // 点击上传
        binding.buchong.setOnClickListener(v -> uploadSelectedImages());

        // 点击退出
        binding.tuichu.setOnClickListener(v -> {
            startActivity(new Intent(AddTrain.this, ModleCtro.class));
            finish();
        });

        // 初始化单线程上传池
        uploadExecutor = Executors.newSingleThreadExecutor();
    }

    // 使用 Matisse 选择图片
    private void pickImages() {
        int remaining = MAX_IMAGES - selectedImages.size();
        if (remaining <= 0) {
            Toast.makeText(this, "最多只能选择" + MAX_IMAGES + "张图片", Toast.LENGTH_SHORT).show();
            return;
        }


        Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(remaining)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, getPackageName() + ".fileprovider"))
                .imageEngine(new GlideEngine())
                .forResult(23);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 23 && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = Matisse.obtainResult(data);
            for (Uri uri : uris) {
                if (selectedImages.size() >= MAX_IMAGES) break;
                try {
                    addImageFile(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
                }
            }
            imageAdapter.notifyDataSetChanged();
            Toast.makeText(this, selectedImages.size() + "/" + MAX_IMAGES, Toast.LENGTH_SHORT).show();
        }
    }


    private void addImageFile(Uri uri) throws IOException {
        File file = getFileFromUri(uri);
        if (!selectedImages.contains(file)) {
            selectedImages.add(file);
        }
    }

    private void takePhoto() {
        takePhotoLauncher.launch(null);
    }

    private void uploadSelectedImages() {
        String rockName = binding.etRockName.getText().toString().trim();
        if (rockName.isEmpty()) {
            Toast.makeText(this, "请填写岩石名称", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取 token
        String token = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("Token", null);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.url("train/upload");

        final int total = selectedImages.size();
        final int[] uploadedCount = {0};
        final boolean[] hasError = {false};

        HttpService httpService = new HttpService(token);

        for (File file : selectedImages) {
            uploadExecutor.submit(() -> {

                httpService.uploadTrainImage(url, file, rockName, new HttpService.Callback() {
                    @Override
                    public void onSuccess(String response) {
                        synchronized (uploadedCount) {
                            uploadedCount[0]++;
                            if (uploadedCount[0] == total) {
                                runOnUiThread(() -> {
                                    if (hasError[0]) {
                                        Toast.makeText(AddTrain.this, "部分图片上传失败", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddTrain.this, "已补充到训练集", Toast.LENGTH_SHORT).show();
                                    }
                                    selectedImages.clear();
                                    imageAdapter.notifyDataSetChanged();
                                    binding.etRockName.setText("");
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {
                        e.printStackTrace();
                        synchronized (uploadedCount) {
                            hasError[0] = true;
                            uploadedCount[0]++;
                            if (uploadedCount[0] == total) {
                                runOnUiThread(() -> {
                                    Toast.makeText(AddTrain.this, "部分图片上传失败", Toast.LENGTH_SHORT).show();
                                    selectedImages.clear();
                                    imageAdapter.notifyDataSetChanged();
                                    binding.etRockName.setText("");
                                });
                            }
                        }
                    }
                });
            });
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(getCacheDir(), System.currentTimeMillis() + ".jpg");
        try (InputStream is = getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
        return tempFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uploadExecutor.shutdownNow();
    }


    private void checkPermissionsAndPickImages() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{"android.permission.READ_MEDIA_IMAGES"};
        } else {
            permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
        }

        boolean granted = true;
        for (String perm : permissions) {
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }

        if (granted) {
            pickImages();
        } else {
            requestPermissions(permissions, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            boolean grantedAll = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                    break;
                }
            }
            if (grantedAll) {
                pickImages();
            } else {
                Toast.makeText(this, "请允许访问相册权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
