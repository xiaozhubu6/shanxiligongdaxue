package com.java1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("t_announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String content;
    
    private Date createTime;
    
    private Integer status;
}