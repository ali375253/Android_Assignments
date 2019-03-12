package com.promactinfo.aliasagar.chatapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApplicationService {
    //@Headers("Content-Type:application/json")
    @POST("user/login")
    Call<User> Login(@Body User user);
    @GET("user")
    Call<List<User>> getAllUsers(@Header("Authorization") String token);
    @GET("chat/{id}")
    Call<List<Message>> getUserMessages(@Path("id") int id,@Header("Authorization") String token);
    @POST("chat")
    Call<Void> sendMessage(@Body Message message,@Header("Authorization") String token);

}
