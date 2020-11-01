package com.example.bookapp.userAPI;

public class EventResponse {
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
