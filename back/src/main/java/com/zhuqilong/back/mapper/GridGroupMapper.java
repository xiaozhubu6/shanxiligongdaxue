package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.GridGroup;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GridGroupMapper {
    @Insert("INSERT INTO grid_group (community_id, created_at, unit_number, name) " +
            "VALUES (#{communityId}, #{createdAt}, #{unitNumber}, #{name})")
    int insert(GridGroup gridGroup);
    
    int updateById(GridGroup gridGroup);
    int deleteById(Long id);
    GridGroup selectById(Long id);
    List<GridGroup> selectAll();
    List<GridGroup> selectByCommunityId(Long communityId);
    
    // 临时方法：获取带单元信息的网格群（不依赖grid_group_unit表）
    @Select("SELECT gg.*, c.name as communityName, " +
            "0 as unitCount, " +  // 临时设为0
            "0 as totalElderCount " +  // 临时设为0
            "FROM grid_group gg " +
            "LEFT JOIN community c ON gg.community_id = c.id " +
            "WHERE gg.community_id = #{communityId}")
    List<GridGroup> selectWithUnitCountByCommunityId(Long communityId);
    
    @Select("SELECT gg.*, c.name as communityName, " +
            "0 as unitCount, " +  // 临时设为0
            "0 as totalElderCount " +  // 临时设为0
            "FROM grid_group gg " +
            "LEFT JOIN community c ON gg.community_id = c.id")
    List<GridGroup> selectAllWithUnitCount();
}
