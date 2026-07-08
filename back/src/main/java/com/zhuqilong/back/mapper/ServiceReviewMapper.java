package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.ServiceReview;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ServiceReviewMapper {
    int insert(ServiceReview serviceReview);
    int updateById(ServiceReview serviceReview);
    int deleteById(Long id);
    int deleteAll();
    ServiceReview selectById(Long id);
    List<ServiceReview> selectAll();
    List<ServiceReview> selectByElderId(Long elderId);
    List<ServiceReview> selectByChildId(Long childId);
    List<ServiceReview> selectByElderAndMonth(Long elderId, String reviewMonth);
    List<ServiceReview> selectByChildAndMonth(Long childId, String reviewMonth);
    List<ServiceReview> selectByMonth(String reviewMonth);
}
