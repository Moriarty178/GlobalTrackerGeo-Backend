package com.example.GlobalTrackerGeo.Dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationRequest {// customer -> backend searchDriver()
    @JsonProperty("loc_source")
    private Location locSource;

    @JsonProperty("loc_destination")
    private Location locDestination;

    // Getters và Setters
    public Location getLocSource() {
        return locSource;
    }

    public void setLocSource(Location locSource) {
        this.locSource = locSource;
    }

    public Location getLocDestination() {
        return locDestination;
    }

    public void setLocDestination(Location locDestination) {
        this.locDestination = locDestination;
    }

    @Override
    public String toString() {
        return "LocationRequest{" +
                "locSource=" + locSource +
                ", locDestination=" + locDestination +
                '}';
    }
}
