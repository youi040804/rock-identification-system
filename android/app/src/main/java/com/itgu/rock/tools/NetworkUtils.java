package com.itgu.rock.tools;

import android.content.Context;
import android.util.Log;

import com.itgu.rock.R;
import com.itgu.rock.pojo.ApiResponse;
import com.itgu.rock.pojo.LoginRequestDTO;
import com.itgu.rock.pojo.LoginResp;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtils {
    private String token;
    private Context context;

    private ApiService apiService;
    RetrofitClient retrofitClient = new RetrofitClient();


public NetworkUtils(Context context, String token) {
    this.context = context;
    this.token = token;

    String baseUrl = ApiConfig.BASE_URL;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    apiService = retrofit.create(ApiService.class);
}

   // private final ApiService apiService = RetrofitClient.getApiService(token);
    /**
     * 登录主要接口
     * @param username
     * @param password
     * @return
     */

    // 修改 Retrofit 接口中的返回类型
    public Call<ApiResponse<LoginResp>> postLogin(String username, String password) {
        LoginRequestDTO loginRequest = new LoginRequestDTO(username, password);
        return apiService.userLoginAPI(loginRequest);
    }


    public Call<ApiResponse<String>> getToken(){
        Call<ApiResponse<String>> dataCall = apiService.userTokenTestApi();

        return dataCall;
    }

    public String GetToken() {
        return token;
    }


    public Call<Map<String, Object>> getModelAuthCheck(Map<String, String> body) {

        return apiService.modelAuthCheck("Bearer " + token, body);
    }

}
