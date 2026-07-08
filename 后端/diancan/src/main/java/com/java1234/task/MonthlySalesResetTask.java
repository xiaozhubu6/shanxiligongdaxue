package com.java1234.task;

import com.java1234.entity.Dish;
import com.java1234.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 月销量重置定时任务
 */
@Component
public class MonthlySalesResetTask {

    @Autowired
    private IDishService dishService;

    /**
     * 每月1号凌晨0点执行
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void resetMonthlySales() {
        try {
            // 获取所有菜品
            List<Dish> dishes = dishService.list();
            // 遍历所有菜品，将月销量设置为0
            for (Dish dish : dishes) {
                dish.setMonthlysale(0);
                dishService.updateById(dish);
            }
            System.out.println("月销量已重置");
        } catch (Exception e) {
            System.err.println("重置月销量失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 