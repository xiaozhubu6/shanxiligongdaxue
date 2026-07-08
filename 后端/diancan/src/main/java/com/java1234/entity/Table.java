package com.java1234.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 桌号实体

 */
@TableName("t_table")
@Data
public class Table implements Serializable {

    private Integer id; // 编号

    private String number; // 桌号

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date time; // 创建日期

    private String qrcode; // 二维码地址


}
