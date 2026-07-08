package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GridGroup {
    private Long id;
    private Long communityId;
    private LocalDateTime createdAt;
    private String name;  // 楼栋名称，如"1号楼"
    private String unitNumber;  // 临时保留，与数据库保持一致
    
    // 关联查询字段
    private String communityName;
    private List<String> unitNumbers;  // 该楼栋的所有单元号
    private Integer totalElderCount;     // 该楼栋的老人总数
    private Integer unitCount;           // 单元数量
}
