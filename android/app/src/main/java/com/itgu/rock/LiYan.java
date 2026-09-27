package com.itgu.rock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.KnowledgeBinding;
import com.itgu.rock.databinding.LiyanLayoutBinding;

public class LiYan extends AppCompatActivity {
    private LiyanLayoutBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = LiyanLayoutBinding.inflate(LayoutInflater.from(this));

        //准备context
        setContentView(binding.getRoot());

        binding.lifan.setOnClickListener(v->{
            Intent intent = new Intent(LiYan.this, RockRecognize.class);
            startActivity(intent);
            finish();
        });

    }
}
