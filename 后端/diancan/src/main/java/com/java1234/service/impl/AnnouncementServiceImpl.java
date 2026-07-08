package com.java1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java1234.entity.Announcement;
import com.java1234.mapper.AnnouncementMapper;
import com.java1234.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
    
    @Override
    public List<Announcement> getEnabledList() {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("createTime");
        return list(wrapper);
    }
    
    @Override
    public List<Announcement> getAllList() {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("createTime");
        return list(wrapper);
    }
}