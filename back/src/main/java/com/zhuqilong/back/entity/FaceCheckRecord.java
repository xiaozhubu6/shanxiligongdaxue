package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FaceCheckRecord {
    private Long id;
    private LocalDateTime checkTime;
    private Long elderId;
    private String result;
    private String photoUrl;  // 照片URL
    private String photoData; // Base64编码的照片数据
    private Boolean adminAssisted; // 管理员协助标记
    private String recheckReason; // 重新打卡原因
    private String description; // 描述
    
    // 用于查询结果中的老人信息
    private String elderName;
    private Integer elderAge;
    private String elderGender;
}
