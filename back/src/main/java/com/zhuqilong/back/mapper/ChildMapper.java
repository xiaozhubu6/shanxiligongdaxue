package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.Child;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ChildMapper {
    int insert(Child child);
    int updateById(Child child);
    int deleteById(Long id);
    Child selectById(Long id);
    List<Child> selectAll();
    Child selectByPhoneAndName(String phone, String name);
    List<Child> selectByElderId(Long elderId);
}
