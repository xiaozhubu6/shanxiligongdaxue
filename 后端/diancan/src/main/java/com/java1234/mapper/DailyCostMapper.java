package com.java1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java1234.entity.DailyCost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DailyCostMapper extends BaseMapper<DailyCost> {
    
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
} 