package com.java1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Unit;
import com.java1234.mapper.UnitMapper;
import com.java1234.service.IUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("unitService")
public class IUnitServiceImpl extends ServiceImpl<UnitMapper,Unit> implements IUnitService {

    @Autowired
    private UnitMapper unitMapper;

}
