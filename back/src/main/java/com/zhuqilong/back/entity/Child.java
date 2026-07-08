package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Child {
    private Long id;
    private LocalDateTime createdAt;
    private String name;
    private String phone;
    
    // 手动添加getter/setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
