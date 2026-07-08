package com.zhuqilong.back.service.impl;

import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.service.ElderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElderServiceImpl implements ElderService {

    @Autowired
    private ElderMapper elderMapper;

    @Override
    public int createElder(Elder elder) {
        return elderMapper.insert(elder);
    }

    @Override
    public Elder getElderById(Long id) {
        return elderMapper.selectById(id);
    }

    @Override
    public int updateElder(Elder elder) {
        return elderMapper.updateById(elder);
    }

    @Override
    public int deleteElder(Long id) {
        return elderMapper.deleteById(id);
    }

    @Override
    public List<Elder> getAllElders() {
        return elderMapper.selectAll();
    }

    @Override
    public List<Elder> getEldersByGridGroupId(Long gridGroupId) {
        return elderMapper.selectByGridGroupId(gridGroupId);
    }

    @Override
    public List<Elder> getEldersByChildId(Long childId) {
        try {
            return elderMapper.selectByChildId(childId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<Elder> getEldersByGridGroupUnitId(Long gridGroupUnitId) {
        try {
            return elderMapper.selectByGridGroupUnitId(gridGroupUnitId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public List<Elder> getAbnormalElders() {
        try {
            return elderMapper.selectAbnormalElders();
        } catch (Exception e) {
            return List.of();
        }
    }
}
