package com.itgu.rock.model;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientWithLogging {
    public static OkHttpClient getClient(boolean isDebug) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
            if (isDebug) {
                Log.d("OkHttp", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    if (isDebug) {
                        Log.d("HTTP", "URL: " + request.url());
                        Log.d("HTTP", "Headers: " + request.headers().toString());
                    }
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();
    }

}
