package com.zhuqilong.back.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bill {
    private Long id;
    private BigDecimal amount;
    private String billType;
    private LocalDateTime createdAt;
    private String description;
    private Long elderId;
    private String elderName;
    private String operatorName;  // 操作人（子女/管理员）
    private String paymentMethod; // 支付方式
    private String remark;        // 备注
}
