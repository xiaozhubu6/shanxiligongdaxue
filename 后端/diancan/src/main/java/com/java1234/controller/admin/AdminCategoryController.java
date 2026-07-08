package com.java1234.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java1234.entity.*;
import com.java1234.service.ICategoryService;
import com.java1234.service.IDishService;
import com.java1234.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IDishService dishService;

    /**
     * 查询所有菜品类别
     * @return
     */
    @RequestMapping("/listAll")
    public R listAll(){
        List<Category> list = categoryService.list();
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("categoryList",list);
        return R.ok(resultMap);
    }

    /**
     * 分页显示
     * @param pageBean
     * @return
     */
    @RequestMapping("/list")
    public R list(@RequestBody PageBean pageBean){
        System.out.println(pageBean);
        Map<String,Object> map=new HashMap<>();
        map.put("start",pageBean.getStart());
        map.put("pageSize",pageBean.getPageSize());
        List<Table> list=categoryService.list(map);
        Long total =categoryService.getTotal(map);
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("categoryList",list);
        resultMap.put("total",total);
        return R.ok(resultMap);
    }

    /**
     * 添加
     * @param category
     * @return
     */
    @PostMapping("/add")
    public R add(@RequestBody Category category){
        category.setValue(category.getLabel());
        category.setCid("a"+DateUtil.getCurrentDateStr());
        category.setCount(0);
        category.setSele_quantity(0);
        categoryService.save(category);
        return R.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete")
    public R delete(Integer id){
        if(dishService.list(new QueryWrapper<Dish>().eq("typeId",id)).size()>0){
            return R.error("该菜品类目下有菜品，不能删除！");
        }else{
            categoryService.removeById(id);
            return R.ok();
        }
    }

}
