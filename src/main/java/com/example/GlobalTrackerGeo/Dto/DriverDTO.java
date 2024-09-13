package com.example.GlobalTrackerGeo.Dto;


public class DriverDTO {// Backend -> customer web
    private String name;
    private Location location;
    private double distance;

    public DriverDTO(String name, Location location, double distance) {
        this.name = name;
        this.location = location;
        this.distance = distance;
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
