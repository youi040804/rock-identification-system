package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.JiaoliyanLayoutBinding;

public class jiaoliyan extends AppCompatActivity {
    private JiaoliyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定角砾岩布局文件（对应breccia_layout.xml）
        binding = JiaoliyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(jiaoliyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}
