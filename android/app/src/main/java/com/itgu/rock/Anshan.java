package com.itgu.rock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.AnshanBinding;
import com.itgu.rock.databinding.LiyanLayoutBinding;

public class Anshan extends AppCompatActivity {
    private AnshanBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = AnshanBinding.inflate(LayoutInflater.from(this));

        //准备context
        setContentView(binding.getRoot());

        binding.lifan.setOnClickListener(v->{
            Intent intent = new Intent(Anshan.this, Knowledge.class);
            startActivity(intent);
            finish();
        });

    }
}
