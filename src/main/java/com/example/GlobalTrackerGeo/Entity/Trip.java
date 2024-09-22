package com.example.GlobalTrackerGeo.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.*;

import java.io.IOException;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @Column(name = "trip_id")
    private String tripId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "distance", columnDefinition = "NUMERIC(10, 2)",nullable = false)
    private Double distance;

    @Column(name = "route", columnDefinition = "TEXT")
    private String route;  // Lưu chuỗi JSON của các vị trí

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


//    public void addLocationToRoute(double lat, double lon) {// Khi thêm vào JSONObject của lib/org.json thứ tự các cặp key-value ko được đảm bảo
//        try {
//            // Chuyển chuỗi JSON thành JSONArray lib/org.json
//            JSONArray routeArray = (this.route == null || this.route.isEmpty()) ? new JSONArray() : new JSONArray(this.route);
//
//            // Tạo JSONObject lib/org.json mới chứa vị trí mới
//            JSONObject newLocation = new JSONObject();
//            newLocation.put("lat", lat);
//            newLocation.put("lon", lon);
//            System.out.println(newLocation);
//            // Thêm vị trí mới vào route
//            routeArray.put(newLocation);
//
//            // Chuyển JSONArray thành chuỗi (string) JSON và lưu vào trường "route" (TEXT) trong PostgreSQL
//            this.route = routeArray.toString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    // Sử dụng ObjectMapper để thêm vị trí mới vào route
    public void addLocationToRoute(double lat, double lon) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Chuyển chuỗi JSON thành ArrayNode (nếu route không null)
            ArrayNode routeArray;
            if (this.route == null || this.route.isEmpty()) {
                routeArray = objectMapper.createArrayNode();
            } else {
                routeArray = (ArrayNode) objectMapper.readTree(this.route);
            }

            // Tạo ObjectNode mới chứa vị trí mới
            ObjectNode newLocation = objectMapper.createObjectNode();
            newLocation.put("lat", lat);
            newLocation.put("lon", lon);

            // Thêm vị trí mới vào routeArray
            routeArray.add(newLocation);
//            System.out.println(routeArray);
            // Chuyển ArrayNode thành chuỗi JSON và lưu vào trường "route"
            this.route = objectMapper.writeValueAsString(routeArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
