package com.java1234.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Cost {
    private Integer id;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date costDate;
    
    private BigDecimal totalCost;
    private BigDecimal totalRevenue;
    private BigDecimal profit;
    private String costDetail;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    private String remark;
} 