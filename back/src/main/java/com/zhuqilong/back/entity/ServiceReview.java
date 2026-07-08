package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceReview {
    private Long id;
    private Long childId;
    private String comment;
    private LocalDateTime createdAt;
    private Long elderId;
    private String reviewMonth;
    private Integer score;
    private String type; // ELDER 或 CHILD
    private String status; // PENDING, PUSHED, COMPLETED
    private LocalDateTime completedAt;
    private Integer attitude; // 服务态度
    private Integer response; // 响应速度
    private Integer quality; // 服务质量
    private Integer satisfaction; // 整体满意度
    private String suggestion; // 改进建议
}
