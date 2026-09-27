package com.itgu.rock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import com.itgu.rock.pojo.ApiResponse;
import com.itgu.rock.pojo.LoginResp;
import com.itgu.rock.saveUtils.SpUtils;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RadioGroup;

import com.itgu.rock.databinding.ActivityMainBinding;
import com.itgu.rock.databinding.RockLoginBinding;
import com.itgu.rock.tools.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private NetworkUtils networkUtils ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        //准备context
        setContentView(binding.getRoot());

        //这个类只实现跳转逻辑
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("Token", null);

        networkUtils = new NetworkUtils(this,token);



        if(token != null && !token.isEmpty()){
            // Token存在，验证Token的有效性
            Call<ApiResponse<String>> loginTestTokenCall = networkUtils.getToken();

            loginTestTokenCall.enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<String> body = response.body();
                        if(body.getCode() == 1){
                            //token有效  直接进主界面
                            Intent intent = new Intent(MainActivity.this, RockRecognize.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(MainActivity.this, Login.class);
                            startActivity(intent);
                            finish();
                        }

                    }else {
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            // Token不存在，跳转到登录界面
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }

        //下面实现导航栏的跳转逻辑
        binding.rgTab.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.rb_find)
                {
                    binding.viewpager.setCurrentItem(0);
                } else if (i == R.id.rb_people) {
                    binding.viewpager.setCurrentItem(1);
                } else if (i == R.id.rb_find) {
                    binding.viewpager.setCurrentItem(2);
                } else if (i == R.id.rb_me) {
                    binding.viewpager.setCurrentItem(3);
                }

            }
        });


    }


}