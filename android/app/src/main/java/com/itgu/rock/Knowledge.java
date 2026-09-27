package com.itgu.rock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.itgu.rock.databinding.KnowledgeBinding;
import com.itgu.rock.tools.AuthDialog;
import com.itgu.rock.tools.NetworkUtils;

public class Knowledge extends AppCompatActivity {

    private KnowledgeBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = KnowledgeBinding.inflate(LayoutInflater.from(this));

        //准备context
        setContentView(binding.getRoot());

        binding.menuBottom.navKnowledgeBase.setBackgroundColor(Color.parseColor("#FF0000"));

        binding.menuBottom.navRecognition.setOnClickListener((View v)->{
            Intent intent = new Intent(Knowledge.this, RockRecognize.class);
            startActivity(intent);
            finish();
        });

        //传token
        SharedPreferences sharedPreferences = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("Token", null);
        // 初始化 networkUtils

        NetworkUtils networkUtils = new NetworkUtils(this,token);
        binding.menuBottom.navModelManagement.setOnClickListener((View v) -> {
            AuthDialog.show(Knowledge.this, networkUtils);
        });
//        binding.menuBottom.navModelManagement.setOnClickListener((View v)->{
//            Intent intent = new Intent(Knowledge.this, ModleCtro.class);
//            startActivity(intent);
//            finish();
//        });
        binding.menuBottom.navPersonalCenter.setOnClickListener((View v)->{
            Intent intent = new Intent(Knowledge.this, UserCenter.class);
            startActivity(intent);
            finish();
        });

        binding.tvGravel.setOnClickListener((View v)->{
            Intent intent = new Intent(Knowledge.this, LiYan.class);
            startActivity(intent);
            finish();

        });

        binding.tvAndesite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, Anshan.class);
            startActivity(intent);
            finish();
        });

        binding.tvGranite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, huagangyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvLimestone.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, shihuiyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvQuartzite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, shiyingyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvSlate1.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, banyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvShale2.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, yeyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvBasalt3.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, xuanwuyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvBreccia.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, jiaoliyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvCarbonate.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, tansuanyanyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvSiliceousRock.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, guizhiyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvErmashi.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, zajiyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvDolomite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, baiyunshi.class);
            startActivity(intent);
            finish();
        });

        binding.tvChert.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, suishi.class);
            startActivity(intent);
            finish();
        });

        binding.tvGabbro.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, huichangyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvGneiss.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, pianmayan.class);
            startActivity(intent);
            finish();
        });

        binding.tvHornfels.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, jiaoyeyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvMarble.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, daliyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvMudstone.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, niyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvOilShale.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, youyeyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvOolite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, erliyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvPegmatite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, weijingyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvPhyllite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, qianmeiyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvPorphyry.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, bannyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvPyroxenite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, huiyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvRhyolite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, liuwenyan.class);
            startActivity(intent);
            finish();
        });

        binding.tvSandstone.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, shayan.class);
            startActivity(intent);
            finish();
        });

        binding.tvSerpentinite.setOnClickListener(v->{
            Intent intent = new Intent(Knowledge.this, shewenyan.class);
            startActivity(intent);
            finish();
        });








    }
}
