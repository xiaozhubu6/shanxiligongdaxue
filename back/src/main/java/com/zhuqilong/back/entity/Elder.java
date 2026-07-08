package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Elder {
    private Long id;
    private Integer age;
    private LocalDateTime createdAt;
    private String gender;
    private Long gridGroupId;
    private String name;
    private String status;      // 备注信息
    private String unitNumber;  // 单元号
    private String phone;       // 手机号
    private String roomNumber;  // 房间号
    private String communityName; // 社区名称（用于查询结果）
}
