package com.java1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Table;
import com.java1234.mapper.TableMapper;
import com.java1234.service.ITableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;



@Service("tableService")
public class ITableServiceImpl extends ServiceImpl<TableMapper,Table> implements ITableService {

   @Autowired
   private TableMapper tableMapper;


   @Override
   public List<Table> list(Map<String, Object> map) {
      return tableMapper.list(map);
   }

   @Override
   public Long getTotal(Map<String, Object> map) {
      return tableMapper.getTotal(map);
   }
}
