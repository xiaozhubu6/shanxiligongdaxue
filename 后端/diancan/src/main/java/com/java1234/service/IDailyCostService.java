package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.DailyCost;
import com.java1234.entity.R;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IDailyCostService extends IService<DailyCost> {
    
    /**
     * 根据条件分页查询
     */
    List<DailyCost> list(Map<String, Object> map);
    
    /**
     * 根据条件查询总记录数
     */
    Long getTotal(Map<String, Object> map);
    
    /**
     * 获取最近7天的成本统计
     */
    List<Map<String, Object>> getWeeklyCostStatistics();
    
    /**
     * 获取最近30天的成本统计
     */
    List<Map<String, Object>> getMonthlyCostStatistics();
    
    /**
     * 添加成本记录
     */
    int add(DailyCost dailyCost);
    
    /**
     * 修改成本记录
     */
    int update(DailyCost dailyCost);
    
    /**
     * 删除成本记录
     */
    int delete(Integer id);
    
    /**
     * 获取某天的成本、收入、收益
     */
    R getProfitByDate(String date);
    
    /**
     * 根据日期查找或创建日常成本记录
     * @param date 日期
     * @return 对应的日常成本记录
     */
    DailyCost findOrCreateByDate(Date date);
} 