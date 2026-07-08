package com.java1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_daily_cost")
public class DailyCost {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("cost_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date costDate;
    
    @TableField("total_cost")
    private BigDecimal totalCost;
    
    @TableField("total_revenue")
    private BigDecimal totalRevenue;
    
    @TableField("profit")
    private BigDecimal profit;
    
    @TableField("cost_detail")
    private String costDetail;
    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    @TableField("remark")
    private String remark;
} 