package com.java1234.controller;


import com.java1234.entity.Category;
import com.java1234.entity.R;
import com.java1234.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 查询所有菜单分类
     * @return
     */
    @RequestMapping("/listAll")
    public R listAll(){
        long startTime = System.currentTimeMillis();
        try {
            List<Category> list = categoryService.list();
            Map<String,Object> map=new HashMap<>();
            map.put("categoryListAll",list);
            long endTime = System.currentTimeMillis();
            System.out.println("CategoryController.listAll 执行时间: " + (endTime - startTime) + "ms");
            return R.ok(map);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            System.out.println("CategoryController.listAll 执行时间: " + (endTime - startTime) + "ms, 发生错误: " + e.getMessage());
            throw e;
        }
    }

}
