package com.itgu.rock.tools;

import com.itgu.rock.pojo.ApiResponse;
import com.itgu.rock.pojo.FeedbackItem;
import com.itgu.rock.pojo.LoginRequestDTO;
import com.itgu.rock.pojo.LoginResp;
import com.itgu.rock.pojo.ModelParams;
import com.itgu.rock.pojo.UserRegisterDTO;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/user/testtoken")
    Call<ApiResponse<String>> userTokenTestApi();

    @GET("/mail/sendCode")
    Call<ApiResponse<Void>> sendCode(@Query("email") String email);

    @POST("/user/login")
    Call<ApiResponse<LoginResp>> userLoginAPI(
            @Body LoginRequestDTO loginRequest
    );

    @POST("/user/register")
    Call<ApiResponse<LoginResp>> userRegisterAPI(
            @Body UserRegisterDTO registerDTO
    );

    @POST("/feedback/submit")
    Call<ResponseBody> submitFeedback(
            @Body FeedbackItem feedback
    );

    @POST("/model/updateParams")
    Call<ResponseBody> updateParams(
            @Body ModelParams params
    );

    @Multipart
    @POST("/user/uploadAvatar")
    Call<ResponseBody> uploadAvatar(
            @Part MultipartBody.Part avatar,
            @Query("userId") int userId
    );

    @FormUrlEncoded
    @POST("/user/updateUsername")
    Call<ResponseBody> updateUsername(
            @Field("userId") int userId,
            @Field("username") String username
    );

    @POST("/user/updateGender")
    Call<ResponseBody> updateGender(
            @Query("userId") int userId,
            @Query("gender") String gender
    );

    @FormUrlEncoded
    @POST("/user/changePassword")
    Call<ApiResponse> changePassword(
            @Field("oldPassword") String oldPassword,
            @Field("newPassword") String newPassword
    );

    @POST("/modelAuth/check")
    Call<Map<String, Object>> modelAuthCheck(
            @Header("Authorization") String authHeader,
            @Body Map<String, String> body
    );
}
