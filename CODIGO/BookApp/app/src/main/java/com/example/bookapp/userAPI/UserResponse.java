package com.example.bookapp.userAPI;

public class UserResponse {
    //Variables
    private boolean success;
    private String env;
    private String token;
    private String tokenRefresh;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenRefresh() {
        return tokenRefresh;
    }

    public void setTokenRefresh(String tokenRefresh) {
        this.tokenRefresh = tokenRefresh;
    }
}
