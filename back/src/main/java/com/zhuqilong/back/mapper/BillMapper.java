package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BillMapper {
    int insert(Bill bill);
    int updateById(Bill bill);
    int deleteById(Long id);
    Bill selectById(Long id);
    List<Bill> selectAll();
    List<Bill> selectByElderId(Long elderId);
}
