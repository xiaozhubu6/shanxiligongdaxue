package com.java1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java1234.entity.Admin;

/**
 * 管理员Mapper接口

 */
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 修改
     * @param admin
     * @return
     */
    public Integer update(Admin admin);

}
