package com.example.bookapp.models.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;
import com.example.bookapp.R;
import com.example.bookapp.responses.UserResponse;
import com.example.bookapp.interfaces.IUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SessionHandler {
    //Variables
    private Context context;
    private SharedPreferences preferences;

    //Constructor
    public SessionHandler(Context context){
        this.context = context;
        preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    //Functions
    public void refreshToken(){
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(context.getString(R.string.api_base_url))
                .build();

        IUser iUser = retrofit.create(IUser.class);

        Call<UserResponse> call = iUser.refreshToken(getTokenRefresh());
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    saveToken(response.body().getToken());
                    saveTokenRefresh(response.body().getTokenRefresh());
                }
                else {
                    Toast.makeText(context,
                            String.format("Wrong data."),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(context,
                        "Error refreshing token.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isTokenAlive() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String tokenHour = getTokenHour();
        Date tokenHourFormatted = formatter.parse(tokenHour);
        Date now = Calendar.getInstance().getTime();

        long millis = now.getTime() - tokenHourFormatted.getTime();
        long minutes = (millis/(1000*60)) % 60;

        if(minutes < 30)
            return true;
        else
            return false;
    }

    //Save token in shared preferences
    public void saveToken(String token) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SharedPreferences.Editor editor = preferences.edit();
        Date tokenHour = Calendar.getInstance().getTime();
        String tokenHourFormatted = formatter.format(tokenHour);
        editor.putString("hour_token", tokenHourFormatted);
        editor.putString("token", token);
        editor.commit();
    }

    //Save token refresh in shared preferences
    public void saveTokenRefresh(String tokenRefresh) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token_refresh", tokenRefresh);
        editor.commit();
    }

    public void saveEmail(String email){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.apply();
    }

    public String getToken() {
        String token = preferences.getString("token", null);
        return token;
    }

    public String getTokenHour(){
        String tokenHour = preferences.getString("hour_token", null);
        return tokenHour;
    }

    public String getTokenRefresh() {
        String tokenRefresh = preferences.getString("token_refresh", null);
        return tokenRefresh;
    }

    public String getEmail(){
        String email = preferences.getString("email", null);
        return email;
    }
}
