package com.java1234.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java1234.entity.Cart;
import com.java1234.entity.Order;
import com.java1234.entity.OrderDetail;
import com.java1234.entity.R;
import com.java1234.entity.Dish;
import com.java1234.service.ICartService;
import com.java1234.service.IDishService;
import com.java1234.service.IOrderDetailService;
import com.java1234.service.IOrderService;
import com.java1234.entity.DailyCost;
import com.java1234.dto.CreateOrderRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private ICartService cartService;
    @Autowired
    private IDishService dishService;

    @Autowired
    private com.java1234.service.IDailyCostService dailyCostService;

    /**
     * 创建订单
     * @param order
     * @return
     */
    @RequestMapping("/create")
    public R create(@RequestBody Order order){
        // System.out.println("order:"+order);
        order.setOrder_time(new Date());
        orderService.save(order);
        System.out.println(order.getId());
        for(OrderDetail od:order.getGoods_list()){
            od.setMId(order.getId());
            orderDetailService.save(od);
        }
        return R.ok();
    }

    /**
     * 根据条件获取订单
     * @param table_number
     * @param transac_status
     * @return
     */
    @RequestMapping("/get")
    public R get(String table_number,String transac_status){
        Order order=orderService.list(new QueryWrapper<Order>().eq("table_number", table_number).eq("transac_status", transac_status).orderByDesc("order_time")).get(0);
        System.out.println("11:"+order);
        List<OrderDetail> goods_list = orderDetailService.list(new QueryWrapper<OrderDetail>().eq("mId", order.getId()));
        Map<String,Object> map=new HashMap<>();
        map.put("goods_list",goods_list);
        map.put("menu",order);
        return R.ok(map);
    }
    @RequestMapping("addCart/{table_num}")
    public R addCart(@RequestBody List<Cart> carts, @PathVariable String table_num){
        cartService.deleteByTableNumber(table_num);
        for (Cart cart : carts) {
            cart.setTable_num(table_num);
            cartService.addCart(cart);
        }
        return R.ok();
    }
    @RequestMapping("getCart/{table_num}")
    public R getCart(@PathVariable String table_num){
        List<Cart> carts = cartService.list(new QueryWrapper<Cart>().eq("table_num", table_num));
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Cart cart : carts) {
            if (cart.getTotal_price() != null) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(cart.getTotal_price()));
            }
        }

        Map<String,Object> map=new HashMap<>();
        map.put("carts",carts);
        map.put("total_price", totalPrice);
        return R.ok(map);
    }

    /**
     * 获取销量统计数据
     * @param type 统计类型：weekly、monthly、yearly
     * @return
     */
    @RequestMapping("/statistics")
    public R getStatistics(@RequestParam String type) {
        Map<String, Object> result = new HashMap<>();

        switch (type) {
            case "weekly":
                result = orderService.getWeeklyStatistics();
                break;
            case "monthly":
                result = orderService.getMonthlyStatistics();
                break;
            case "yearly":
                result = orderService.getYearlyStatistics();
                break;
            default:
                return R.error("无效的统计类型");
        }

        return R.ok(result);
    }
    /**
     * 获取推荐菜品（按月销量排序）
     * @param limit 推荐数量，默认3个
     * @return
     */
    @RequestMapping("/recommend")
    public R getRecommendDishes(@RequestParam(defaultValue = "3") Integer limit) {
        // 从t_dish表中查询月销量最高的菜品
        List<Dish> topDishes = dishService.list(
            new QueryWrapper<Dish>()
                .orderByDesc("monthlysale")
                .last("LIMIT " + limit)
        );

        // 转换为前端需要的格式
        List<Map<String, Object>> recommendList = topDishes.stream()
            .map(dish -> {
                Map<String, Object> recommend = new HashMap<>();
                recommend.put("name", dish.getName());
                recommend.put("sales", dish.getMonthlysale());
                recommend.put("image", dish.getImage());
                recommend.put("unit", dish.getUnit());
                recommend.put("total_price", dish.getUnitprice());
                return recommend;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("recommendList", recommendList);
        result.put("total", recommendList.size());

        return R.ok(result);
    }

    /**
     * 订单结账并更新日常成本
     * @param id 订单ID
     * @return
     */
    @RequestMapping("/checkout")
    public R checkout(@RequestParam("id") Integer id) {
        // 1. 查询订单
        Order order = orderService.getById(id);
        if (order == null) {
            return R.error("订单不存在");
        }

        // 2. 更新订单状态为已结账
        order.setTransac_status("success");
        boolean updated = orderService.updateById(order);
        
        if (updated) {
            return R.ok("结账成功");
        } else {
            return R.error("结账失败");
        }
    }

    /**
     * 根据购物车数据创建已支付订单并清空购物车
     * @param table_num 桌号 (现在从DTO中获取)
     * @return
     */
    @RequestMapping("/createPaidOrderFromCart/{table_num}")
    public R createPaidOrderFromCart(@PathVariable String table_num, // 保留@PathVariable获取URL中的table_num
                                      @RequestBody CreateOrderRequestDTO requestDTO) { // 使用DTO接收请求体
        System.out.println("接收到的请求DTO内容: " + requestDTO); // 新增调试打印
        // 从DTO中获取用餐人数和桌号（如果请求体中包含）
        String numberOfDinersStr = requestDTO.getNumberOfDiners();
        String requestTableNum = requestDTO.getTable_number(); // 从请求体中获取的table_number
        List<Cart> carts = requestDTO.getGoods_list();
        Double settAmount = requestDTO.getSett_amount();
        String transactionId = requestDTO.getTransaction_id();

        // 如果URL中的table_num和请求体中的table_number不一致，优先使用URL中的
        String finalTableNum = (table_num != null && !table_num.isEmpty()) ? table_num : requestTableNum;

        // 1. 根据finalTableNum获取购物车数据 (这里的carts应该直接来自requestDTO.getGoods_list())
        // 为了保持原逻辑的完整性，如果requestDTO.getGoods_list()为空，则从数据库获取
        if (carts == null || carts.isEmpty()) {
            carts = cartService.list(new QueryWrapper<Cart>().eq("table_num", finalTableNum));
        }

        if (carts == null || carts.isEmpty()) {
            return R.error("购物车为空，无法创建订单");
        }

        // 2. 计算总价并构建订单详情 (这里可以直接使用requestDTO中的sett_amount)
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (settAmount != null) {
            totalAmount = BigDecimal.valueOf(settAmount);
        } else {
            // 如果DTO中sett_amount为空，则重新计算
            for (Cart cart : carts) {
                if (cart.getTotal_price() != null) {
                    totalAmount = totalAmount.add(BigDecimal.valueOf(cart.getTotal_price()));
                }
            }
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Cart cart : carts) {
            OrderDetail od = new OrderDetail();
            od.setName(cart.getName());
            od.setQuantity(String.valueOf(cart.getQuantity()));
            od.setUnit(cart.getUnit());
            od.setImage(cart.getImage());
            od.setTotal_price(cart.getTotal_price());
            orderDetails.add(od);
        }

        // 3. 创建新订单
        Order order = new Order();
        order.setOrder_no(System.currentTimeMillis() + "" + (int) ((Math.random() * 9 + 1) * 100000)); // 生成订单号
        order.setTable_number(finalTableNum);

        Integer diners = null;
        if (numberOfDinersStr != null && !numberOfDinersStr.trim().isEmpty()) {
            try {
                diners = Integer.parseInt(numberOfDinersStr);
            } catch (NumberFormatException e) {
                System.err.println("用餐人数类型转换失败: " + numberOfDinersStr + ", 错误信息: " + e.getMessage());
            }
        }
        order.setNumber_of_diners(diners); // 设置用餐人数，转换为Integer类型

        order.setSett_amount(totalAmount.doubleValue());
        order.setTransac_status("success"); // 设置为已支付状态
        order.setOrder_receiving("mis_orders"); // 设置为未接单状态
        order.setOrder_time(new Date());
        order.setPay_time(new Date()); // 设置支付时间
        order.setTransaction_id(transactionId); // 设置交易号
        order.setIs_delivered(false); // 设置初始派送状态为未派送
        order.setGoods_list(orderDetails); // 关联订单详情

        orderService.save(order); // 保存订单主表

        // 重新查询订单以获取完整的数据库状态，包括可能由数据库生成的字段或默认值
        Order savedOrder = orderService.getById(order.getId());

        // 保存订单详情
        for (OrderDetail od : orderDetails) {
            od.setMId(order.getId());
            orderDetailService.save(od);
            
            // 更新菜品月销量
            Dish dish = dishService.getOne(new QueryWrapper<Dish>().eq("name", od.getName()));
            if(dish != null) {
                dish.setMonthlysale(dish.getMonthlysale() + Integer.parseInt(od.getQuantity()));
                dishService.updateById(dish);
            }
        }

        // 4. 清空购物车
        cartService.remove(new QueryWrapper<Cart>().eq("table_num", finalTableNum));

        // 5. 返回完整的订单信息
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedOrder.getId());
        result.put("order_no", savedOrder.getOrder_no());
        result.put("table_number", savedOrder.getTable_number());
        result.put("sett_amount", savedOrder.getSett_amount());
        result.put("transac_status", savedOrder.getTransac_status());
        result.put("order_receiving", savedOrder.getOrder_receiving());
        result.put("order_time", savedOrder.getOrder_time());
        result.put("pay_time", savedOrder.getPay_time());
        result.put("is_delivered", savedOrder.getIs_delivered());
        result.put("delivery_time", savedOrder.getDelivery_time());
        result.put("rating", savedOrder.getRating());
        result.put("comment_time", savedOrder.getComment_time());
        result.put("transaction_id", savedOrder.getTransaction_id());
        result.put("goods_list", orderDetails);
        result.put("number_of_diners", savedOrder.getNumber_of_diners()); // 从重新查询的订单中获取用餐人数
        
        return R.ok(result);
    }

    /**
     * 根据购物车数据创建未支付订单，不清空购物车
     * @param table_num 桌号
     * @param requestDTO 请求参数
     * @return
     */
    @RequestMapping("/createUnpaidOrderFromCart/{table_num}")
    public R createUnpaidOrderFromCart(@PathVariable String table_num, 
                                        @RequestBody CreateOrderRequestDTO requestDTO) {
        System.out.println("接收到的请求DTO内容: " + requestDTO);
        // 从DTO中获取用餐人数和桌号
        String numberOfDinersStr = requestDTO.getNumberOfDiners();
        String requestTableNum = requestDTO.getTable_number();
        List<Cart> carts = requestDTO.getGoods_list();
        Double settAmount = requestDTO.getSett_amount();

        // 如果URL中的table_num和请求体中的table_number不一致，优先使用URL中的
        String finalTableNum = (table_num != null && !table_num.isEmpty()) ? table_num : requestTableNum;

        // 1. 获取购物车数据
        if (carts == null || carts.isEmpty()) {
            carts = cartService.list(new QueryWrapper<Cart>().eq("table_num", finalTableNum));
        }

        if (carts == null || carts.isEmpty()) {
            return R.error("购物车为空，无法创建订单");
        }

        // 2. 计算总价并构建订单详情
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (settAmount != null) {
            totalAmount = BigDecimal.valueOf(settAmount);
        } else {
            for (Cart cart : carts) {
                if (cart.getTotal_price() != null) {
                    totalAmount = totalAmount.add(BigDecimal.valueOf(cart.getTotal_price()));
                }
            }
        }

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Cart cart : carts) {
            OrderDetail od = new OrderDetail();
            od.setName(cart.getName());
            od.setQuantity(String.valueOf(cart.getQuantity()));
            od.setUnit(cart.getUnit());
            od.setImage(cart.getImage());
            od.setTotal_price(cart.getTotal_price());
            orderDetails.add(od);
        }

        // 3. 创建新订单
        Order order = new Order();
        order.setOrder_no(System.currentTimeMillis() + "" + (int) ((Math.random() * 9 + 1) * 100000)); // 生成订单号
        order.setTable_number(finalTableNum);

        Integer diners = null;
        if (numberOfDinersStr != null && !numberOfDinersStr.trim().isEmpty()) {
            try {
                diners = Integer.parseInt(numberOfDinersStr);
            } catch (NumberFormatException e) {
                System.err.println("用餐人数类型转换失败: " + numberOfDinersStr + ", 错误信息: " + e.getMessage());
            }
        }
        order.setNumber_of_diners(diners);

        order.setSett_amount(totalAmount.doubleValue());
        order.setTransac_status("unpaid"); // 设置为未支付状态
        order.setOrder_receiving("unrec_order"); // 设置为未接单状态
        order.setOrder_time(new Date());
        // 不设置支付时间
        order.setIs_delivered(false); // 设置初始派送状态为未派送
        order.setGoods_list(orderDetails); // 关联订单详情

        orderService.save(order); // 保存订单主表

        // 重新查询订单以获取完整的数据库状态
        Order savedOrder = orderService.getById(order.getId());

        // 保存订单详情
        for (OrderDetail od : orderDetails) {
            od.setMId(order.getId());
            orderDetailService.save(od);
        }

        // 4. 不清空购物车，因为用户可能还会继续加菜

        // 5. 返回完整的订单信息
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedOrder.getId());
        result.put("order_no", savedOrder.getOrder_no());
        result.put("table_number", savedOrder.getTable_number());
        result.put("sett_amount", savedOrder.getSett_amount());
        result.put("transac_status", savedOrder.getTransac_status());
        result.put("order_receiving", savedOrder.getOrder_receiving());
        result.put("order_time", savedOrder.getOrder_time());
        result.put("is_delivered", savedOrder.getIs_delivered());
        result.put("goods_list", orderDetails);
        result.put("number_of_diners", savedOrder.getNumber_of_diners());
        
        return R.ok(result);
    }

    /**
     * 提交订单评价
     * @param params 请求参数
     * @return
     */
    @RequestMapping("/submitRating")
    public R submitRating(@RequestBody Map<String, Object> params) {
        // 1. 获取参数
        Object orderIdObj = params.get("orderId");
        Object ratingObj = params.get("rating");
        String comment = (String) params.get("comment");
        
        // 2. 处理 orderId 类型转换
        Integer orderId = null;
        if (orderIdObj instanceof String) {
            try {
                orderId = Integer.parseInt((String) orderIdObj);
            } catch (NumberFormatException e) {
                return R.error("订单ID格式错误");
            }
        } else if (orderIdObj instanceof Integer) {
            orderId = (Integer) orderIdObj;
        }
        
        // 3. 处理 rating 类型转换
        Integer rating = null;
        if (ratingObj instanceof String) {
            try {
                rating = Integer.parseInt((String) ratingObj);
            } catch (NumberFormatException e) {
                return R.error("评分格式错误");
            }
        } else if (ratingObj instanceof Integer) {
            rating = (Integer) ratingObj;
        } else if (ratingObj instanceof Double) {
            rating = ((Double) ratingObj).intValue();
        }
        
        // 4. 验证参数
        if (orderId == null || rating == null) {
            return R.error("参数错误");
        }
        
        // 3. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return R.error("订单不存在");
        }

        // 4. 验证评分范围
        if (rating < 1 || rating > 10) {
            return R.error("评分必须在1-10分之间");
        }

        // 5. 更新订单评价信息
        order.setRating(rating);
        order.setComment(comment);
        order.setComment_time(new Date());
        boolean updated = orderService.updateById(order);
        
        if (updated) {
            return R.ok("评价提交成功");
        } else {
            return R.error("评价提交失败");
        }
    }

    /**
     * 获取订单评价
     * @param orderId 订单ID
     * @return
     */
    @RequestMapping("/getRating")
    public R getRating(@RequestParam("orderId") Integer orderId) {
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return R.error("订单不存在");
        }

        // 2. 返回评价信息
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("rating", order.getRating());
        result.put("comment_time", order.getComment_time());
        
        return R.ok(result);
    }

    /**
     * 更新订单派送状态
     * @param params 请求参数
     * @return
     */
    @RequestMapping("/updateDeliveryStatus")
    public R updateDeliveryStatus(@RequestBody Map<String, Object> params) {
        // 1. 获取参数
        Object orderIdObj = params.get("orderId");
        Boolean isDelivered = (Boolean) params.get("isDelivered");
        
        // 2. 处理 orderId 类型转换
        Integer orderId = null;
        if (orderIdObj instanceof String) {
            try {
                orderId = Integer.parseInt((String) orderIdObj);
            } catch (NumberFormatException e) {
                return R.error("订单ID格式错误");
            }
        } else if (orderIdObj instanceof Integer) {
            orderId = (Integer) orderIdObj;
        }
        
        // 3. 验证参数
        if (orderId == null || isDelivered == null) {
            return R.error("参数错误");
        }
        
        // 3. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return R.error("订单不存在");
        }

        // 4. 更新派送状态
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
     * 更新支付交易号
     * @param orderId 订单ID
     * @param transactionId 交易号
     * @return
     */
    @RequestMapping("/updateTransactionId")
    public R updateTransactionId(@RequestParam("orderId") Integer orderId, @RequestParam("transactionId") String transactionId) {
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return R.error("订单不存在");
        }

        // 2. 更新交易号
        order.setTransaction_id(transactionId);
        boolean updated = orderService.updateById(order);
        
        if (updated) {
            return R.ok("交易号更新成功");
        } else {
            return R.error("交易号更新失败");
        }
    }

    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return
     */
    @RequestMapping("/getOrderById/{orderId}")
    public R getOrderById(@PathVariable("orderId") Integer orderId) {
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return R.error("订单不存在");
        }

        // 2. 查询订单详情
        List<OrderDetail> goods_list = orderDetailService.list(new QueryWrapper<OrderDetail>().eq("mId", order.getId()));
        order.setGoods_list(goods_list);

        // 3. 返回订单信息
        return R.ok().put("data", order);
    }
}
