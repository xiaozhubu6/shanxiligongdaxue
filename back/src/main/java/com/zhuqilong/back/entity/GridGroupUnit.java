package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GridGroupUnit {
    private Long id;
    private Long gridGroupId;
    private String unitNumber;
    private LocalDateTime createdAt;
    
    // 关联查询字段
    private String gridGroupName;
    private String communityName;
    private Integer elderCount; // 该单元的老人数量
}
