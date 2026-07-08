package com.java1234.controller;

import com.java1234.entity.Announcement;
import com.java1234.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {
    
    @Autowired
    private AnnouncementService announcementService;
    
    @GetMapping("/list")
    public List<Announcement> list() {
        return announcementService.getAllList();
    }
    
    @PostMapping("/add")
    public boolean add(@RequestBody Announcement announcement) {
        announcement.setCreateTime(new Date());
        announcement.setStatus(1);
        return announcementService.save(announcement);
    }
    
    @PostMapping("/update")
    public boolean update(@RequestBody Announcement announcement) {
        return announcementService.updateById(announcement);
    }
    
    @GetMapping("/delete")
    public boolean delete(Integer id) {
        return announcementService.removeById(id);
    }
}