package com.example.bookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp.userAPI.IUser;
import com.example.bookapp.userAPI.SessionManager;
import com.example.bookapp.userAPI.UserEventRequest;
import com.example.bookapp.userAPI.UserEventResponse;
import com.example.bookapp.userAPI.UserRequest;
import com.example.bookapp.userAPI.UserResponse;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    //Variables
    private EditText editTextEmailLogin, editTextPasswordLogin;
    private Button buttonLogin, buttonRegister;
    private SessionManager sessionManager;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        //Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());

        //IDs screen elements
        editTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);
        buttonRegister = findViewById(R.id.buttonRegisterLogin);
        buttonLogin = findViewById(R.id.buttonLogin);

        //Listeners
        buttonLogin.setOnClickListener(buttonLoginListener);
        buttonRegister.setOnClickListener(buttonRegisterListener);
    }

    private View.OnClickListener buttonLoginListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!existInternetConnection()) {
                Toast.makeText(LoginActivity.this, "There is no Internet connection.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!validateFields())
                return;

            UserRequest request = new UserRequest();

            request.setEmail(editTextEmailLogin.getText().toString());
            request.setPassword(editTextPasswordLogin.getText().toString());

            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(getString(R.string.api_base_url))
                    .build();
            IUser iUser = retrofit.create(IUser.class);

            Call<UserResponse> call = iUser.loginUser(request);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful()) {
                        sessionManager.saveEmail(request.getEmail());
                        sessionManager.saveToken(response.body().getToken());
                        sessionManager.saveTokenRefresh(response.body().getTokenRefresh());
                        registerLoginEvent();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private View.OnClickListener buttonRegisterListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent registerActivityIntent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(registerActivityIntent);
            finish();
        }
    };


    private void registerLoginEvent() {
        UserEventRequest request = new UserEventRequest();
        request.setEnv("PROD");
        request.setTypeEvents("login");
        request.setDescription("Successful login.");

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getString(R.string.api_base_url))
                .build();

        IUser iUser = retrofit.create(IUser.class);

        try {
            if(!sessionManager.isTokenAlive()){
                sessionManager.refreshToken();
            }
        } catch(Exception e){ sessionManager.refreshToken(); }

        Call<UserEventResponse> call = iUser.registerEvent("Bearer " + sessionManager.getToken(), request);
        call.enqueue(new Callback<UserEventResponse>() {
            @Override
            public void onResponse(Call<UserEventResponse> call, Response<UserEventResponse> response) {
                if(!response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Wrong data.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserEventResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Register login failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean existInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean validateFields() {
        if (editTextEmailLogin.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "Email is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextPasswordLogin.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "Password is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(editTextPasswordLogin.getText().toString().length() < 8) {
            Toast.makeText(LoginActivity.this, "Password requires al least 8 characters.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}