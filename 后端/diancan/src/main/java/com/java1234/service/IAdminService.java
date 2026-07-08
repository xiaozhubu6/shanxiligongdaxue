package com.java1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java1234.entity.Admin;



public interface IAdminService extends IService<Admin> {

    /**
     * 修改
     * @param admin
     * @return
     */
    public Integer update(Admin admin);

}
