package com.java1234.controller.admin;

import com.java1234.entity.Dish;
import com.java1234.entity.PageBean;
import com.java1234.entity.R;
import com.java1234.entity.Table;
import com.java1234.service.IDishService;
import com.java1234.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin/dish")
public class AdminDishController {

    @Autowired
    private IDishService dishService;

    @Value("${dishImgsFilePath}")
    private String dishImgsFilePath;

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
        List<Dish> list=dishService.list(map);
        Long total =dishService.getTotal(map);
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("dishList",list);
        resultMap.put("total",total);
        return R.ok(resultMap);
    }

    /**
     * 添加或者更新
     * @param dish
     * @return
     */
    @PostMapping("/save")
    public R save(@RequestBody Dish dish){
        dish.setTime(new Date());
        if(dish.getId()!=null){
            dishService.updateById(dish);
        }else{
            dish.setOnsale(true);
            dishService.save(dish);
        }
        return R.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete")
    public R delete(Integer id){
        dishService.removeById(id);
        return R.ok();
    }

    /**
     * 更新上架 下架 状态
     * @return
     */
    @PostMapping("/updateOnSale")
    public R updateOnSale(@RequestBody Dish dish){
        Dish d = dishService.getById(dish.getId());
        d.setOnsale(dish.isOnsale());
        dishService.updateById(d);
        return R.ok();
    }

    /**
     * 上传菜品图片
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping("/uploadImage")
    public Map<String,Object> uploadImage(MultipartFile file)throws Exception{
        Map<String,Object> map=new HashMap<String,Object>();
        if(!file.isEmpty()){
            // 获取文件名
            String fileName = file.getOriginalFilename();
            // 获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            String newFileName= DateUtil.getCurrentDateStr()+suffixName;

            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(dishImgsFilePath+newFileName));
            map.put("code", 0);
            map.put("msg", "上传成功");
            Map<String,Object> map2=new HashMap<String,Object>();
            map2.put("imageName", newFileName);
            map2.put("src", "/image/dish/"+newFileName);
            map.put("data", map2);
        }
        return map;
    }

}
