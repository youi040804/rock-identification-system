package com.itgu.rock.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itgu.rock.R;

import java.io.File;
import java.util.List;
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final List<File> images;
    private final Context context;
    private OnAddClickListener listener;

    public ImageAdapter(Context context, List<File> images) {
        this.context = context;
        this.images = images;
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.addtrain_item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isAddButton(position)) {
            //holder.imageView.setImageResource(R.drawable.1); // 加号图片

            holder.imageView.setImageResource(R.drawable.add); // 加号图片
            holder.btnDelete.setVisibility(View.GONE);
            holder.imageView.setOnClickListener(v -> showAddMenu(v));
        } else {
            holder.imageView.setImageURI(Uri.fromFile(images.get(position)));
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                images.remove(position);
                notifyDataSetChanged();
            });
        }
    }

    private boolean isAddButton(int position) {
        return position == images.size(); // 最后一个位置显示加号
    }

    private void showAddMenu(View view) {
        if (images.size() >= 9) {
            Toast.makeText(context, "最多只能上传9张图片", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.from_gallery) {
                if (listener != null) listener.onPickImages(); // <- 这里调用接口
                return true;
            } else if (item.getItemId() == R.id.action_take_photo) {
                if (listener != null) listener.onTakePhoto();  // <- 这里调用接口
                return true;
            } else if (item.getItemId() == R.id.action_cancel) {
                popup.dismiss();
                return true;
            }
            return false;
        });

        popup.show();
    }



    @Override
    public int getItemCount() {
        return images.size() >= 9 ? 9 : images.size() + 1; // 超过9张就不显示加号
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface OnAddClickListener {
        void onTakePhoto();
        void onPickImages();

    }
}
