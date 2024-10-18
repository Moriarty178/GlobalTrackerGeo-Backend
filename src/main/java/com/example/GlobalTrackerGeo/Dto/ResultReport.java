package com.example.GlobalTrackerGeo.Dto;

import java.time.LocalDateTime;

public class ResultReport {
    private String tripId;
    private String driverName;
    private String customerName;
    private LocalDateTime createdAt;
    private Double totalRevenue;
    private Double commission;
    private Double driverPaymentAmount;
    private Double adminEarningAmount;
    private String paymentMethod;

    public ResultReport(String tripId, String driverName, String customerName, LocalDateTime createdAt, Double totalRevenue, Double commission, Double driverPaymentAmount, Double adminEarningAmount, String paymentMethod) {
        this.tripId = tripId;
        this.driverName = driverName;
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.totalRevenue = totalRevenue;
        this.commission = commission;
        this.driverPaymentAmount = driverPaymentAmount;
        this.adminEarningAmount = adminEarningAmount;
        this.paymentMethod = paymentMethod;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public Double getDriverPaymentAmount() {
        return driverPaymentAmount;
    }

    public void setDriverPaymentAmount(Double driverPaymentAmount) {
        this.driverPaymentAmount = driverPaymentAmount;
    }

    public Double getAdminEarningAmount() {
        return adminEarningAmount;
    }

    public void setAdminEarningAmount(Double adminEarningAmount) {
        this.adminEarningAmount = adminEarningAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
