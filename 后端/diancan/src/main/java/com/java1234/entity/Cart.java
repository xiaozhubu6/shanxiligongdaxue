package com.java1234.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("cart")
@Data
public class Cart {
    private String table_num;
    private String cid;
    private Integer good_index;
    private Integer id;
    private String image;
    private String name;
    private Integer quantity;
    private String unit;
    private Double total_price;
    private Double unitprice;
}
