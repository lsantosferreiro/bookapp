package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp.R;
import com.example.bookapp.activities.utils.LightSensorEventAsync;
import com.example.bookapp.models.firebase.Firebase;
import com.example.bookapp.models.firebase.User;
import com.example.bookapp.models.session.SessionHandler;
import com.example.bookapp.models.userEventsAPI.UserEventsAPI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ReadingsActivity extends AppCompatActivity implements SensorEventListener {
    //Variables
    private String userEmail;
    private String readings;

    //View//
    private TextView textViewTotalReadingsValue;
    private Button buttonExplore;

    //Logic//
    private static Context context;
    private FirebaseDatabase firebase;
    private DatabaseReference usersDB;
    private SessionHandler sessionHandler;
    private SensorManager sensorManager;
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

        setContentView(R.layout.activity_readings);

        context = getApplicationContext();

        sessionHandler = new SessionHandler(context);

        firebase = Firebase.getInstance();
        usersDB = firebase.getReference(getString(R.string.db_users));

        getUser();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initializeSensors();

        buttonExplore = findViewById(R.id.buttonExploreReadings);

        buttonExplore.setOnClickListener(buttonExploreListener);

        new LightSensorEventAsync(context).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeSensors();
    }

    @Override
    public void onPause() {
        stopSensors();
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopSensors();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        initializeSensors();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        stopSensors();
        super.onDestroy();
    }

    private View.OnClickListener buttonExploreListener = v -> {
        if (!existInternetConnection()) {
            Toast.makeText(ReadingsActivity.this, String.format("There is no Internet connection."), Toast.LENGTH_LONG).show();
            return;
        }
        Intent exploreActivityIntent = new Intent(getApplicationContext(), ExploreActivity.class);
        startActivity(exploreActivityIntent);
        finish();
    };

    private boolean existInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }

    private void initializeReadings() {
        textViewTotalReadingsValue = findViewById(R.id.textViewTotalReadingsValue);
        readings = Integer.toString(user.getReadings());
        textViewTotalReadingsValue.setText(readings);
    }

    private void getUser() {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    user = postSnapshot.getValue(User.class);
                    user.setId(postSnapshot.getKey());
                    initializeReadings();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DB Error.", databaseError.getMessage());
            }
        };

        userEmail = sessionHandler.getEmail();
        Query queryUser = usersDB.orderByChild("email").equalTo(userEmail);
        queryUser.addValueEventListener(eventListener);
    }

    protected void initializeSensors() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopSensors() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    //Save light sensor value in shared preferences
    private void saveLightSensor(float lightValue) {
        SharedPreferences preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("lightSensor", lightValue);
        editor.apply();
    }

    //Save gyroscope values in shared preferences
    private void saveGyroscope(float x, float y, float z) {
        SharedPreferences preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("gyroscopeX", x);
        editor.putFloat("gyroscopeY", y);
        editor.putFloat("gyroscopeZ", z);
        editor.apply();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized(this) {
            switch(event.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    saveGyroscope(event.values[0], event.values[1], event.values[2]);
                    break;
                case Sensor.TYPE_LIGHT :
                    saveLightSensor(event.values[0]);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

