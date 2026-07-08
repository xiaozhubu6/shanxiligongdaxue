package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.Table;

import java.util.List;
import java.util.Map;



public interface ITableService extends IService<Table> {

    /**
     * 根据条件 分页查询
     * @param map
     * @return
     */
    public List<Table> list(Map<String,Object> map);

    /**
     * 根据条件，查询订单总记录数
     * @param map
     * @return
     */
    public Long getTotal(Map<String,Object> map);


}
