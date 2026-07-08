package com.java1234.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java1234.entity.Cart;
import com.java1234.entity.Category;
import com.java1234.entity.Dish;
import com.java1234.entity.R;
import com.java1234.service.ICartService;
import com.java1234.service.ICategoryService;
import com.java1234.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private IDishService dishService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private ICartService cartService;


    /**
     * 查询所有菜单分类
     * @return
     */
    @RequestMapping("/listAll/{table_num}")
    public R listAll(@PathVariable String table_num){
        long startTime = System.currentTimeMillis();
        try {
            List<Category> categoryList = categoryService.list();
            for(Category category : categoryList){
                List<Dish> dishList = dishService.list(new QueryWrapper<Dish>().eq("typeId", category.getId()).eq("onsale", 1));
                for (Cart cart : cartService.list(new QueryWrapper<Cart>().eq("table_num", table_num))) {
                    for (Dish dish : dishList) {
                        if (cart.getId().equals(dish.getId())){
                            dish.setQuantity(cart.getQuantity());
                        }
                    }
                }
                category.setDishList(dishList);
            }
            Map<String,Object> map=new HashMap<>();
            map.put("allDish",categoryList);
            long endTime = System.currentTimeMillis();
            System.out.println("DishController.listAll 执行时间: " + (endTime - startTime) + "ms");
            return R.ok(map);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            System.out.println("DishController.listAll 执行时间: " + (endTime - startTime) + "ms, 发生错误: " + e.getMessage());
            throw e;
        }
    }

}
