package com.itgu.rock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.text.method.ScrollingMovementMethod;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.itgu.rock.databinding.ImageMainBinding;
import com.itgu.rock.databinding.RockLoginBinding;
import com.itgu.rock.databinding.RockRegisterBinding;
import com.itgu.rock.tools.AuthDialog;
import com.itgu.rock.tools.HttpService;
import com.itgu.rock.tools.NetworkUtils;
import com.itgu.rock.tools.SelectPhotoContract;
import com.itgu.rock.tools.TakeCameraUri;
import com.itgu.rock.tools.ApiConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Result;
// 导入Java集合类（List、ArrayList）
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

// 导入JSON解析相关类（JSONArray、JSONObject、JSONException）
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

// 导入自定义的岩石概率模型类
import com.itgu.rock.model.RockProbability;

public class RockRecognize extends AppCompatActivity {
    private ImageMainBinding binding;
    private ImageView imageView;
    private Icon icon;
    private TextView textView;
    private ImageButton imageButton;
    private Uri imageUri;
    private Button okButton;
    private TextView result_text;

    private Boolean imageIsReady = false;

    private HashMap<String, Class<?>> rockActivityMap = new HashMap<String, Class<?>>() {{
        put("砾岩", LiYan.class);       // 砾岩 -> LiYan活动
        put("安山岩", Anshan.class);    // 安山岩 -> Anshan活动
        put("花岗岩", huagangyan.class);// 花岗岩 -> HuaGangYan活动（假设已创建）
        put("石灰岩", shihuiyan.class); // 石灰岩 -> ShiHuiYan活动（假设已创建）
        put("石英岩", shiyingyan.class);
        put("板岩", banyan.class);
        put("页岩", yeyan.class);
        put("玄武岩", xuanwuyan.class);
        put("角砾岩", jiaoliyan.class);
        put("碳酸盐岩", tansuanyanyan.class);
        put("硅质岩", guizhiyan.class);
        put("杂积岩", zajiyan.class);
        put("白云石", baiyunshi.class);
        put("燧石", suishi.class);
        put("辉长岩", huichangyan.class);
        put("片麻岩", pianmayan.class);
        put("角页岩", jiaoyeyan.class);
        put("大理岩", daliyan.class);
        put("泥岩", niyan.class);
        put("油页岩", youyeyan.class);
        put("鲕粒岩", erliyan.class);
        put("伟晶岩", weijingyan.class);
        put("千枚岩", qianmeiyan.class);
        put("斑岩", bannyan.class);
        put("辉岩", huiyan.class);
        put("流纹岩", liuwenyan.class);
        put("砂岩", shayan.class);
        put("蛇纹岩", shewenyan.class);
        // 补充其他岩石的映射关系
    }};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ImageMainBinding.inflate(LayoutInflater.from(this));

        //准备context
        setContentView(binding.getRoot());
        init();

    }

    // 辅助方法：从结果文本中提取岩石名称
    private String extractRockName(String resultText) {
        // 处理格式如"砾岩（85.0%）"或"安山岩"的文本
        if (resultText.contains("（")) {
            return resultText.substring(0, resultText.indexOf("（"));
        }
        return resultText;
    }


    public void init() {
        //控件注册
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.result_text);
        imageButton = findViewById(R.id.imageButton);

        okButton = findViewById(R.id.uploadButton);
        result_text = findViewById(R.id.result_text);
        // 启用滚动（必须设置，否则滚动条无效）
        result_text.setMovementMethod(ScrollingMovementMethod.getInstance());

        binding.menuBottom.navRecognition.setBackgroundColor(Color.parseColor("#FF0000"));


        binding.menuBottom.navKnowledgeBase.setOnClickListener((View v)->{
            Intent intent = new Intent(RockRecognize.this, Knowledge.class);
            startActivity(intent);
            finish();
        });
//        binding.menuBottom.navModelManagement.setOnClickListener((View v)->{
//            Intent intent = new Intent(RockRecognize.this, ModleCtro.class);
//            startActivity(intent);
//            finish();
//        });

        //传token
            SharedPreferences sharedPreferences = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String token = sharedPreferences.getString("Token", null);
        // 初始化 networkUtils

        NetworkUtils networkUtils = new NetworkUtils(this,token);
        binding.menuBottom.navModelManagement.setOnClickListener((View v) -> {
            AuthDialog.show(RockRecognize.this, networkUtils);
        });


        binding.menuBottom.navPersonalCenter.setOnClickListener((View v)->{
            Intent intent = new Intent(RockRecognize.this, UserCenter.class);
            startActivity(intent);
            finish();
        });


        imageButton.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(this, v);

            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            // 设置点击事件
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.from_gallery) {
                    chooseFromGallery();
                    return true;

                } else if (item.getItemId() == R.id.action_take_photo) {
                    takePhoto();
                    return true;
                } else if (item.getItemId() == R.id.action_cancel) {
                    popup.dismiss();
                    return true;
                }
                return false;
            });
            // 显示弹出菜单
            popup.show();
        });

        okButton.setOnClickListener(v->{
            //获取imageview得图片
//            Drawable drawable = imageView.getDrawable();
//            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap = null;

            if(imageIsReady) {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }else {
                Toast.makeText(RockRecognize.this,"你的图片呢？",Toast.LENGTH_LONG).show();
            }

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jpg");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //传token
//            SharedPreferences sharedPreferences = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
//            String token = sharedPreferences.getString("Token", null);

            HttpService httpService = new HttpService(token);

            String url = ApiConfig.url("rock/recognize");
            //httpService.sendAsyncRequest(url, file, new HttpService.Callback() {

            httpService.sendRockRecognizeRequest(url, file, new HttpService.Callback() {
                /*@Override
                public void onSuccess(String response) {

                     * Handler handler = new Handler(Looper.getMainLooper)
                     * handler.post(new Runnable)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RockRecognize.this,response,Toast.LENGTH_LONG).show();

                            result_text.setText(response);
                        }
                    });

                }*/
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 1. 第一步：解析外层 ApiResponse 对象（必须先处理）
                                JSONObject apiResponseObj = new JSONObject(response);

                                // 2. 验证响应状态：code=200 表示后端处理成功（对应 ApiResponse.success）
                                int responseCode = apiResponseObj.getInt("code");
                                if (responseCode != 200) {
                                    // 后端返回错误（如 code=500），提示错误信息
                                    String errorMsg = apiResponseObj.getString("message");
                                    result_text.setText("识别失败：" + errorMsg);
                                    Toast.makeText(RockRecognize.this, "识别失败：" + errorMsg, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // 3. 第二步：从 data 字段获取真正的岩石识别数组（核心步骤）
                                JSONArray rockJsonArray = apiResponseObj.getJSONArray("data");
                                //log.d("RockParseSuccess", "解析到岩石数量: " + rockJsonArray.length());

                                // 4. 第三步：解析岩石数组为 RockProbability 列表（原逻辑保留，仅调整数据源）
                                List<RockProbability> rockList = new ArrayList<>();
                                for (int i = 0; i < rockJsonArray.length(); i++) {
                                    JSONObject rockObj = rockJsonArray.getJSONObject(i);
                                    String rockName = rockObj.getString("name");
                                    // 将 Python 返回的小数概率（0-1）转为百分比（0-100）
                                    float probability =(float) (rockObj.getDouble("confidence") * 100);
                                    rockList.add(new RockProbability(rockName, probability));
                                }

                                // 5. 第四步：筛选高概率岩石（原逻辑保留，可根据需求调整阈值）
                                List<RockProbability> highProbRocks = new ArrayList<>();
                                for (RockProbability rock : rockList) {
                                    if (rock.getProbability() >= 10) { // 筛选概率≥10%的岩石
                                        highProbRocks.add(rock);
                                    }
                                }

                                // 6. 第五步：处理并显示结果（原逻辑保留）
                                String resultText;
                                if (highProbRocks.isEmpty()) {
                                    resultText = "识别结果：未发现高概率岩石（<10%）";
                                } else if (highProbRocks.size() == 1) {
                                    RockProbability topRock = highProbRocks.get(0);
                                    resultText = String.format("%s（%.1f%%）", topRock.getName(), topRock.getProbability());
                                } else {
                                    // 多高概率结果，按概率降序排序
                                    highProbRocks.sort((r1, r2) -> Float.compare(r2.getProbability(), r1.getProbability()));
                                    float maxProb = highProbRocks.get(0).getProbability();
                                    float minProb = highProbRocks.get(highProbRocks.size() - 1).getProbability();

                                    if (maxProb - minProb <= 10) {
                                        // 概率差值≤10%，显示所有高概率结果
                                        StringBuilder sb = new StringBuilder();
                                        for (RockProbability rock : highProbRocks) {
                                            sb.append(String.format("%s（%.1f%%）\n", rock.getName(), rock.getProbability()));
                                        }
                                        resultText = sb.substring(0, sb.length() - 1); // 移除最后一个换行
                                    } else {
                                        // 概率差值大，仅显示最高概率
                                        RockProbability topRock = highProbRocks.get(0);
                                        resultText = String.format("%s（%.1f%%）", topRock.getName(), topRock.getProbability());
                                    }
                                }

                                // 7. 显示最终结果
                                result_text.setText(resultText);
                                Toast.makeText(RockRecognize.this, resultText, Toast.LENGTH_LONG).show();

                            } catch (JSONException e) {
                                // 解析异常时打印详细日志，便于定位问题
                                String errorMsg = "数据解析失败：" + e.getMessage();
                                Log.e("RockParseError", errorMsg + "\n原始响应: " + response, e);
                                result_text.setText("识别失败：数据结构错误");
                                Toast.makeText(RockRecognize.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                @Override
                public void onFailure(IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String errorMsg = "连接失败：" + e.getMessage();
                            Log.e("RockRecognizeError", errorMsg, e);  // 打印完整错误栈
                            Toast.makeText(RockRecognize.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                }

            });
        });
        result_text.setOnClickListener(v -> {
            String result = result_text.getText().toString().trim();
            if (result.isEmpty()) return;

            // 提取岩石名称（例如从"砾岩（85.0%）"中提取"砾岩"）
            String rockName = extractRockName(result);

            // 根据名称匹配目标Activity
            Class<?> targetActivity = rockActivityMap.get(rockName);
            if (targetActivity != null) {
                // 跳转至对应的岩石知识库界面
                Intent intent = new Intent(RockRecognize.this, targetActivity);
                startActivity(intent);
                finish(); // 可选：关闭当前识别界面
            } else {
                Toast.makeText(RockRecognize.this, "未找到该岩石的知识库", Toast.LENGTH_SHORT).show();
            }
        });


        binding.outLogin.setOnClickListener((View v)-> {
            Intent intent = new Intent(RockRecognize.this, UserFeed.class);
            startActivity(intent);
            finish();
        } );

        /*binding.resultText.setOnClickListener(v->{
            Intent intent = new Intent(RockRecognize.this, LiYan.class);
            startActivity(intent);
            finish();
        });*/
    }


    // 从相册选择图片
    ActivityResultLauncher<Intent> selectPhotoLauncher = registerForActivityResult
            (new SelectPhotoContract(),
                    uri -> {
                        // 处理返回的 uri
                        //这里把imageview设置成image，然后给uri赋值
                        imageView.setImageURI(uri);
                        imageUri = uri;
                        imageIsReady = true;
                    });


    ActivityResultLauncher<Object> takePhotoLauncher = registerForActivityResult(
            new TakeCameraUri(),
            uri -> {
                imageView.setImageURI(uri);
                imageUri = uri;
                imageIsReady = true;
            }
    );


    public void chooseFromGallery() {
        /**
         * 打开相册
         */
        selectPhotoLauncher.launch(null);
    }

    //拍照
    public void takePhoto() {

        takePhotoLauncher.launch(null);

    }

}