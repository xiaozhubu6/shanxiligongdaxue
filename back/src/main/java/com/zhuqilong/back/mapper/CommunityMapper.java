package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.Community;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommunityMapper {
    int insert(Community community);
    int updateById(Community community);
    int deleteById(Long id);
    Community selectById(Long id);
    List<Community> selectAll();
}
