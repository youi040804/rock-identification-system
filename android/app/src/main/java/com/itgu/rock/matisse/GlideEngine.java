package com.itgu.rock.matisse;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhihu.matisse.engine.ImageEngine;

import android.net.Uri;

public class GlideEngine implements ImageEngine {
    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(context).load(uri).override(resizeX, resizeY).into(imageView);
    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(context).asGif().load(uri).override(resizeX, resizeY).into(imageView);
    }

    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        Glide.with(context).load(uri).override(resize, resize).placeholder(placeholder).into(imageView);
    }

    @Override
    public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        Glide.with(context).asGif().load(uri).override(resize, resize).placeholder(placeholder).into(imageView);
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }
}
