package com.example.bookapp.responses;

import com.example.bookapp.models.userEventsAPI.Event;

public class UserEventResponse {
    //Variables
    private boolean success;
    private String env;
    private Event event;

    //Functions
    public boolean isSuccess() {
        return success;
    }

    //Getters and setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
