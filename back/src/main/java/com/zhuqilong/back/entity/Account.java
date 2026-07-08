package com.zhuqilong.back.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Account {
    private Long id;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private Long elderId;
    private LocalDateTime updatedAt;
    
    // 手动添加getter/setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
