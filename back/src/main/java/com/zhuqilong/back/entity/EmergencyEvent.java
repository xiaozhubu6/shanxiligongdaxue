package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmergencyEvent {
    private Long id;
    private LocalDateTime createdAt;
    private Long elderId;
    private String eventType;
    private LocalDateTime handledAt;
    private String status;
    private String description;
}
