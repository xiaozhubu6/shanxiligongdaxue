package com.java1234.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CostDTO {
    private Integer id;
    private Date costDate;
    private BigDecimal totalCost;
    private BigDecimal totalRevenue;
    private BigDecimal profit;
    private String costDetail;
    private String remark;
} 