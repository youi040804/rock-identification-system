package com.itgu.rock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itgu.rock.databinding.ModleCtroBinding;
import com.itgu.rock.tools.ModelParameterManager;

public class ModleCtro extends AppCompatActivity {
    private ModleCtroBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ModleCtroBinding.inflate(LayoutInflater.from(this));

        //准备context
        setContentView(binding.getRoot());


        binding.menuBottom.navModelManagement.setBackgroundColor(Color.parseColor("#FF0000"));

        binding.menuBottom.navKnowledgeBase.setOnClickListener((View v)->{
            Intent intent = new Intent(ModleCtro.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
        binding.menuBottom.navRecognition.setOnClickListener((View v)->{
            Intent intent = new Intent(ModleCtro.this, RockRecognize.class);
            startActivity(intent);
            finish();
        });
        binding.menuBottom.navPersonalCenter.setOnClickListener((View v)->{
            Intent intent = new Intent(ModleCtro.this, UserCenter.class);
            startActivity(intent);
            finish();
        });


        binding.btnModifyParameters.setOnClickListener(v -> {
            // 调用工具类来处理逻辑
            ModelParameterManager.modify(
                    this,
                    binding.etLearningRate.getText().toString(),
                    binding.etBatchSize.getText().toString(),
                    binding.etEpochs.getText().toString()
            );

            // 清空输入框
            binding.etLearningRate.setText("");
            binding.etBatchSize.setText("");
            binding.etEpochs.setText("");
        });


        binding.btnTrainingSetSupplement.setOnClickListener((View v)->{
            Intent intent = new Intent(ModleCtro.this, AddTrain.class);
            startActivity(intent);
            finish();
        });

        binding.btnUserFeedbackManagement.setOnClickListener((View v)->{
            Intent intent = new Intent(ModleCtro.this, FeedBack.class);
            startActivity(intent);
            finish();
        });

    }
}