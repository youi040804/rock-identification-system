package com.itgu.rock.adapter;

import com.itgu.rock.tools.ApiConfig;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.itgu.rock.R;
import com.itgu.rock.pojo.FeedbackItem;
import com.itgu.rock.tools.HttpService;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private final List<FeedbackItem> feedbackList;
    private final Context context;
    private final String token;
    private final HttpService httpService;

    public FeedbackAdapter(Context context, List<FeedbackItem> feedbackList, String token) {
        this.context = context;
        this.feedbackList = feedbackList;
        this.token = token;
        this.httpService = new HttpService(token); // 一次性创建 HttpService，复用
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rockNameTV;
        ImageView rockIV;
        Button deleteBtn, addBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rockNameTV = itemView.findViewById(R.id.tv_gravel_name);
            rockIV = itemView.findViewById(R.id.iv_gravel_image);
            deleteBtn = itemView.findViewById(R.id.btn_delete_gravel);
            addBtn = itemView.findViewById(R.id.btn_add_gravel_to_dataset);
        }
    }

    @NonNull
    @Override
    public FeedbackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feedback_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdapter.ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) return;

        FeedbackItem item = feedbackList.get(adapterPosition);
        holder.rockNameTV.setText(item.getRockName());

        // 图片 URL 拼接
        String imageUrl = formatImageUrl(item.getImagePath());
        GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .build());

        Glide.with(context)
                .load(glideUrl)
                .placeholder(R.drawable.camer) // 可选，加载占位图
                .error(R.drawable.camer)
                .into(holder.rockIV);

        // 删除按钮
        holder.deleteBtn.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            String url = ApiConfig.url("feedback/delete/" + feedbackList.get(pos).getId());
            handlePutAction(url, pos, "已删除", "删除失败: ");
        });

        // 补充至训练集按钮
        holder.addBtn.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            String url = ApiConfig.url("feedback/addToTraining/" + feedbackList.get(pos).getId());
            handlePutAction(url, pos, "已补充至训练集", "补充失败: ");
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    private String formatImageUrl(String path) {
        path = path.trim();
        if (!path.startsWith("/")) path = "/" + path;
        return ApiConfig.url(path);
    }

    private void handlePutAction(String url, int pos, String successMsg, String failMsg) {
        RequestBody body = RequestBody.create(new byte[0], MediaType.parse("application/json"));
        httpService.sendPutRequest(url, body, new HttpService.Callback() {
            @Override
            public void onSuccess(String response) {
                ((Activity) context).runOnUiThread(() -> {
                    feedbackList.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, failMsg + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
