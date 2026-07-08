package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.Dish;
import com.java1234.entity.Order;
import com.java1234.entity.Table;

import java.util.List;
import java.util.Map;


public interface IDishService extends IService<Dish> {

    /**
     * 根据条件 分页查询
     * @param map
     * @return
     */
    public List<Dish> list(Map<String,Object> map);

    /**
     * 根据条件，查询总记录数
     * @param map
     * @return
     */
    public Long getTotal(Map<String,Object> map);

}
