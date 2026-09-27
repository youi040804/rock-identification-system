package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.ShiyingyanLayoutBinding;

public class shiyingyan extends AppCompatActivity {
    private ShiyingyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定石英岩布局文件（需创建shiyingyan.xml布局）
        binding = ShiyingyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(shiyingyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}