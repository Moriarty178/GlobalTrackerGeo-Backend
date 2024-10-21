package com.example.GlobalTrackerGeo.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "promo_id")
    private String promoId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "code", length = 255, nullable = false)
    private String code;

    @Column(name = "type", length = 255, nullable = false)
    private String type;

    @Column(name = "value",columnDefinition = "NUMERIC(10, 2)", length = 255, nullable = false)
    private Double value;

    @Column(name = "status", length = 255, nullable = false)
    private String status;

    @Column(name = "usage_limit", length = 255, nullable = false)
    private Long usageLimit;

    @Column(name = "total_user_used", nullable = false)
    private Long totalUserUsed = 0L; // default = 0 nếu ko có giá trị

    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected  void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate // khi promoCodeRepository.save() -> tụ động cập nhât trường updateAt = now()
    protected  void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getPromoId() {
        return promoId;
    }

    public void setPromoId(String promoId) {
        this.promoId = promoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalUserUsed() {
        return totalUserUsed;
    }

    public void setTotalUserUsed(Long totalUserUsed) {
        this.totalUserUsed = totalUserUsed;
    }

    public LocalDateTime getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDateTime expiredDate) {
        this.expiredDate = expiredDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
