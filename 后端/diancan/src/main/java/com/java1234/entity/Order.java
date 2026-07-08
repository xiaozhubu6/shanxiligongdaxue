package com.java1234.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单主表
 * @author java1234_小锋

 */
@TableName("t_order")
@Data
public class Order {

    private Integer id; // 编号

    private String order_no; // 订单号

    private String table_number; // 桌号

    private Integer number_of_diners; // 用餐人数

    private Double sett_amount; // 计算总价

    private String transac_status; // 结账状态

    private String order_receiving;  // 接单状态

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date order_time; // 下单时间

    private Integer rating; // 就餐评分 (1-10 分)

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date comment_time; // 评价时间

    private Boolean is_delivered; // 是否已派送 (0/1)

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date delivery_time; // 派送时间

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date pay_time; // 支付时间

    private String transaction_id; // 支付交易号

    private String comment; // 文字评价

    @TableField(select = false,exist = false)
    private List<OrderDetail> goods_list; // 订单详情

}
