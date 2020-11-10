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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp.R;
import com.example.bookapp.activities.utils.LightSensorEventAsync;
import com.example.bookapp.models.session.SessionHandler;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {
    //Variables
    //View//
    private TextView textViewEmailHome;
    private Button buttonExplore, buttonReadings, buttonSensors;

    //Logic//
    private static Context context;
    private SessionHandler sessionHandler;
    private SensorManager sensorManager;

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

        setContentView(R.layout.activity_home);

        context = getApplicationContext();

        sessionHandler = new SessionHandler(context);

        initializeData();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initializeSensors();

        setButtonsListeners();

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

    private void setButtonsListeners() {
        //IDs screen elements
        buttonExplore = findViewById(R.id.buttonExploreHome);
        buttonReadings = findViewById(R.id.buttonReadingsHome);
        buttonSensors= findViewById(R.id.buttonSensors);

        //Listeners
        buttonExplore.setOnClickListener(buttonExploreListener);
        buttonReadings.setOnClickListener(buttonReadingsListener);
        buttonSensors.setOnClickListener(buttonSensorsListener);
    }

    private void initializeData() {
        textViewEmailHome = findViewById(R.id.textViewEmailHome);
        textViewEmailHome.setText(sessionHandler.getEmail());
    }

    private View.OnClickListener buttonExploreListener = v -> {
        //Verify Internet connection
        if (!existInternetConnection()) {
            Toast.makeText(HomeActivity.this, String.format("There is no Internet connection."), Toast.LENGTH_LONG).show();
            return;
        }
        Intent exploreActivityIntent = new Intent(getApplicationContext(), ExploreActivity.class);
        startActivity(exploreActivityIntent);
    };

    private View.OnClickListener buttonReadingsListener = v -> {
        //Verify Internet connection
        if (!existInternetConnection()) {
            Toast.makeText(HomeActivity.this, String.format("There is no Internet connection."), Toast.LENGTH_LONG).show();
            return;
        }
        Intent readingsActivityIntent = new Intent(getApplicationContext(), ReadingsActivity.class);
        startActivity(readingsActivityIntent);
    };

    private View.OnClickListener buttonSensorsListener = v -> {
        Intent sensorsActivityIntent = new Intent(getApplicationContext(), SensorsActivity.class);
        startActivity(sensorsActivityIntent);
    };

    private boolean existInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
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