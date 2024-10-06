package com.example.GlobalTrackerGeo.Dto;

import java.time.LocalDateTime;

public class TripToAdmin {
    private String tripId;
    private Long customerId;
    private String customerName; // Thêm thuộc tính tên khách hàng
    private Long driverId;
    private String driverName; // Thêm thuộc tính tên tài xế
    private String status;
    private String source;
    private String destination;
    private Double distance;
    private String route;
    private LocalDateTime createdAt;

    public TripToAdmin(String tripId, Long customerId, String customerName, Long driverId, String driverName, String status, String source, String destination, Double distance, String route, LocalDateTime createdAt) {
        this.tripId = tripId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.driverId = driverId;
        this.driverName = driverName;
        this.status = status;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.route = route;
        this.createdAt = createdAt;
    }

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
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
}
