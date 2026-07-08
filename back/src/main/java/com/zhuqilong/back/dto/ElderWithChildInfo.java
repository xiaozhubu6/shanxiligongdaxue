package com.zhuqilong.back.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ElderWithChildInfo {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String status;
    private Long gridGroupId;
    private String communityName; // 社区名称
    private LocalDateTime createdAt;
    private String phone;  // 老人手机号
    private String unitNumber;  // 单元号
    private String roomNumber;  // 房间号
    
    // 子女信息列表
    private List<ChildInfo> children;
    
    @Data
    public static class ChildInfo {
        private Long id;
        private String name;
        private String phone;
        private LocalDateTime childCreatedAt;
    }
}
