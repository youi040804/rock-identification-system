package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.QianmeiyanLayoutBinding;

public class qianmeiyan extends AppCompatActivity {
    private QianmeiyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定千枚岩布局文件（对应phyllite_layout.xml）
        binding = QianmeiyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(qianmeiyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}