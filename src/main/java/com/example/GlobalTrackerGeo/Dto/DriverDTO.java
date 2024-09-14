package com.example.GlobalTrackerGeo.Dto;


public class DriverDTO {// Backend -> customer web
    private long driverId;
    private String name;
    private Location location;
    private double distance;

    public DriverDTO(long driverId, String name, Location location, double distance) {
        this.driverId = driverId;
        this.name = name;
        this.location = location;
        this.distance = distance;
    }

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long driverId) {
        this.driverId = driverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
