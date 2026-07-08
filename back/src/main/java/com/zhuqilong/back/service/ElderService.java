package com.zhuqilong.back.service;

import com.zhuqilong.back.entity.Elder;
import java.util.List;

public interface ElderService {
    int createElder(Elder elder);
    int updateElder(Elder elder);
    int deleteElder(Long id);
    Elder getElderById(Long id);
    List<Elder> getAllElders();
    List<Elder> getEldersByGridGroupId(Long gridGroupId);
    List<Elder> getEldersByChildId(Long childId);
    List<Elder> getEldersByGridGroupUnitId(Long gridGroupUnitId);
    List<Elder> getAbnormalElders();
}
