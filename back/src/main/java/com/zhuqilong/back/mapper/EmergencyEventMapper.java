package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.EmergencyEvent;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface EmergencyEventMapper {
    int insert(EmergencyEvent emergencyEvent);
    int updateById(EmergencyEvent emergencyEvent);
    int deleteById(Long id);
    EmergencyEvent selectById(Long id);
    List<EmergencyEvent> selectAll();
    List<EmergencyEvent> selectByElderId(Long elderId);
    List<EmergencyEvent> selectByStatus(String status);
}
