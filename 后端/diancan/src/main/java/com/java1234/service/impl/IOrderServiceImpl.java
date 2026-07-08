package com.java1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Order;
import com.java1234.entity.SalesVolume;
import com.java1234.mapper.OrderMapper;
import com.java1234.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;



@Service("orderService")
public class IOrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<Order> list(Map<String, Object> map) {
        return orderMapper.list(map);
    }

    @Override
    public Long getTotal(Map<String, Object> map) {
        return orderMapper.getTotal(map);
    }

    @Override
    public List<SalesVolume> getSalesVolume() {
        return orderMapper.getSalesVolume();
    }

    @Override
    public Map<String, Object> getWeeklyStatistics() {
        LocalDate today = LocalDate.now();

        // 获取本周的周一
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 获取本周的周日
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 将 LocalDate 转换为 java.util.Date
        Date startDate = java.sql.Date.valueOf(monday);
        Date endDate = java.sql.Date.valueOf(sunday);

        List<Map<String, Object>> weeklyData = orderMapper.getWeeklySalesStatistics(startDate, endDate);

        // 用于存储每个日期对应的销售额，方便查找
        Map<String, Double> salesMap = new HashMap<>();
        for (Map<String, Object> data : weeklyData) {
            String orderDateStr = (String) data.get("order_date");
            Double sales = Double.valueOf(data.get("total_sales").toString());
            salesMap.put(orderDateStr, sales);
        }

        List<String> xAxis = new ArrayList<>();
        List<Double> series = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM月dd日");

        // 生成从周一开始的七天日期和对应的销售额
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dayOfWeekName = getChineseDayOfWeek(date.getDayOfWeek()); // 获取中文星期几
            String formattedDate = date.format(dateFormatter); // 格式化为 MM月DD日

            xAxis.add(dayOfWeekName + " (" + formattedDate + ")");
            series.add(salesMap.getOrDefault(dateStr, 0.0)); // 如果没有数据，默认为0
        }

        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", xAxis);
        result.put("series", series);
        return result;
    }

    /**
     * 将 DayOfWeek 转换为中文星期几
     * @param dayOfWeek DayOfWeek枚举
     * @return 中文星期几字符串
     */
    private String getChineseDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "周一";
            case TUESDAY: return "周二";
            case WEDNESDAY: return "周三";
            case THURSDAY: return "周四";
            case FRIDAY: return "周五";
            case SATURDAY: return "周六";
            case SUNDAY: return "周日";
            default: return "";
        }
    }

    @Override
    public Map<String, Object> getMonthlyStatistics() {
        List<Map<String, Object>> monthlyData = orderMapper.getMonthlySalesStatistics();

        // 初始化12个月的数据
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"};
        List<String> xAxis = Arrays.asList(months);
        List<Double> series = Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // 填充实际数据
        for (Map<String, Object> data : monthlyData) {
            Object monthObj = data.get("month");
            Object salesObj = data.get("total_sales");

            if (monthObj != null && salesObj != null) {
                // 安全的类型转换
                Long month = Long.valueOf(monthObj.toString());
                Double sales = Double.valueOf(salesObj.toString());

                if (month >= 1 && month <= 12) {
                    series.set(month.intValue() - 1, sales);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", xAxis);
        result.put("series", series);
        return result;
    }

    @Override
    public Map<String, Object> getYearlyStatistics() {
        List<Map<String, Object>> yearlyData = orderMapper.getYearlySalesStatistics();

        List<String> xAxis = new ArrayList<>();
        List<Double> series = new ArrayList<>();

        // 填充年度数据
        for (Map<String, Object> data : yearlyData) {
            Object yearObj = data.get("year");
            Object salesObj = data.get("total_sales");

            if (yearObj != null && salesObj != null) {
                // 安全的类型转换
                Long year = Long.valueOf(yearObj.toString());
                Double sales = Double.valueOf(salesObj.toString());

                xAxis.add(year + "年");
                series.add(sales);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", xAxis);
        result.put("series", series);
        return result;
    }
}
