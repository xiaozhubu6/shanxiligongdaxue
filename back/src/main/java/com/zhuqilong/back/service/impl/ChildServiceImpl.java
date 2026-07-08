package com.zhuqilong.back.service.impl;

import com.zhuqilong.back.dto.ReviewRequest;
import com.zhuqilong.back.entity.*;
import com.zhuqilong.back.mapper.*;
import com.zhuqilong.back.service.ChildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChildServiceImpl implements ChildService {

    @Autowired
    private ChildMapper childMapper;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private ElderChildMapper elderChildMapper;
    
    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private BillMapper billMapper;
    
    @Autowired
    private ServiceReviewMapper serviceReviewMapper;
    
    @Autowired
    private FaceCheckRecordMapper faceCheckRecordMapper;

    // 管理员需要的CRUD方法实现
    @Override
    public List<Child> getAllChildren() {
        return childMapper.selectAll();
    }

    @Override
    public Child getChildById(Long id) {
        return childMapper.selectById(id);
    }

    @Override
    public int createChild(Child child) {
        return childMapper.insert(child);
    }

    @Override
    public int updateChild(Child child) {
        return childMapper.updateById(child);
    }

    @Override
    public int deleteChild(Long id) {
        return childMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> login(String phone, String name) {
        if (phone == null || name == null) {
            return null;
        }
        
        Child child = childMapper.selectByPhoneAndName(phone, name);
        if (child != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("childId", child.getId());
            result.put("childName", child.getName());
            result.put("phone", child.getPhone());
            return result;
        }
        
        return null;
    }

    @Override
    public Map<String, Object> recharge(Long elderId, BigDecimal amount) {
        if (elderId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        
        // 查询老人账户
        Account account = accountMapper.selectByElderId(elderId);
        if (account == null) {
            return null;
        }
        
        // 更新余额
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());
        accountMapper.updateById(account);
        
        // 创建账单记录
        Bill bill = new Bill();
        bill.setElderId(elderId);
        bill.setAmount(amount);
        bill.setBillType("充值");
        bill.setDescription("子女充值");
        bill.setCreatedAt(LocalDateTime.now());
        billMapper.insert(bill);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("newBalance", newBalance);
        result.put("billId", bill.getId());
        return result;
    }

    @Override
    public List<Bill> getBills(Long elderId) {
        if (elderId == null) {
            return new ArrayList<>();
        }
        return billMapper.selectByElderId(elderId);
    }

    @Override
    public Map<String, Object> createReview(ReviewRequest request) {
        if (request == null || request.getElderId() == null || request.getChildId() == null) {
            return null;
        }
        
        // 检查是否已经评价过本月
        String reviewMonth = request.getReviewMonth();
        if (reviewMonth == null) {
            reviewMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
        List<ServiceReview> existingReviews = serviceReviewMapper.selectByElderAndMonth(
            request.getElderId(), reviewMonth);
        if (!existingReviews.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "本月已评价");
            return result;
        }
        
        // 创建评价记录
        ServiceReview review = new ServiceReview();
        review.setElderId(request.getElderId());
        review.setChildId(request.getChildId());
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setReviewMonth(reviewMonth);
        review.setCreatedAt(LocalDateTime.now());
        serviceReviewMapper.insert(review);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("reviewId", review.getId());
        return result;
    }

    @Override
    public List<Elder> getAssociatedElders(Long childId) {
        if (childId == null) {
            return new ArrayList<>();
        }
        List<ElderChild> elderChilds = elderChildMapper.selectByChildId(childId);
        List<Elder> elders = new ArrayList<>();
        for (ElderChild elderChild : elderChilds) {
            Elder elder = elderMapper.selectById(elderChild.getElderId());
            if (elder != null) {
                elders.add(elder);
            }
        }
        return elders;
    }

    @Override
    public Map<String, Object> getElderStatus(Long elderId) {
        if (elderId == null) {
            return null;
        }
        
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            return null;
        }
        
        // 查询账户信息
        Account account = accountMapper.selectByElderId(elderId);
        
        // 查询今日刷脸记录
        List<FaceCheckRecord> todayRecords = faceCheckRecordMapper.selectTodayByElderId(elderId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("elder", elder);
        status.put("account", account);
        status.put("todayFaceCheck", todayRecords.isEmpty() ? null : todayRecords.get(0));
        status.put("hasFaceCheckToday", !todayRecords.isEmpty());
        
        return status;
    }
}
