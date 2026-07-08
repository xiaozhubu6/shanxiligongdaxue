package com.zhuqilong.back.service;

import com.zhuqilong.back.entity.GridGroupUnit;
import com.zhuqilong.back.mapper.GridGroupUnitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GridGroupUnitService {
    
    @Autowired
    private GridGroupUnitMapper gridGroupUnitMapper;
    
    public List<GridGroupUnit> getUnitsByGridGroupId(Long gridGroupId) {
        return gridGroupUnitMapper.selectByGridGroupId(gridGroupId);
    }
    
    public List<GridGroupUnit> getUnitsWithDetailsByGridGroupId(Long gridGroupId) {
        return gridGroupUnitMapper.selectWithDetailsByGridGroupId(gridGroupId);
    }
    
    public GridGroupUnit getUnitById(Long id) {
        return gridGroupUnitMapper.selectById(id);
    }
    
    public int createUnit(GridGroupUnit unit) {
        unit.setCreatedAt(LocalDateTime.now());
        return gridGroupUnitMapper.insert(unit);
    }
    
    public int updateUnit(GridGroupUnit unit) {
        return gridGroupUnitMapper.updateById(unit);
    }
    
    public int deleteUnit(Long id) {
        return gridGroupUnitMapper.deleteById(id);
    }
    
    public int deleteUnitsByGridGroupId(Long gridGroupId) {
        return gridGroupUnitMapper.deleteByGridGroupId(gridGroupId);
    }
    
    public List<GridGroupUnit> getAllUnitsWithDetails() {
        return gridGroupUnitMapper.selectAllWithDetails();
    }
}
