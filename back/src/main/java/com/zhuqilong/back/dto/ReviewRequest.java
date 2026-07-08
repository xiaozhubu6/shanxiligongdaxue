package com.zhuqilong.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "评价请求")
public class ReviewRequest {
    
    @NotNull(message = "老人ID不能为空")
    @Schema(description = "老人ID", example = "1")
    private Long elderId;
    
    @NotNull(message = "子女ID不能为空")
    @Schema(description = "子女ID", example = "1")
    private Long childId;
    
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    @Schema(description = "评分(1-5分)", example = "5")
    private Integer score;
    
    @Schema(description = "评价内容", example = "服务很好，老人很满意")
    private String comment;
    
    @Schema(description = "评价月份", example = "2024-12")
    private String reviewMonth;
}
