package com.java1234.dto;

import com.java1234.entity.Cart;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDTO {
    private String table_number;
    private String numberOfDiners;
    private List<Cart> goods_list;
    private Double sett_amount;
    private String transaction_id;
} 