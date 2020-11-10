package com.example.bookapp.interfaces;

import com.example.bookapp.requests.UserEventRequest;
import com.example.bookapp.requests.UserRequest;
import com.example.bookapp.responses.UserEventResponse;
import com.example.bookapp.responses.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface IUser {
    @POST("register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    @POST("login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    @POST("event")
    Call<UserEventResponse> registerEvent(@Header("Authorization") String token, @Body UserEventRequest request);

    @PUT("refresh")
    Call<UserResponse> refreshToken(@Header("Authorization") String tokenRefresh);
}
