package com.example.GlobalTrackerGeo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TripRequest {
    @JsonProperty("id")
    private Long id; // customerId or driverId
    @JsonProperty("offset")
    private int offset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
