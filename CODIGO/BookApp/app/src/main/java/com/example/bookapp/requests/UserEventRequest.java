package com.example.bookapp.requests;

import com.google.gson.annotations.SerializedName;

public class UserEventRequest {
    //Variables
    @SerializedName("env")
    private String env;
    @SerializedName("type_events")
    private String typeEvents;
    @SerializedName("description")
    private String description;

    //Getters and setters
    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getTypeEvents() {
        return typeEvents;
    }

    public void setTypeEvents(String typeEvents) {
        this.typeEvents = typeEvents;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
