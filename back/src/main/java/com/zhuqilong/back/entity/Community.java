package com.zhuqilong.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Community {
    private Long id;
    private String address;
    private LocalDateTime createdAt;
    private String name;
}
