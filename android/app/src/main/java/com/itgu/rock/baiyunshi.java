package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.BaiyunshiLayoutBinding;

public class baiyunshi extends AppCompatActivity {
    private BaiyunshiLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定白云石布局文件（对应dolomite_layout.xml）
        binding = BaiyunshiLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(baiyunshi.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}
