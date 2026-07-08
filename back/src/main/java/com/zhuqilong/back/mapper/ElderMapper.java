package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.dto.ElderWithChildInfo;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ElderMapper {
    int insert(Elder elder);
    int updateById(Elder elder);
    int deleteById(Long id);
    Elder selectById(Long id);
    List<Elder> selectAll();
    List<Elder> selectByGridGroupId(Long gridGroupId);
    List<Elder> selectByChildId(Long childId);
    
    // 临时方法：根据单元获取老人（暂时返回空列表）
    @Select("SELECT * FROM elder WHERE 1=0")  // 临时返回空列表
    List<Elder> selectByGridGroupUnitId(Long gridGroupUnitId);
    
    List<Elder> selectAbnormalElders();
    
    // 删除老人相关的绑定关系
    int deleteElderChildByElderId(Long elderId);
    
    // 获取老人及其子女信息
    ElderWithChildInfo selectElderWithChildInfo(Long elderId);
    List<ElderWithChildInfo> selectAllEldersWithChildInfo();
    
    // 获取包含社区名的老人信息
    Elder selectElderWithCommunity(Long id);
    List<Elder> selectAllEldersWithCommunity();
}
