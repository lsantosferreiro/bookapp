package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp.R;
import com.example.bookapp.interfaces.IUser;
import com.example.bookapp.models.firebase.Firebase;
import com.example.bookapp.models.firebase.User;
import com.example.bookapp.models.session.SessionHandler;
import com.example.bookapp.requests.UserEventRequest;
import com.example.bookapp.responses.UserEventResponse;
import com.example.bookapp.requests.UserRequest;
import com.example.bookapp.responses.UserResponse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    //Variables
    private String userEmail;

    //View//
    private EditText editTextEmailLogin, editTextPasswordLogin;
    private Button buttonLogin, buttonRegister;

    //Logic//
    private static Context context;
    private SessionHandler sessionHandler;
    private FirebaseDatabase firebase;
    private DatabaseReference usersDB;
    private User user;

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

        context = getApplicationContext();

        //Show toast with | status
        checkBattery();

        sessionHandler = new SessionHandler(getApplicationContext());

        firebase = Firebase.getInstance();
        usersDB = firebase.getReference(getString(R.string.db_users));

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

            userEmail = editTextEmailLogin.getText().toString();
            request.setEmail(userEmail);
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
                        checkUserIntegrity(request);
                        sessionHandler.saveEmail(request.getEmail());
                        sessionHandler.saveToken(response.body().getToken());
                        sessionHandler.saveTokenRefresh(response.body().getTokenRefresh());
                        registerLoginEvent();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
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

    //Verify if the user exists in firebase
    //Create the user if It does not exist
    private void checkUserIntegrity(UserRequest request) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    user = new User();
                    user.setEmail(request.getEmail());
                    createUser(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DB Error.", databaseError.getMessage());
            }
        };
        Query queryUser = usersDB.orderByChild("email").equalTo(userEmail);
        queryUser.addValueEventListener(eventListener);
    }

    private void createUser(User user) {
        usersDB.push().setValue(user);
    }

    private View.OnClickListener buttonRegisterListener = v -> {
        Intent registerActivityIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerActivityIntent);
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

        Call<UserEventResponse> call = iUser.registerEvent("Bearer " + sessionHandler.getToken(), request);
        call.enqueue(new Callback<UserEventResponse>() {
            @Override
            public void onResponse(Call<UserEventResponse> call, Response<UserEventResponse> response) {
                if(!response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Wrong data.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserEventResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Register login event failed.", Toast.LENGTH_SHORT).show();
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

    private void checkBattery() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percent = level * 100 / scale;

        if(percent >= 80) {
            Toast.makeText(this, percent + "% - High battery.", Toast.LENGTH_SHORT).show();
        }
        else if(percent <= 20) {
            Toast.makeText(this, percent + "% - Low battery.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, percent + "% - Normal battery.", Toast.LENGTH_SHORT).show();
        }
    }
}