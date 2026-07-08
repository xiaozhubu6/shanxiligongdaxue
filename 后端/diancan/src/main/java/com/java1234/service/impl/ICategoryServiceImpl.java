package com.java1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Category;
import com.java1234.entity.Table;
import com.java1234.mapper.CategoryMapper;
import com.java1234.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;



@Service("categoryService")
public class ICategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    @Override
    public List<Table> list(Map<String, Object> map) {
        return categoryMapper.list(map);
    }

    @Override
    public Long getTotal(Map<String, Object> map) {
        return categoryMapper.getTotal(map);
    }
}
