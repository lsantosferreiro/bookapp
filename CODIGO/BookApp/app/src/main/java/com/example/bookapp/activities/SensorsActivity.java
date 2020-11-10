package com.example.bookapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookapp.R;
import java.text.DecimalFormat;
import java.util.Objects;

public class SensorsActivity extends AppCompatActivity {
    //Variables
    DecimalFormat floatFormat = new DecimalFormat("###.###");

    //View//
    TextView textViewGyroscopeXValue, textViewGyroscopeYValue, textViewGyroscopeZValue, textViewLightSensorValue;

    //Logic//
    Context context;

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

        setContentView(R.layout.activity_sensors);

        context = getApplicationContext();

        textViewGyroscopeXValue = findViewById(R.id.textViewGyroscopeXValue);
        textViewGyroscopeYValue = findViewById(R.id.textViewGyroscopeYValue);
        textViewGyroscopeZValue = findViewById(R.id.textViewGyroscopeZValue);

        textViewLightSensorValue = findViewById(R.id.textViewLightSensorValue);

        initializeSensorsValues();
    }

    private void initializeSensorsValues() {
        SharedPreferences preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        float gyroscopeX = preferences.getFloat("gyroscopeX", 0);
        float gyroscopeY = preferences.getFloat("gyroscopeY", 0);
        float gyroscopeZ = preferences.getFloat("gyroscopeZ", 0);
        float lightSensor = preferences.getFloat("lightSensor", 0);

        textViewGyroscopeXValue.setText(floatFormat.format(gyroscopeX)+ " deg/s.");
        textViewGyroscopeYValue.setText(floatFormat.format(gyroscopeY)+ " deg/s.");
        textViewGyroscopeZValue.setText(floatFormat.format(gyroscopeZ)+ " deg/s.");
        textViewLightSensorValue.setText(floatFormat.format(lightSensor)+ " lux.");
    }
}

