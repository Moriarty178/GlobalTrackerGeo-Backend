package com.example.GlobalTrackerGeo.Dto;

public class DriverPaymentReport {
    private long driverId;
    private String driverName;
    private String driverAccountNo;
    private String driverBankName;
    private Double totalRevenue;
    private Double commission;
    private Double driverPaymentAmount;
    private String paymentMethod;

    public DriverPaymentReport(long driverId, String driverName, String driverAccountNo, String driverBankName, Double totalRevenue, Double commission, Double driverPaymentAmount, String paymentMethod) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverAccountNo = driverAccountNo;
        this.driverBankName = driverBankName;
        this.totalRevenue = totalRevenue;
        this.commission = commission;
        this.driverPaymentAmount = driverPaymentAmount;
        this.paymentMethod = paymentMethod;
    }

    public long getDriverId() {
        return driverId;
    }

    public void setDriverId(long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverAccountNo() {
        return driverAccountNo;
    }

    public void setDriverAccountNo(String driverAccountNo) {
        this.driverAccountNo = driverAccountNo;
    }

    public String getDriverBankName() {
        return driverBankName;
    }

    public void setDriverBankName(String driverBankName) {
        this.driverBankName = driverBankName;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
