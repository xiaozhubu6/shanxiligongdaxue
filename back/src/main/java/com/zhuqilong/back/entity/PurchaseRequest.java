package com.zhuqilong.back.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseRequest {
    private Long id;
    private BigDecimal actualAmount;
    private String content;
    private LocalDateTime createdAt;
    private Long elderId;
    private BigDecimal estimatedAmount;
    private String remark;
    private String status;
    private LocalDateTime updatedAt;
}
