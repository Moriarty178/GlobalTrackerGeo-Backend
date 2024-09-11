package com.example.GlobalTrackerGeo.Dto;

public class LoginResponse {
    private String jwt;
    private long driverId;

    public LoginResponse(String jwt, long driverId) {
        this.jwt = jwt;
        this.driverId = driverId;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long driverId) {
        this.driverId = driverId;
    }
}
