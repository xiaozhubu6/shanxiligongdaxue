package com.zhuqilong.back.mapper;

import com.zhuqilong.back.entity.FaceCheckRecord;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FaceCheckRecordMapper {
    int insert(FaceCheckRecord faceCheckRecord);
    int updateById(FaceCheckRecord faceCheckRecord);
    int deleteById(Long id);
    FaceCheckRecord selectById(Long id);
    List<FaceCheckRecord> selectAll();
    List<FaceCheckRecord> selectByElderId(Long elderId);
    List<FaceCheckRecord> selectByDate(LocalDate date);
    List<FaceCheckRecord> selectTodayByElderId(Long elderId);
    List<FaceCheckRecord> selectPhotosByElder(Long elderId);
}
