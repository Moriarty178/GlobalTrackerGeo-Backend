package com.example.GlobalTrackerGeo.Dto;

public class TripRequest {
    private Long customerId;
    private int offset;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
