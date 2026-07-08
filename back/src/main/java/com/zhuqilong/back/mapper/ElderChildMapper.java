package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.ElderChild;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ElderChildMapper {
    int insert(ElderChild elderChild);
    int updateById(ElderChild elderChild);
    int deleteById(Long id);
    int deleteByChildAndElder(Long childId, Long elderId);
    ElderChild selectById(Long id);
    List<ElderChild> selectAll();
    List<ElderChild> selectByElderId(Long elderId);
    List<ElderChild> selectByChildId(Long childId);
}
