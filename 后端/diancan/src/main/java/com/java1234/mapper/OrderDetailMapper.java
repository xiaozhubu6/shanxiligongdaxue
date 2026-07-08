package com.java1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.java1234.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;



@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    /**
     * 查询销量最高的菜品
     * @param limit 返回记录数量
     * @return 热门菜品列表
     */


}
