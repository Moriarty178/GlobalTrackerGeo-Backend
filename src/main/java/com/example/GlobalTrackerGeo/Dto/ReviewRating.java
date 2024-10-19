package com.example.GlobalTrackerGeo.Dto;

import java.time.LocalDateTime;

public class ReviewRating {
    private String tripId;
    private String ratingId;
    private String driverName;
    private String customerName;
    private String rating;
    private LocalDateTime createdAt;
    private String feedBack;

    public ReviewRating(String tripId, String ratingId, String driverName, String customerName, String rating, LocalDateTime createdAt, String feedBack) {
        this.tripId = tripId;
        this.ratingId = ratingId;
        this.driverName = driverName;
        this.customerName = customerName;
        this.rating = rating;
        this.createdAt = createdAt;
        this.feedBack = feedBack;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRatingId() {
        return ratingId;
    }

    public void setRatingId(String ratingId) {
        this.ratingId = ratingId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFeedBack() {
        return feedBack;
    }

    public void setFeedBack(String feedBack) {
        this.feedBack = feedBack;
    }
}
