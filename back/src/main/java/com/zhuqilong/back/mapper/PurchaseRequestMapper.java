package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.PurchaseRequest;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PurchaseRequestMapper {
    int insert(PurchaseRequest purchaseRequest);
    int updateById(PurchaseRequest purchaseRequest);
    int deleteById(Long id);
    PurchaseRequest selectById(Long id);
    List<PurchaseRequest> selectAll();
    List<PurchaseRequest> selectByElderId(Long elderId);
    List<PurchaseRequest> selectByStatus(String status);
}
