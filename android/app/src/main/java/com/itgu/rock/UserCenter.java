package com.itgu.rock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.itgu.rock.databinding.UserCerterBinding;
import com.itgu.rock.tools.AuthDialog;
import com.itgu.rock.tools.NetworkUtils;

public class UserCenter extends AppCompatActivity {
    private UserCerterBinding binding;
    private SharedPreferences sharedPreferences;
    private String username;
    private String email;
    private String phone;
    private String gender;

    private String avatarUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 使用 inflater 加载布局
        super.onCreate(savedInstanceState);
        binding = UserCerterBinding.inflate(LayoutInflater.from(this));

        //准备context
        setContentView(binding.getRoot());
        ImageView ivAvatar = binding.ivUserAvatar;
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        updateUserInfo(); // 统一刷新方法


        binding.menuBottom.navPersonalCenter.setBackgroundColor(Color.parseColor("#FF0000"));

        binding.menuBottom.navKnowledgeBase.setOnClickListener((View v)->{
            Intent intent = new Intent(UserCenter.this, Knowledge.class);
            startActivity(intent);
            finish();
        });

        //传token
        SharedPreferences sharedPreferences = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("Token", null);

        // 初始化 networkUtils
        NetworkUtils networkUtils = new NetworkUtils(this,token);
        binding.menuBottom.navModelManagement.setOnClickListener((View v) -> {
            AuthDialog.show(UserCenter.this, networkUtils);
        });
//        binding.menuBottom.navModelManagement.setOnClickListener((View v)->{
//            Intent intent = new Intent(UserCenter.this, ModleCtro.class);
//            startActivity(intent);
//            finish();
//        });
        binding.menuBottom.navRecognition.setOnClickListener((View v)->{
            Intent intent = new Intent(UserCenter.this, RockRecognize.class);
            startActivity(intent);
            finish();
        });

        //跳转设置页面
        //btn_settings
        binding.btnSettings.setOnClickListener((View v)->{
            Intent intent = new Intent(UserCenter.this, Settings.class);
            startActivity(intent);
            finish();
        });

        //退出登录
        binding.btnLogout.setOnClickListener((View v)-> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();  // 清除所有存储的数据
            // 提交修改
            editor.apply();
            Toast.makeText(UserCenter.this, "已退出登录", Toast.LENGTH_LONG).show();

            //跳转到登录界面
            Intent intent = new Intent(UserCenter.this, Login.class);
            startActivity(intent);
            finish();
        } );





    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserInfo();
    }

    private void updateUserInfo() {
        username = sharedPreferences.getString("Username", "默认用户名");
        email = sharedPreferences.getString("Email", "默认邮箱");
        phone = sharedPreferences.getString("Phone", "默认电话");
        gender=sharedPreferences.getString("Gender","未设置性别");
        avatarUrl = sharedPreferences.getString("AvatarUrl", null);

        String avatarUrl = sharedPreferences.getString("AvatarUrl", null);
        if (avatarUrl != null) {
            Glide.with(this)
                    .load(avatarUrl)      // 直接用完整 URL
                    .circleCrop()
                    .into(binding.ivUserAvatar);
        } else {
            binding.ivUserAvatar.setImageResource(R.drawable.user);
        }

        // 确保UI更新在主线程中进行
        runOnUiThread(() -> {
            binding.tvUsername.setText(username);  // 用户名
            binding.tvEmail.setText(email);        // 邮箱
            binding.tvPhone.setText(phone);        // 电话
            binding.tvGender.setText(gender);//性别
        });
    }
}
