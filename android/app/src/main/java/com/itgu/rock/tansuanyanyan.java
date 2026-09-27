package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.TansuanyanyanLayoutBinding;

public class tansuanyanyan extends AppCompatActivity {
    private TansuanyanyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定碳酸盐岩布局文件（对应carbonate_layout.xml）
        binding = TansuanyanyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(tansuanyanyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}
