package com.java1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Cart;
import com.java1234.mapper.CartMapper;
import com.java1234.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service("cartService")
public class ICartServiceImpl extends ServiceImpl<CartMapper,Cart> implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Override
    public int deleteByTableNumber(String table_number) {
        return cartMapper.deleteByTableNumber(table_number);
    }

    @Override
    public int addCart(Cart cart) {
        return cartMapper.addCart(cart);
    }
}
