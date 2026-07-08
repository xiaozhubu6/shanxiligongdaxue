package com.java1234.controller;

import com.java1234.entity.DailyCost;
import com.java1234.entity.R;
import com.java1234.service.IDailyCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dailyCost")
public class DailyCostController {

    @Autowired
    private IDailyCostService dailyCostService;

    /**
     * 分页查询成本记录
     */
    @GetMapping("/list")
    public R list(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                  @RequestParam(value = "pageSize", required = false) Integer pageSize,
                  @RequestParam(value = "startDate", required = false) String startDate,
                  @RequestParam(value = "endDate", required = false) String endDate) {
        if (startDate != null && startDate.trim().isEmpty()) startDate = null;
        if (endDate != null && endDate.trim().isEmpty()) endDate = null;
        Map<String, Object> map = new HashMap<>();
        int start = (pageNum != null && pageSize != null) ? (pageNum - 1) * pageSize : 0;
        int limit = pageSize != null ? pageSize : 10;
        
        map.put("start", start);
        map.put("pageSize", limit);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        System.out.println("map = " + map); // 调试用，打印参数
        
        List<DailyCost> list = dailyCostService.list(map);
        Long total = dailyCostService.getTotal(map);
        
        return R.ok().put("data", list).put("count", total);
    }

    /**
     * 添加成本记录
     */
    @PostMapping("/add")
    public R add(@RequestBody DailyCost dailyCost) {
        System.out.println("接收到的 dailyCost 对象: " + dailyCost); // 添加的打印语句
        int result = dailyCostService.add(dailyCost);
        if (result > 0) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 修改成本记录
     */
    @PostMapping("/update")
    public R update(@RequestBody DailyCost dailyCost) {
        int result = dailyCostService.update(dailyCost);
        if (result > 0) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 删除成本记录
     */
    @GetMapping("/delete")
    public R delete(@RequestParam("id") Integer id) {
        int result = dailyCostService.delete(id);
        if (result > 0) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 获取最近7天的成本统计
     */
    @GetMapping("/weeklyStatistics")
    public R getWeeklyStatistics() {
        List<Map<String, Object>> list = dailyCostService.getWeeklyCostStatistics();
        return R.ok().put("data", list);
    }

    /**
     * 获取最近30天的成本统计
     */
    @GetMapping("/monthlyStatistics")
    public R getMonthlyStatistics() {
        List<Map<String, Object>> list = dailyCostService.getMonthlyCostStatistics();
        return R.ok().put("data", list);
    }

    /**
     * 获取某天的成本、收入、收益
     */
    @GetMapping("/profit")
    public R getProfit(@RequestParam("date") String date) {
        return dailyCostService.getProfitByDate(date);
    }
} 