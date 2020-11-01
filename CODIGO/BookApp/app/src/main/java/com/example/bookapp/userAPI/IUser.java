package com.example.bookapp.userAPI;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface IUser {
    @POST("register")
    Call<UserResponse> registrarUsuario(@Body UserRequest request);

    @POST("login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    @POST("event")
    Call<UserEventResponse> registerEvent(@Header("Authorization") String token, @Body UserEventRequest request);

    @PUT("refresh")
    Call<UserResponse> refreshToken(@Header("Authorization") String tokenRefresh);
}
