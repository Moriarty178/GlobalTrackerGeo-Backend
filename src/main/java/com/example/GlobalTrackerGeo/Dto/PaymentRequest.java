package com.example.GlobalTrackerGeo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequest {
    private double price;
    private double voucher;
    private double total;

    @JsonProperty("method")
    private String paymentMethod;
    @JsonProperty("status")
    private String paymentStatus;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVoucher() {
        return voucher;
    }

    public void setVoucher(double voucher) {
        this.voucher = voucher;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
