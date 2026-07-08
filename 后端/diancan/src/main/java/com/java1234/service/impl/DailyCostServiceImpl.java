package com.java1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.DailyCost;
import com.java1234.entity.R;
import com.java1234.mapper.DailyCostMapper;
import com.java1234.service.IDailyCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyCostServiceImpl extends ServiceImpl<DailyCostMapper, DailyCost> implements IDailyCostService {

    @Autowired
    private DailyCostMapper dailyCostMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<DailyCost> list(Map<String, Object> map) {
        return dailyCostMapper.list(map);
    }

    @Override
    public Long getTotal(Map<String, Object> map) {
        return dailyCostMapper.getTotal(map);
    }

    @Override
    public List<Map<String, Object>> getWeeklyCostStatistics() {
        return dailyCostMapper.getWeeklyCostStatistics();
    }

    @Override
    public List<Map<String, Object>> getMonthlyCostStatistics() {
        return dailyCostMapper.getMonthlyCostStatistics();
    }

    @Override
    public int add(DailyCost dailyCost) {
        if (dailyCost.getTotalRevenue() == null) {
            dailyCost.setTotalRevenue(BigDecimal.ZERO);
        }
        if (dailyCost.getProfit() == null) {
            dailyCost.setProfit(BigDecimal.ZERO);
        }
        dailyCost.setCreateTime(new Date());
        return dailyCostMapper.insert(dailyCost);
    }

    @Override
    public int update(DailyCost dailyCost) {
        System.out.println("DailyCostServiceImpl.update 方法被调用，接收到 dailyCost: " + dailyCost);

        // 1. 根据ID查询数据库中的现有记录
        DailyCost existingDailyCost = dailyCostMapper.selectById(dailyCost.getId());

        if (existingDailyCost == null) {
            // 记录不存在，无法更新
            System.err.println("DailyCostServiceImpl.update: 未找到ID为 " + dailyCost.getId() + " 的日常成本记录。");
            return 0; // 或者抛出异常
        }

        // 2. 更新现有记录的字段（只更新前端可能传递的字段）
        if (dailyCost.getCostDate() != null) {
             // 如果前端传递了日期，更新日期 (注意日期处理的一致性，这里假设传递的是java.util.Date)
            existingDailyCost.setCostDate(dailyCost.getCostDate());
        }
        if (dailyCost.getCostDetail() != null) {
            existingDailyCost.setCostDetail(dailyCost.getCostDetail());
            // 3. 根据新的 costDetail 重新计算 totalCost
            try {
                // 假设 costDetail 是一个 JSON 字符串，格式如 {"食材": 200, "人工": 200, "水电": 12}
                Map<String, Object> detailMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(dailyCost.getCostDetail(), Map.class);
                BigDecimal totalCost = BigDecimal.ZERO;
                for (Object value : detailMap.values()) {
                    if (value instanceof Number) {
                        totalCost = totalCost.add(new BigDecimal(value.toString()));
                    }
                }
                existingDailyCost.setTotalCost(totalCost);
            } catch (Exception e) {
                System.err.println("DailyCostServiceImpl.update: 解析 costDetail 或计算 totalCost 失败: " + e.getMessage());
                e.printStackTrace();
                // 如果解析失败，可以考虑不更新 totalCost，或者设置为0，取决于业务需求
                // existingDailyCost.setTotalCost(BigDecimal.ZERO);
            }
        }
        if (dailyCost.getRemark() != null) {
            existingDailyCost.setRemark(dailyCost.getRemark());
        }
         // 注意：totalRevenue 在订单结账时更新，这里不应直接使用前端传过来的值 unless explicitly required


        // 4. 重新计算 profit
        BigDecimal currentTotalCost = existingDailyCost.getTotalCost() != null ? existingDailyCost.getTotalCost() : BigDecimal.ZERO;
        BigDecimal currentTotalRevenue = existingDailyCost.getTotalRevenue() != null ? existingDailyCost.getTotalRevenue() : BigDecimal.ZERO;
        existingDailyCost.setProfit(currentTotalRevenue.subtract(currentTotalCost));

        // 5. 更新数据库记录
        return dailyCostMapper.updateById(existingDailyCost);
    }

    @Override
    public int delete(Integer id) {
        return dailyCostMapper.deleteById(id);
    }

    @Override
    public R getProfitByDate(String date) {
        // 查询成本
        BigDecimal cost = jdbcTemplate.queryForObject(
            "SELECT IFNULL(total_cost,0) FROM t_daily_cost WHERE cost_date = ?",
            new Object[]{date}, BigDecimal.class);
        // 查询收入
        BigDecimal income = jdbcTemplate.queryForObject(
            "SELECT IFNULL(SUM(sett_amount),0) FROM t_order WHERE DATE(order_time) = ? AND transac_status = 'success' AND order_receiving = 'rec_order'",
            new Object[]{date}, BigDecimal.class);
        if (cost == null) cost = BigDecimal.ZERO;
        if (income == null) income = BigDecimal.ZERO;
        BigDecimal profit = income.subtract(cost);
        Map<String, Object> result = new HashMap<>();
        result.put("cost", cost);
        result.put("income", income);
        result.put("profit", profit);
        return R.ok().put("data", result);
    }

    @Override
    public DailyCost findOrCreateByDate(Date date) {
        // 将Date对象转换为只有年月日部分的 java.sql.Date
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

        // 根据日期查询DailyCost记录
        QueryWrapper<DailyCost> queryWrapper = new QueryWrapper<>();
        // 使用转换后的 sqlDate 进行查询，忽略时间部分
        queryWrapper.eq("cost_date", sqlDate);

        // 改用 selectList 查询，处理多条记录的情况
        List<DailyCost> dailyCostList = dailyCostMapper.selectList(queryWrapper);

        DailyCost dailyCost = null;

        if (dailyCostList != null && !dailyCostList.isEmpty()) {
            // 如果找到了记录，取第一条
            dailyCost = dailyCostList.get(0);
            // 如果有多条记录，可以考虑日志警告或其他处理
            if (dailyCostList.size() > 1) {
                 System.err.println("警告：发现多个日期为 " + sqlDate + " 的日常成本记录，使用了第一条。");
            }
        }

        // 如果不存在，创建新的记录
        if (dailyCost == null) {
            dailyCost = new DailyCost();
            // 插入时也使用只有年月日部分的 sqlDate
            dailyCost.setCostDate(sqlDate);
            dailyCost.setTotalCost(BigDecimal.ZERO); // 默认总成本为0
            dailyCost.setTotalRevenue(BigDecimal.ZERO); // 默认总营业额为0
            dailyCost.setProfit(BigDecimal.ZERO); // 默认利润为0
            dailyCost.setCreateTime(new Date()); // 设置创建时间 (create_time 可以包含时间)
            dailyCostMapper.insert(dailyCost);
            // 插入后重新查询一次，确保获取到完整的实体（包含自增ID等）
            // 仍然使用忽略时间部分的 sqlDate 进行查询
            dailyCost = dailyCostMapper.selectOne(queryWrapper); // 这里理论上只会查到刚刚插入的一条
        }
        return dailyCost;
    }
} 