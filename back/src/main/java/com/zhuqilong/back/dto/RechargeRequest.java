package com.zhuqilong.back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Schema(description = "充值请求")
public class RechargeRequest {
    
    @NotNull(message = "老人ID不能为空")
    @Schema(description = "老人ID", example = "1")
    private Long elderId;
    
    @NotNull(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;
}
