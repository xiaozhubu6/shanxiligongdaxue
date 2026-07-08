package com.java1234.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java1234.entity.*;
import com.java1234.service.IOrderDetailService;
import com.java1234.service.IOrderService;
import com.java1234.service.IDailyCostService;
import com.java1234.service.IDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;


@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private IDishService dishService;

    @Autowired
    private IDailyCostService dailyCostService;

    /**
     * 根据条件分页查询
     * @param pageBean
     * @return
     */
    @RequestMapping("/list")
    public R list(@RequestBody PageBean pageBean){
        System.out.println(pageBean);
        Map<String,Object> map=new HashMap<>();
        map.put("order_no",pageBean.getQuery().trim());
        map.put("start",pageBean.getStart());
        map.put("pageSize",pageBean.getPageSize());
        List<Order> list = orderService.list(map);
        Long total = orderService.getTotal(map);
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("orderList",list);
        resultMap.put("total",total);
        return R.ok(resultMap);
    }

    /**
     * 更新接单状态
     * @param order
     * @return
     */
    @PostMapping("/receiving")
    public R receiving(@RequestBody Order order){
        Order resultOrder = orderService.getById(order.getId());
        if(resultOrder == null){
            return R.error("订单不存在");
        }
        if(!"success".equals(resultOrder.getTransac_status())){
            return R.error("用户未支付，不能接单");
        }
        resultOrder.setOrder_receiving(order.getOrder_receiving());
        boolean updated = orderService.updateById(resultOrder);
        
        if (updated && "rec_order".equals(order.getOrder_receiving())) {
            try {
                // 获取订单日期和结算金额
                Date orderDate = resultOrder.getOrder_time();
                BigDecimal settledAmount = resultOrder.getSett_amount() != null ? 
                    new BigDecimal(resultOrder.getSett_amount().toString()) : BigDecimal.ZERO;

                // 查找或创建对应日期的日常成本记录
                DailyCost dailyCost = dailyCostService.findOrCreateByDate(orderDate);

                // 更新 dailyCost 的 totalRevenue 和 profit
                BigDecimal currentRevenue = dailyCost.getTotalRevenue() != null ? 
                    dailyCost.getTotalRevenue() : BigDecimal.ZERO;
                dailyCost.setTotalRevenue(currentRevenue.add(settledAmount));

                BigDecimal totalCost = dailyCost.getTotalCost() != null ? 
                    dailyCost.getTotalCost() : BigDecimal.ZERO;
                dailyCost.setProfit(dailyCost.getTotalRevenue().subtract(totalCost));

                // 更新日常成本记录
                dailyCostService.updateById(dailyCost);
            } catch (Exception e) {
                // 如果更新日常成本失败，记录错误但不影响接单
                System.err.println("更新日常成本失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return R.ok();
    }

    /**
     * 更新结账状态
     * @param order
     * @return
     */
    @PostMapping("/checkout")
    public R checkout(@RequestBody Order order){
        Order resultOrder = orderService.getById(order.getId());
        if(resultOrder==null){
            return R.error("订单不存在");
        }
        if(!"rec_order".equals(resultOrder.getOrder_receiving())){
            return R.error("订单未接单，不能结账");
        }
        if("success".equals(resultOrder.getTransac_status())){
            return R.error("订单已结账");
        }
        resultOrder.setTransac_status("success");
        orderService.updateById(resultOrder);

        // 更新菜品月销量
        List<OrderDetail> orderDetails = orderDetailService.list(
            new QueryWrapper<OrderDetail>().eq("mId", order.getId())
        );
        for(OrderDetail detail : orderDetails) {
            Dish dish = dishService.getById(detail.getId());
            if(dish != null) {
                // 更新月销量
                dish.setMonthlysale(dish.getMonthlysale() + Integer.parseInt(detail.getQuantity()));
                dishService.updateById(dish);
            }
        }

        // 更新日成本记录
        if("rec_order".equals(resultOrder.getOrder_receiving())){
            try {
                Date orderDate = resultOrder.getOrder_time();
                String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(orderDate);
                DailyCost dailyCost = dailyCostService.getOne(
                    new QueryWrapper<DailyCost>().eq("cost_date", dateStr)
                );
                if(dailyCost == null) {
                    dailyCost = new DailyCost();
                    dailyCost.setCostDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
                    dailyCost.setTotalCost(new BigDecimal("0"));
                    dailyCost.setTotalRevenue(new BigDecimal("0"));
                    dailyCost.setProfit(new BigDecimal("0"));
                    dailyCost.setCreateTime(new Date());
                    dailyCostService.save(dailyCost);
                }
                BigDecimal currentRevenue = dailyCost.getTotalRevenue();
                BigDecimal orderAmount = new BigDecimal(resultOrder.getSett_amount().toString());
                dailyCost.setTotalRevenue(currentRevenue.add(orderAmount));
                dailyCost.setProfit(dailyCost.getTotalRevenue().subtract(dailyCost.getTotalCost()));
                dailyCostService.updateById(dailyCost);
            } catch (ParseException e) {
                System.err.println("日期解析错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return R.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public R delete(@PathVariable(value = "id") Integer id){
        // 删除订单细表的数据
        orderDetailService.remove(new QueryWrapper<OrderDetail>().eq("mId",id));
        orderService.removeById(id);
        return R.ok();
    }

    /**
     *数据分析：七天销售额
     * @return
     */
    @GetMapping("/salesvolume")
    public R salesvolume(){
       List<SalesVolume> salesVolumeList = orderService.getSalesVolume();
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("salesVolumeList",salesVolumeList);
        return R.ok(resultMap);
    }

    /**
     * 更新派送状态
     * @param params 参数
     * @return
     */
    @PostMapping("/updateDeliveryStatus")
    public R updateDeliveryStatus(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("orderId");
        Boolean isDelivered = (Boolean) params.get("isDelivered");
        
        Order order = orderService.getById(orderId);
        if (order == null) {
            return R.error("订单不存在");
        }

        order.setIs_delivered(isDelivered);
        if (isDelivered) {
            order.setDelivery_time(new Date());
        }
        boolean updated = orderService.updateById(order);
        
        if (updated) {
            return R.ok("派送状态更新成功");
        } else {
            return R.error("派送状态更新失败");
        }
    }

    /**
     * 获取评价统计
     * @return
     */
    @GetMapping("/getRatingStats")
    public R getRatingStats() {
        List<Order> orders = orderService.list(new QueryWrapper<Order>().isNotNull("rating"));
        
        int totalRatings = orders.size();
        double averageRating = 0;
        int maxRating = 0;
        int minRating = 10;
        Map<Integer, Integer> distribution = new HashMap<>();
        
        for (Order order : orders) {
            int rating = order.getRating();
            averageRating += rating;
            maxRating = Math.max(maxRating, rating);
            minRating = Math.min(minRating, rating);
            distribution.put(rating, distribution.getOrDefault(rating, 0) + 1);
        }
        
        if (totalRatings > 0) {
            averageRating = averageRating / totalRatings;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRatings", totalRatings);
        result.put("averageRating", averageRating);
        result.put("maxRating", maxRating);
        result.put("minRating", minRating);
        result.put("distribution", distribution);
        result.put("ratingList", orders);
        
        return R.ok(result);
    }

}
