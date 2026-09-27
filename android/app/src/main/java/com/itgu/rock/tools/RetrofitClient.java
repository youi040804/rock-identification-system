package com.itgu.rock.tools;

import com.itgu.rock.R;
import com.itgu.rock.MyApplication;

import java.util.concurrent.TimeUnit;

import java.util.HashMap;

import okhttp3.ConnectionPool;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    
    private Retrofit retrofit;
    private OkHttpClient mOkHttpClient;//static可以保证单例

    // 修复：将headMap改为静态变量，允许静态方法访问
    private static final HashMap<String, String> headMap = new HashMap<>();


    // 网络配置常量
    private static final int MAX_IDLE_CONNECTIONS = 5;
    private static final int KEEP_ALIVE_DURATION = 30;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int TIMEOUT_CONNECT = 10;
    private static final int TIMEOUT_READ = 30;
    private static final int TIMEOUT_WRITE = 30;

    // 创建 Retrofit 实例
    public static Retrofit getRetrofit(String token) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(token))
                .build();

        return new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 获取 ApiService
    public static ApiService getApiService(String token) {
        return getRetrofit(token).create(ApiService.class);
    }
    public static ApiService getApiService() {
        return getRetrofit(null).create(ApiService.class);
    }
   /* private  void updateHeaderMap(String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("Token", null);
        if (token != null) {
            headMap.put("Authorization", "Bearer " + token);
        }
    }*/
    }
    /**
     * 获取 OkHttpClient 实例
     *
     * @return OkHttpClient 实例
     */
//    public OkHttpClient getOkHttpClient(String token) {
//        if (mOkHttpClient == null) {
////            updateHeaderMap(context);
//            //使用连接池，尽量保持TCP连接的复用，而不是立即关闭。
//
//            if (token != null) {
//                headMap.put("Authorization", token); // 键改为 "Authorization"
//            }
//
//            ConnectionPool connectionPool = new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, KEEP_ALIVE_TIME_UNIT);
//            mOkHttpClient = new OkHttpClient.Builder()
//                    .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
//                    .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
//                    .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
//                    .connectionPool(connectionPool)
//                    /*拦截器的添加*/
//                    .addInterceptor(new BaseInterceptor(headMap))//添加头部信息
//                    .build();
//        }
//        return mOkHttpClient;
//    }

    // 无 token 的公共接口
//    public static ApiService getApiService() {
 //       return getRetrofit(null).create(ApiService.class);
 //   }

//    public Retrofit getInstance(String token) {
 //       if (retrofit == null) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
//            String token = sharedPreferences.getString("Token", null);
//}
 //   public Retrofit getInstance(String token) {
 //       if (retrofit == null) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
//            String token = sharedPreferences.getString("Token", null);


//}

