package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.Announcement;
import java.util.List;

public interface AnnouncementService extends IService<Announcement> {
    List<Announcement> getEnabledList();
    
    List<Announcement> getAllList();
}