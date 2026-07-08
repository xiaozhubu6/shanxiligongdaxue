package com.java1234.service;

import com.java1234.dto.CostDTO;
import com.java1234.entity.Cost;
import com.java1234.util.Result;

import java.util.Date;

public interface CostService {
    Result getCostList(Date startDate, Date endDate);
    Result addCost(CostDTO costDTO);
    Result updateCost(CostDTO costDTO);
    Result deleteCost(Integer id);
    Result getCostStatistics(Date startDate, Date endDate);
} 