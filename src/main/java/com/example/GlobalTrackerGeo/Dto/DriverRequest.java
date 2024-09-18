package com.example.GlobalTrackerGeo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DriverRequest {//Customer Web -> Backend,customer chọn 1 tài xê cụ thể
    private Long driverId;

    private Long customerId;

    @JsonProperty("loc_source")
    private Location loc_source;

    @JsonProperty("loc_destination")
    private Location loc_destination;

    private double distance;// D(loc_source, loc_destination)

    @JsonProperty("payment_request")
    private PaymentRequest paymentRequest;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Location getLoc_source() {
        return loc_source;
    }

    public void setLoc_source(Location loc_source) {
        this.loc_source = loc_source;
    }

    public Location getLoc_destination() {
        return loc_destination;
    }

    public void setLoc_destination(Location loc_destination) {
        this.loc_destination = loc_destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
    }
}
