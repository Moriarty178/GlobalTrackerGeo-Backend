package com.example.GlobalTrackerGeo.Dto;

public class DriverLocationDTO {// driver web -> backend
    private Long driverId;
    private double latitude;
    private double longitude;
    private double speed;
    private long timestamp;
    //heading: hướng di chuyển, vd: 0 độ đại diện cho hướng Bắc

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
