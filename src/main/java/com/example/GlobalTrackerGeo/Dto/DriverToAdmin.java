package com.example.GlobalTrackerGeo.Dto;

import java.time.LocalDateTime;

public class DriverToAdmin {
    private long driverId;
    private String name;
    private String email;
    private String phone;
    private String online;
    private String status;
    private LocalDateTime createdAt;

    public DriverToAdmin(long driverId, String name, String email, String phone, String online, String status, LocalDateTime createdAt) {
        this.driverId = driverId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.online = online;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
