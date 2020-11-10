package com.example.bookapp.activities.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.example.bookapp.models.session.SessionHandler;
import com.example.bookapp.models.userEventsAPI.UserEventsAPI;
import com.example.bookapp.requests.UserEventRequest;
import com.example.bookapp.responses.UserEventResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LightSensorEventAsync extends AsyncTask<Void, String, String> {
    //Variables
    private boolean flagLightSensor = true;

    //Logic//
    private Context context;
    private UserEventsAPI userEventsAPI;
    private SessionHandler sessionHandler;
    private SharedPreferences preferences;
    float lightValue;


    public LightSensorEventAsync(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        lightValue = preferences.getFloat("lightSensor", 0);
        sessionHandler = new SessionHandler(context);
    }


    @Override
    protected String doInBackground(Void... voids) {
        if (lightValue > 50)
            registerLightSensor();

        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {

    }


    @Override
    protected void onProgressUpdate(String ...string) {
        Toast.makeText(context, string[0], Toast.LENGTH_SHORT).show();
    }

    public void registerLightSensor() {
        userEventsAPI = UserEventsAPI.instance();

        //Refresh the token if It is not alive
        try {
            if(!sessionHandler.isTokenAlive()){
                sessionHandler.refreshToken();
            }
        } catch(Exception e) {
            sessionHandler.refreshToken();
        }
        UserEventRequest request = new UserEventRequest();
        request.setEnv("PROD");
        request.setTypeEvents("lightSensor");
        request.setDescription("Light sensor value is over 50 lux.");

        userEventsAPI.registerEvent(sessionHandler.getToken(), request, new Callback<UserEventResponse>() {
            @Override
            public void onResponse(Call<UserEventResponse> call, Response<UserEventResponse> response) {
                if (response.isSuccessful()) {
                    publishProgress("Light sensor value is over 50 lux.");
                }
                else {
                    publishProgress("Wrong data.");
                }
            }
            @Override
            public void onFailure(Call<UserEventResponse> call, Throwable t) {
                publishProgress("Register light sensor event failed.");
            }
        });
    }

}

