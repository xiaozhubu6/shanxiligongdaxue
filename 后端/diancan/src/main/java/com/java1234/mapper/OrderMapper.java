package com.java1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java1234.entity.Order;
import com.java1234.entity.SalesVolume;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据条件 分页查询
     * @param map
     * @return
     */
    public List<Order> list(Map<String,Object> map);

    /**
     * 根据条件，查询订单总记录数
     * @param map
     * @return
     */
    public Long getTotal(Map<String,Object> map);

    public List<SalesVolume> getSalesVolume();

// 在 OrderMapper 接口中添加以下方法声明

    /**
     * 获取周统计数据
     * @return
     */
    List<Map<String, Object>> getWeeklyStatistics();

    /**
     * 获取月统计数据
     * @return
     */
    List<Map<String, Object>> getMonthlyStatistics();

    /**
     * 获取年统计数据
     * @return
     */
    List<Map<String, Object>> getYearlyStatistics();

    // 在 OrderMapper 接口中添加以下销售额统计方法声明
    /**
     * 获取周销售额统计数据
     * @return
     */
    List<Map<String, Object>> getWeeklySalesStatistics(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取月销售额统计数据
     * @return
     */
    List<Map<String, Object>> getMonthlySalesStatistics();

    /**
     * 获取年销售额统计数据
     * @return
     */
    List<Map<String, Object>> getYearlySalesStatistics();
}
