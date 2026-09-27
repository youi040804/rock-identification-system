package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.HuichangyanLayoutBinding;

public class huichangyan extends AppCompatActivity {
    private HuichangyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定辉长岩布局文件（对应gabbro_layout.xml）
        binding = HuichangyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(huichangyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}