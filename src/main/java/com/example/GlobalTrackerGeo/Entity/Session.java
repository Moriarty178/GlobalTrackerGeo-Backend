package com.example.GlobalTrackerGeo.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "diver_id", nullable = false)
    private Driver driver;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
