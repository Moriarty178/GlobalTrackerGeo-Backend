package com.example.GlobalTrackerGeo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {
    private double lat;
    private double lon;
    @JsonProperty("display_name")
    private String displayName;


    public Location(double lat, double lon, String displayName) {
        this.lat = lat;
        this.lon = lon;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
