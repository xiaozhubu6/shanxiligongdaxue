package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.GridGroupUnit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GridGroupUnitMapper {
    
    @Select("SELECT * FROM grid_group_unit WHERE grid_group_id = #{gridGroupId}")
    List<GridGroupUnit> selectByGridGroupId(Long gridGroupId);
    
    @Select("SELECT ggu.*, gg.name as gridGroupName, c.name as communityName, " +
            "(SELECT COUNT(*) FROM elder e WHERE e.grid_group_unit_id = ggu.id) as elderCount " +
            "FROM grid_group_unit ggu " +
            "LEFT JOIN grid_group gg ON ggu.grid_group_id = gg.id " +
            "LEFT JOIN community c ON gg.community_id = c.id " +
            "WHERE ggu.grid_group_id = #{gridGroupId}")
    List<GridGroupUnit> selectWithDetailsByGridGroupId(Long gridGroupId);
    
    @Select("SELECT * FROM grid_group_unit WHERE id = #{id}")
    GridGroupUnit selectById(Long id);
    
    @Insert("INSERT INTO grid_group_unit (grid_group_id, unit_number, created_at) VALUES (#{gridGroupId}, #{unitNumber}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GridGroupUnit gridGroupUnit);
    
    @Update("UPDATE grid_group_unit SET unit_number = #{unitNumber} WHERE id = #{id}")
    int updateById(GridGroupUnit gridGroupUnit);
    
    @Delete("DELETE FROM grid_group_unit WHERE id = #{id}")
    int deleteById(Long id);
    
    @Delete("DELETE FROM grid_group_unit WHERE grid_group_id = #{gridGroupId}")
    int deleteByGridGroupId(Long gridGroupId);
    
    @Select("SELECT ggu.*, gg.name as gridGroupName, c.name as communityName " +
            "FROM grid_group_unit ggu " +
            "LEFT JOIN grid_group gg ON ggu.grid_group_id = gg.id " +
            "LEFT JOIN community c ON gg.community_id = c.id " +
            "ORDER BY c.name, gg.name, ggu.unit_number")
    List<GridGroupUnit> selectAllWithDetails();
}
