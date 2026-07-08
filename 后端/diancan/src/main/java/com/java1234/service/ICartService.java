package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.Admin;
import com.java1234.entity.Cart;


public interface ICartService extends IService<Cart> {

    public int deleteByTableNumber(String table_number);
    public int addCart(Cart cart);
}
