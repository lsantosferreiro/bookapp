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
import com.example.bookapp.models.firebase.Book;
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

public class ExploreActivity extends AppCompatActivity implements SensorEventListener {
    //Variables
    private String userEmail;
    private int i = 0;
    private int readings;

    //View//
    private TextView textViewExplore;
    private Button buttonLike, buttonReadings;

    //Logic//
    private static Context context;
    private FirebaseDatabase firebase;
    private DatabaseReference usersDB, booksDB;
    private SessionHandler sessionHandler;
    private SensorManager sensorManager;
    private Book book;
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

        setContentView(R.layout.activity_explore);

        context = getApplicationContext();

        sessionHandler = new SessionHandler(context);

        firebase = Firebase.getInstance();
        usersDB = firebase.getReference(getString(R.string.db_users));
        booksDB = firebase.getReference(getString(R.string.db_books));
        booksDB.keepSynced(true);

        getBooks();

        getUser();

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
        //Like buttons//
        buttonLike = findViewById(R.id.buttonLike1);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike2);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike3);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike4);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike5);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike6);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike7);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike8);
        buttonLike.setOnClickListener(likeListener);
        buttonLike = findViewById(R.id.buttonLike9);
        buttonLike.setOnClickListener(likeListener);

        //Readings button//
        buttonReadings = findViewById(R.id.buttonReadingsExplore);
        buttonReadings.setOnClickListener(buttonReadingsListener);
    }

    private void chargeBooks() {
        if(i == 0) {
            textViewExplore = findViewById(R.id.textViewExplore1);
        }
        else if(i == 1) {
            textViewExplore = findViewById(R.id.textViewExplore2);
        }
        else if(i == 2) {
            textViewExplore = findViewById(R.id.textViewExplore3);
        }
        else if(i == 3) {
            textViewExplore = findViewById(R.id.textViewExplore4);
        }
        else if(i == 4) {
            textViewExplore = findViewById(R.id.textViewExplore5);
        }
        else if(i == 5) {
            textViewExplore = findViewById(R.id.textViewExplore6);
        }
        else if(i == 6) {
            textViewExplore = findViewById(R.id.textViewExplore7);
        }
        else if(i == 7) {
            textViewExplore = findViewById(R.id.textViewExplore8);
        }
        else if(i == 8) {
            textViewExplore = findViewById(R.id.textViewExplore9);
        }
        textViewExplore.setText(book.getName());
    }

    private View.OnClickListener buttonReadingsListener = v -> {
        //Verify Internet connection
        if (!existInternetConnection()) {
            Toast.makeText(ExploreActivity.this, String.format("There is no Internet connection."), Toast.LENGTH_LONG).show();
            return;
        }
        Intent readingsActivityIntent = new Intent(getApplicationContext(), ReadingsActivity.class);
        startActivity(readingsActivityIntent);
        finish();
    };

    private View.OnClickListener likeListener = v -> {
        //Verify Internet connection
        if (!existInternetConnection()) {
            Toast.makeText(ExploreActivity.this, String.format("There is no Internet connection."), Toast.LENGTH_LONG).show();
            return;
        }

        //Add reading to the user
        addReading();
    };

    private boolean existInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected())
            return true;

        return false;
    }

    private void addReading() {
        readings = user.getReadings();
        readings++;
        user.setReadings(readings);
        usersDB.child(user.getId()).setValue(user);
    }

    private void getUser() {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    user = postSnapshot.getValue(User.class);
                    user.setId(postSnapshot.getKey());
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

    private void getBooks() {
        booksDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    book = postSnapshot.getValue(Book.class);
                    chargeBooks();
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DB Error.", "loadPost:onCancelled", databaseError.toException());
            }
        });
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
