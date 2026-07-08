package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ElderChild {
    private Long id;
    private Long childId;
    private LocalDateTime createdAt;
    private Long elderId;
}
