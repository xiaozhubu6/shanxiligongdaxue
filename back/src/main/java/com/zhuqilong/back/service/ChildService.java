package com.zhuqilong.back.service;

import com.zhuqilong.back.dto.ReviewRequest;
import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.entity.Child;
import com.zhuqilong.back.entity.Elder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ChildService {
    
    // 管理员需要的CRUD方法
    List<Child> getAllChildren();
    Child getChildById(Long id);
    int createChild(Child child);
    int updateChild(Child child);
    int deleteChild(Long id);
    
    /**
     * 子女登录
     */
    Map<String, Object> login(String phone, String name);
    
    /**
     * 充值
     */
    Map<String, Object> recharge(Long elderId, BigDecimal amount);
    
    /**
     * 获取账单
     */
    List<Bill> getBills(Long elderId);
    
    /**
     * 创建评价
     */
    Map<String, Object> createReview(ReviewRequest request);
    
    /**
     * 获取关联的老人
     */
    List<Elder> getAssociatedElders(Long childId);
    
    /**
     * 获取老人状态
     */
    Map<String, Object> getElderStatus(Long elderId);
}
