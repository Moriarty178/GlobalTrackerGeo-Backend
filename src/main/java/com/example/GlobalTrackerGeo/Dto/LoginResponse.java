package com.example.GlobalTrackerGeo.Dto;

public class LoginResponse {// Driver Web -> Backend
    private String jwt;
    private long id;

    public LoginResponse(String jwt, long id) {
        this.jwt = jwt;
        this.id = id;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public long getId() {
        return id;
    }

    public void setId(long driverId) {
        this.id = driverId;
    }
}
