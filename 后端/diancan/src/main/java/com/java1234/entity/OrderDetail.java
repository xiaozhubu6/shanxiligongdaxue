package com.java1234.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;



@TableName("t_order_detail")
@Data
public class OrderDetail {

    private Integer id; // 编号

    private Integer mId; // 订单主表Id

    private String name; // 菜品名称

    private String quantity; // 数量

    private String unit; // 单位

    private String image; // 菜品图片

    private Double total_price; // 总价

}
