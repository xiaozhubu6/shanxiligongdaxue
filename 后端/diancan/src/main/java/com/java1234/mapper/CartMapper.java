package com.java1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java1234.entity.Cart;

public interface CartMapper extends BaseMapper<Cart> {
    public int deleteByTableNumber(String table_number);
    public int addCart(Cart cart);
}
