package com.example.bookapp.models.firebase;

public class User {
    //Variables
    private String id;
    private String email;
    private int readings;

    //Constructor
    public User() {
        readings = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getReadings() {
        return readings;
    }

    public void setReadings(int readings) {
        this.readings = readings;
    }
}
