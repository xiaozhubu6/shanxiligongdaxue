package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.PurchaseRequest;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.mapper.PurchaseRequestMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.mapper.ElderChildMapper;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.mapper.BillMapper;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchase")
@Tag(name = "代购管理", description = "代购请求相关接口")
@CrossOrigin(origins = "*")
public class PurchaseRequestController {

    @Autowired
    private PurchaseRequestMapper purchaseRequestMapper;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private ElderChildMapper elderChildMapper;
    
    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private BillMapper billMapper;

    @PostMapping
    @Operation(summary = "创建代购请求")
    public ResponseEntity<ApiResponse<PurchaseRequest>> createRequest(@RequestBody PurchaseRequest request) {
        try {
            request.setCreatedAt(LocalDateTime.now());
            request.setStatus("pending");
            // estimatedAmount可以为null，由管理员确认时填写
            int result = purchaseRequestMapper.insert(request);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success(request));
            }
            return ResponseEntity.ok(ApiResponse.error("创建代购请求失败"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("创建代购请求失败: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取代购统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 待处理数量
            List<PurchaseRequest> pendingRequests = purchaseRequestMapper.selectByStatus("pending");
            statistics.put("pending", pendingRequests.size());
            
            // 今日完成数量
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            
            List<PurchaseRequest> allRequests = purchaseRequestMapper.selectAll();
            long todayCount = allRequests.stream()
                .filter(req -> "confirmed".equals(req.getStatus()) || "completed".equals(req.getStatus()))
                .filter(req -> req.getCreatedAt() != null && 
                              req.getCreatedAt().isAfter(todayStart) && 
                              req.getCreatedAt().isBefore(todayEnd))
                .count();
            statistics.put("today", todayCount);
            
            // 本月完成数量
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long monthCount = allRequests.stream()
                .filter(req -> "confirmed".equals(req.getStatus()) || "completed".equals(req.getStatus()))
                .filter(req -> req.getCreatedAt() != null && req.getCreatedAt().isAfter(monthStart))
                .count();
            statistics.put("month", monthCount);
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "获取老人的代购请求")
    public ResponseEntity<List<PurchaseRequest>> getRequestsByElder(
            @Parameter(description = "老人ID") @PathVariable Long elderId) {
        List<PurchaseRequest> requests = purchaseRequestMapper.selectByElderId(elderId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/associated-elders")
    @Operation(summary = "获取关联的老人列表")
    public ResponseEntity<List<Elder>> getAssociatedElders() {
        try {
            // 暂时返回所有老人作为关联老人（简化处理）
            List<Elder> elders = elderMapper.selectAll();
            return ResponseEntity.ok(elders);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理的代购请求")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingRequests() {
        try {
            List<PurchaseRequest> requests = purchaseRequestMapper.selectByStatus("pending");
            
            // 转换为包含老人信息的完整数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (PurchaseRequest request : requests) {
                Map<String, Object> item = new HashMap<>();
                Elder elder = elderMapper.selectById(request.getElderId());
                
                item.put("id", request.getId());
                item.put("content", request.getContent());
                item.put("estimatedAmount", request.getEstimatedAmount());
                item.put("actualAmount", request.getActualAmount());
                item.put("remark", request.getRemark());
                item.put("status", request.getStatus());
                item.put("createdAt", request.getCreatedAt());
                item.put("updatedAt", request.getUpdatedAt());
                item.put("elderId", request.getElderId());
                item.put("elderName", elder != null ? elder.getName() : "未知老人");
                item.put("elderAge", elder != null ? elder.getAge() : null);
                item.put("elderGender", elder != null ? elder.getGender() : null);
                
                result.add(item);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取待处理请求失败: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有代购请求")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllRequests() {
        try {
            List<PurchaseRequest> requests = purchaseRequestMapper.selectAll();
            
            // 转换为包含老人信息的完整数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (PurchaseRequest request : requests) {
                Map<String, Object> item = new HashMap<>();
                Elder elder = elderMapper.selectById(request.getElderId());
                
                item.put("id", request.getId());
                item.put("content", request.getContent());
                item.put("estimatedAmount", request.getEstimatedAmount());
                item.put("actualAmount", request.getActualAmount());
                item.put("remark", request.getRemark());
                item.put("status", request.getStatus());
                item.put("createdAt", request.getCreatedAt());
                item.put("updatedAt", request.getUpdatedAt());
                item.put("elderId", request.getElderId());
                item.put("elderName", elder != null ? elder.getName() : "未知老人");
                item.put("elderAge", elder != null ? elder.getAge() : null);
                item.put("elderGender", elder != null ? elder.getGender() : null);
                
                result.add(item);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取所有请求失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{requestId}/confirm")
    @Operation(summary = "确认代购请求")
    public ResponseEntity<ApiResponse<String>> confirmRequest(
            @Parameter(description = "请求ID") @PathVariable Long requestId,
            @RequestBody Map<String, Object> request) {
        try {
            PurchaseRequest existingRequest = purchaseRequestMapper.selectById(requestId);
            if (existingRequest == null) {
                return ResponseEntity.notFound().build();
            }
            
            Object actualAmountObj = request.get("actualAmount");
            String remark = (String) request.get("remark");
            
            if (actualAmountObj == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("实际金额不能为空"));
            }
            
            // 处理不同类型的数字输入
            double actualAmount;
            if (actualAmountObj instanceof Integer) {
                actualAmount = ((Integer) actualAmountObj).doubleValue();
            } else if (actualAmountObj instanceof Double) {
                actualAmount = (Double) actualAmountObj;
            } else if (actualAmountObj instanceof String) {
                actualAmount = Double.parseDouble((String) actualAmountObj);
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("金额格式错误"));
            }
            
            // 检查老人账户余额
            Account account = accountMapper.selectByElderId(existingRequest.getElderId());
            if (account == null || account.getBalance().doubleValue() < actualAmount) {
                return ResponseEntity.badRequest().body(ApiResponse.error("账户余额不足"));
            }
            
            // 扣款
            account.setBalance(account.getBalance().subtract(java.math.BigDecimal.valueOf(actualAmount)));
            account.setUpdatedAt(LocalDateTime.now());
            accountMapper.updateById(account);
            
            // 更新代购请求状态
            existingRequest.setActualAmount(java.math.BigDecimal.valueOf(actualAmount));
            existingRequest.setRemark(remark);
            existingRequest.setStatus("confirmed");
            existingRequest.setUpdatedAt(LocalDateTime.now());
            purchaseRequestMapper.updateById(existingRequest);
            
            // 生成账单
            Bill bill = new Bill();
            bill.setAmount(java.math.BigDecimal.valueOf(actualAmount));
            bill.setBillType("代购消费");
            bill.setDescription("代购：" + existingRequest.getContent() + 
                              (remark != null ? " 备注：" + remark : ""));
            bill.setElderId(existingRequest.getElderId());
            bill.setCreatedAt(LocalDateTime.now());
            billMapper.insert(bill);
            
            return ResponseEntity.ok(ApiResponse.success("代购确认成功，已扣款并生成账单"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("确认代购失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{requestId}/reject")
    @Operation(summary = "拒绝代购请求")
    public ResponseEntity<ApiResponse<String>> rejectRequest(
            @Parameter(description = "请求ID") @PathVariable Long requestId,
            @RequestBody Map<String, String> request) {
        try {
            PurchaseRequest existingRequest = purchaseRequestMapper.selectById(requestId);
            if (existingRequest != null) {
                existingRequest.setStatus("rejected");
                int result = purchaseRequestMapper.updateById(existingRequest);
                
                if (result > 0) {
                    String reason = request.get("reason");
                    return ResponseEntity.ok(ApiResponse.success("代购请求已拒绝"));
                } else {
                    return ResponseEntity.status(500).body(ApiResponse.error("更新失败"));
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("拒绝代购失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{requestId}/complete")
    @Operation(summary = "完成代购请求")
    public ResponseEntity<PurchaseRequest> completeRequest(@Parameter(description = "请求ID") @PathVariable Long requestId) {
        PurchaseRequest request = purchaseRequestMapper.selectById(requestId);
        if (request != null) {
            request.setStatus("completed");
            purchaseRequestMapper.updateById(request);
            return ResponseEntity.ok(request);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requestId}")
    @Operation(summary = "删除代购请求")
    public ResponseEntity<Void> deleteRequest(@Parameter(description = "请求ID") @PathVariable Long requestId) {
        int result = purchaseRequestMapper.deleteById(requestId);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
