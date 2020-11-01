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
import com.example.bookapp.userAPI.UserRequest;
import com.example.bookapp.userAPI.UserResponse;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    //Variables
    private EditText editTextName, editTextLastName, editTextDNI, editTextEmailRegister, editTextPasswordRegister, editTextCommission;
    private Button buttonRegisterRegister;
    private SessionManager sessionManager;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        //Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(getApplicationContext());

        //IDs screen elements
        editTextName = findViewById(R.id.editTextName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextDNI = findViewById(R.id.editTextDNI);
        editTextEmailRegister = findViewById(R.id.editTextEmailRegister);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        editTextCommission = findViewById(R.id.editTextCommission);
        buttonRegisterRegister = findViewById(R.id.buttonRegisterRegister);

        //Listeners
        buttonRegisterRegister.setOnClickListener(buttonRegisterListener);
    }

    private View.OnClickListener buttonRegisterListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!existInternetConnection()) {
                Toast.makeText(RegisterActivity.this, String.format("There is no Internet connection."), Toast.LENGTH_LONG).show();
                return;
            }
            if (!validateFields())
                return;

            UserRequest request = new UserRequest();
            request.setEnv("PROD");
            request.setName(editTextName.getText().toString());
            request.setLastName(editTextLastName.getText().toString());
            request.setDni(Long.parseLong(editTextDNI.getText().toString()));
            request.setEmail(editTextEmailRegister.getText().toString());
            request.setPassword(editTextPasswordRegister.getText().toString());
            request.setCommission(Integer.parseInt(editTextCommission.getText().toString()));

            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(getString(R.string.api_base_url))
                    .build();
            IUser iUser = retrofit.create(IUser.class);

            Call<UserResponse> call = iUser.registrarUsuario(request);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful()) {
                        sessionManager.saveEmail(request.getEmail());
                        sessionManager.saveToken(response.body().getToken());
                        sessionManager.saveTokenRefresh(response.body().getTokenRefresh());
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Wrong data.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Register failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private boolean existInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }

    private boolean validateFields() {
        if(editTextName.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Name is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextLastName.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Last name is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextDNI.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "DNI is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextEmailRegister.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Email is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextPasswordRegister.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Password is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextPasswordRegister.getText().toString().length() < 8) {
            Toast.makeText(RegisterActivity.this, "Password requires al least 8 characters.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(editTextCommission.getText().toString().equals("")) {
            Toast.makeText(RegisterActivity.this, "Commission is required.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}