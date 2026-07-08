package com.java1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Dish;
import com.java1234.mapper.DishMapper;
import com.java1234.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("dishService")
public class IDishServiceImpl extends ServiceImpl<DishMapper,Dish> implements IDishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Dish> list(Map<String, Object> map) {
        return dishMapper.list(map);
    }

    @Override
    public Long getTotal(Map<String, Object> map) {
        return dishMapper.getTotal(map);
    }
}
