package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.Category;
import com.java1234.entity.Table;

import java.util.List;
import java.util.Map;



public interface ICategoryService extends IService<Category> {

    /**
     * 根据条件 分页查询
     * @param map
     * @return
     */
    public List<Table> list(Map<String,Object> map);

    /**
     * 根据条件，查询总记录数
     * @param map
     * @return
     */
    public Long getTotal(Map<String,Object> map);


}
