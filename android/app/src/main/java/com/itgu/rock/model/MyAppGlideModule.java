package com.itgu.rock.model;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import java.io.InputStream;
/// #####这个文件用来用 Glide 显示网络图片，并且打印日志、规定缓存
@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setMemoryCache(new LruResourceCache(20 * 1024 * 1024)); // 20MB
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull com.bumptech.glide.Registry registry) {
        // 用自定义 OkHttpClient 替换 Glide 默认网络
        registry.replace(
                GlideUrl.class,
                InputStream.class,
                new OkHttpUrlLoader.Factory((okhttp3.Call.Factory) OkHttpClientWithLogging.getClient(true))
        );

    }
}
