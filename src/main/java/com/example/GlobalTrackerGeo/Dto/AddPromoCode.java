package com.example.GlobalTrackerGeo.Dto;

import java.time.LocalDateTime;

public class AddPromoCode {
    private String name;
    private String codeName;
    private String type;
    private Double value;
    private Long usageLimit;
    private LocalDateTime expiredDate;
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Long usageLimit) {
        this.usageLimit = usageLimit;
    }

    public LocalDateTime getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDateTime expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
