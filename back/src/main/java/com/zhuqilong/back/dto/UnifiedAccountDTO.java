package com.zhuqilong.back.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一账户信息DTO（包含admin、child、elder三种类型）
 */
@Data
public class UnifiedAccountDTO {
    private Long id;
    private String username;      // 用户名
    private String name;          // 姓名
    private String userType;      // 用户类型：admin/child/elder
    private String email;          // 邮箱（虚拟字段）
    private String phone;          // 手机号
    private String status;        // 状态：active/inactive
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt; // 最后登录时间（虚拟字段）
    
    // 老人特有字段
    private Integer age;
    private String gender;
    private Long gridGroupId;
    private BigDecimal balance;   // 账户余额
    private String unitNumber;    // 单元号
    
    // 子女特有字段
    private String associatedElders; // 关联的老人（简化为字符串）
    
    // 管理员特有字段
    private String role;          // 角色
    
    // 手动添加getter/setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Long getGridGroupId() { return gridGroupId; }
    public void setGridGroupId(Long gridGroupId) { this.gridGroupId = gridGroupId; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
    
    public String getAssociatedElders() { return associatedElders; }
    public void setAssociatedElders(String associatedElders) { this.associatedElders = associatedElders; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
