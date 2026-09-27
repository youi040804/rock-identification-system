package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.GuizhiyanLayoutBinding;

public class guizhiyan extends AppCompatActivity {
    private GuizhiyanLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定硅质岩布局文件（对应siliceous_rock_layout.xml）
        binding = GuizhiyanLayoutBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        // 返回按钮逻辑：跳转至知识库首页
        binding.lifan.setOnClickListener(v -> {
            Intent intent = new Intent(guizhiyan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
    }
}
