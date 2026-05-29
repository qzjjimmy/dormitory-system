package com.bishe.dormitory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BizRecord {
    private Long id;
    private String category;
    private String title;
    private String owner;
    private String location;
    private BigDecimal amount;
    private String status;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BizRecord() {}

    public BizRecord(String category, String title, String owner, String location,
                     BigDecimal amount, String status, String content) {
        this.category = category;
        this.title = title;
        this.owner = owner;
        this.location = location;
        this.amount = amount;
        this.status = status;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
