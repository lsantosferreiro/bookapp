package com.example.bookapp.models.userEventsAPI;

import com.example.bookapp.requests.UserEventRequest;
import com.example.bookapp.responses.UserEventResponse;
import com.example.bookapp.interfaces.IUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserEventsAPI {
    //Variables
    //Logic//
    private static UserEventsAPI userEventsAPI;
    public static IUser iUser;

    //Constructor
    private UserEventsAPI(){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://so-unlam.net.ar/api/api/")
                .build();
        iUser = retrofit.create(IUser.class);
    }

    //Singleton
    public static UserEventsAPI instance(){
        if(userEventsAPI == null)
            userEventsAPI = new UserEventsAPI();
        return userEventsAPI;
    }

    public void registerEvent(String token, UserEventRequest request, Callback<UserEventResponse> callback) {
        Call<UserEventResponse> userCall = iUser.registerEvent("Bearer " + token, request);
        userCall.enqueue(callback);
    }
}