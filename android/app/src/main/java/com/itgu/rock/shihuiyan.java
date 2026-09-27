package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.ShihuiyanLayoutBinding;

public class shihuiyan extends AppCompatActivity {
    private ShihuiyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定石灰岩布局文件（需创建shuihuiyan.xml布局）
        binding = ShihuiyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(shihuiyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}