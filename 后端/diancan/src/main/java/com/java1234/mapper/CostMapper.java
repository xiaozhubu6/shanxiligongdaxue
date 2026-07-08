package com.java1234.mapper;

import com.java1234.entity.Cost;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface CostMapper {
    
    @Select("SELECT * FROM t_daily_cost WHERE cost_date BETWEEN #{startDate} AND #{endDate} ORDER BY cost_date DESC")
    List<Cost> getCostList(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Insert("INSERT INTO t_daily_cost(cost_date, total_cost, total_revenue, profit, cost_detail, create_time, remark) " +
            "VALUES(#{costDate}, #{totalCost}, #{totalRevenue}, #{profit}, #{costDetail}, NOW(), #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addCost(Cost cost);
    
    @Update("UPDATE t_daily_cost SET cost_date=#{costDate}, total_cost=#{totalCost}, total_revenue=#{totalRevenue}, " +
            "profit=#{profit}, cost_detail=#{costDetail}, remark=#{remark} WHERE id=#{id}")
    int updateCost(Cost cost);
    
    @Delete("DELETE FROM t_daily_cost WHERE id=#{id}")
    int deleteCost(Integer id);
    
    @Select("SELECT * FROM t_daily_cost WHERE id=#{id}")
    Cost getCostById(Integer id);
    
    @Select("SELECT SUM(total_cost) as totalCost, SUM(total_revenue) as totalRevenue, SUM(profit) as totalProfit " +
            "FROM t_daily_cost WHERE cost_date BETWEEN #{startDate} AND #{endDate}")
    Cost getCostStatistics(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
} 