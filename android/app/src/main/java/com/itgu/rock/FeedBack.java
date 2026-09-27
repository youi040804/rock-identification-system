package com.itgu.rock;

import com.itgu.rock.tools.ApiConfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.itgu.rock.adapter.FeedbackAdapter;
import com.itgu.rock.databinding.FeedbackBinding;
import com.itgu.rock.pojo.FeedbackItem;
import com.itgu.rock.tools.HttpService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedBack extends AppCompatActivity {

    private FeedbackBinding binding;
    private FeedbackAdapter adapter;
    private List<FeedbackItem> feedbackList = new ArrayList<>();

    private String token;
    private HttpService httpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 读取 Token 和 Backend IP
        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = sp.getString("Token", null);

        // 初始化 HttpService
        httpService = new HttpService(token);

        // 返回按钮
        binding.fan.setOnClickListener(v -> {
            Intent intent = new Intent(FeedBack.this, ModleCtro.class);
            startActivity(intent);
            finish();
        });

        // 初始化 RecyclerView
        adapter = new FeedbackAdapter(this, feedbackList, token);
        binding.recyclerFeedback.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFeedback.setAdapter(adapter);

        // 加载反馈列表
        loadFeedbackFromServer();
    }

    private void loadFeedbackFromServer() {
        String url = buildUrl("/feedback/list");
        httpService.getFeedbackList(url, new HttpService.Callback() {

            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        feedbackList.clear();
                        feedbackList.addAll(parseFeedbackList(response));
                        adapter.notifyDataSetChanged();

                        if (feedbackList.isEmpty()) {
                            showToast("暂无反馈数据");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast("解析反馈数据失败");
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                showToast("加载反馈失败：" + e.getMessage());
            }
        });
    }

    /** 解析 JSON 为反馈列表 */
    private List<FeedbackItem> parseFeedbackList(String json) throws JSONException {
        List<FeedbackItem> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            list.add(new FeedbackItem(
                    obj.optLong("id", -1),
                    obj.optString("rockName", "未知名称"),
                    obj.optString("imagePath", "")
            ));
        }
        return list;
    }

    /** UI 线程安全 Toast */
    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(FeedBack.this, msg, Toast.LENGTH_SHORT).show());
    }

    /** 拼接完整 URL */
    private String buildUrl(String path) {
        return ApiConfig.url(path);
    }
}
