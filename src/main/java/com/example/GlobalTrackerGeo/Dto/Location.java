package com.example.GlobalTrackerGeo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.IOException;

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

    public Location() {}

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

    // Sử dụng ObjectMapper để chuyển đổi đối tượng Location thành chuỗi JSON
    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this); // Chuyển đổi Location thành JSON
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Chuyển chuỗi JSON thành đối tượng Location
    public static Location convertJsonToLocation(String jsonString) throws IOException {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            Location location = new Location();
            if (jsonNode.has("lat")) {
                location.setLat(jsonNode.get("lat").asDouble());
            }
            if (jsonNode.has("lon")) {
                location.setLon(jsonNode.get("lon").asDouble());
            }
            if (jsonNode.has("display_name")) {
                location.setDisplayName(jsonNode.get("display_name").asText());
            }

            return location;
        } catch (IOException e) {
            System.out.println("Error in convertJsonToLocation: " + e.getMessage());
            throw e;
        }
    }
}
