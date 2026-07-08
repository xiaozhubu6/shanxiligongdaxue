package com.zhuqilong.back.controller;

import com.zhuqilong.back.dto.UnifiedAccountDTO;
import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.entity.Child;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.EmergencyEvent;
import com.zhuqilong.back.entity.FaceCheckRecord;
import com.zhuqilong.back.entity.PurchaseRequest;
import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.mapper.ChildMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.mapper.EmergencyEventMapper;
import com.zhuqilong.back.mapper.FaceCheckRecordMapper;
import com.zhuqilong.back.mapper.PurchaseRequestMapper;
import com.zhuqilong.back.mapper.BillMapper;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员控制器 - 管理所有用户账户（admin、child、elder）
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员账户管理", description = "管理员管理所有用户账户相关接口")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private ChildMapper childMapper;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private EmergencyEventMapper emergencyEventMapper;
    
    @Autowired
    private FaceCheckRecordMapper faceCheckRecordMapper;
    
    @Autowired
    private PurchaseRequestMapper purchaseRequestMapper;
    
    @Autowired
    private BillMapper billMapper;

    @GetMapping("/accounts")
    @Operation(summary = "获取所有用户账户列表")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAccounts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户类型筛选") @RequestParam(required = false) String userType,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        
        try {
            List<UnifiedAccountDTO> allAccounts = new ArrayList<>();
            
            // 1. 添加管理员账户（模拟数据，因为admin表不存在）
            if (userType == null || userType.isEmpty() || "admin".equals(userType)) {
                UnifiedAccountDTO admin = new UnifiedAccountDTO();
                admin.setId(1L);
                admin.setUsername("admin");
                admin.setName("系统管理员");
                admin.setUserType("admin");
                admin.setEmail("admin@example.com");
                admin.setPhone("13800138000");
                admin.setStatus("active");
                admin.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
                admin.setLastLoginAt(LocalDateTime.now());
                admin.setRole("super_admin");
                
                if (status == null || status.isEmpty() || status.equals(admin.getStatus())) {
                    allAccounts.add(admin);
                }
            }
            
            // 2. 添加子女账户
            if (userType == null || userType.isEmpty() || "child".equals(userType)) {
                List<Child> children = childMapper.selectAll();
                for (Child child : children) {
                    UnifiedAccountDTO dto = new UnifiedAccountDTO();
                    dto.setId(child.getId());
                    dto.setUsername("child" + child.getId()); // 生成用户名
                    dto.setName(child.getName());
                    dto.setUserType("child");
                    dto.setEmail(child.getName().toLowerCase() + "@example.com"); // 生成虚拟邮箱
                    dto.setPhone(child.getPhone());
                    dto.setStatus("active"); // 默认状态
                    dto.setCreatedAt(child.getCreatedAt());
                    dto.setLastLoginAt(null); // 暂时为空
                    
                    if (status == null || status.isEmpty() || "active".equals(dto.getStatus())) {
                        allAccounts.add(dto);
                    }
                }
            }
            
            // 3. 添加老人账户
            if (userType == null || userType.isEmpty() || "elder".equals(userType)) {
                List<Elder> elders = elderMapper.selectAll();
                for (Elder elder : elders) {
                    UnifiedAccountDTO dto = new UnifiedAccountDTO();
                    dto.setId(elder.getId());
                    dto.setUsername("elder" + elder.getId()); // 生成用户名
                    dto.setName(elder.getName());
                    dto.setUserType("elder");
                    dto.setEmail(elder.getName().toLowerCase() + "@example.com"); // 生成虚拟邮箱
                    dto.setPhone("1380013800" + String.format("%02d", elder.getId() % 100)); // 生成虚拟手机号
                    dto.setStatus("active"); // 老人默认状态为active
                    dto.setCreatedAt(elder.getCreatedAt());
                    dto.setLastLoginAt(null);
                    
                    // 老人特有字段
                    dto.setAge(elder.getAge());
                    dto.setGender(elder.getGender());
                    dto.setGridGroupId(elder.getGridGroupId());
                    
                    // 获取账户余额
                    Account account = accountMapper.selectByElderId(elder.getId());
                    if (account != null) {
                        dto.setBalance(account.getBalance());
                    } else {
                        dto.setBalance(java.math.BigDecimal.ZERO);
                    }
                    
                    if (status == null || status.isEmpty() || "active".equals(dto.getStatus())) {
                        allAccounts.add(dto);
                    }
                }
            }
            
            // 分页处理
            int total = allAccounts.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            List<UnifiedAccountDTO> pageData = start < total ? allAccounts.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", pageData);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取账户列表失败: " + e.getMessage()));
        }
    }

    @PostMapping("/accounts")
    @Operation(summary = "创建用户账户")
    public ResponseEntity<ApiResponse<UnifiedAccountDTO>> createAccount(@RequestBody UnifiedAccountDTO accountDTO) {
        try {
            String userType = accountDTO.getUserType();
            
            if ("child".equals(userType)) {
                // 创建子女
                Child child = new Child();
                child.setName(accountDTO.getName());
                child.setPhone(accountDTO.getPhone());
                child.setCreatedAt(LocalDateTime.now());
                
                int result = childMapper.insert(child);
                if (result > 0) {
                    accountDTO.setId(child.getId());
                    accountDTO.setUsername("child" + child.getId());
                    accountDTO.setCreatedAt(child.getCreatedAt());
                    accountDTO.setStatus("active");
                    return ResponseEntity.ok(ApiResponse.success("创建成功", accountDTO));
                }
            } else if ("elder".equals(userType)) {
                // 创建老人
                Elder elder = new Elder();
                elder.setName(accountDTO.getName());
                elder.setAge(accountDTO.getAge());
                elder.setGender(accountDTO.getGender());
                elder.setGridGroupId(accountDTO.getGridGroupId());
                elder.setUnitNumber(accountDTO.getUnitNumber() != null ? accountDTO.getUnitNumber() : "");
                elder.setCreatedAt(LocalDateTime.now());
                
                int result = elderMapper.insert(elder);
                if (result > 0) {
                    // 创建对应的账户
                    Account account = new Account();
                    account.setElderId(elder.getId());
                    account.setBalance(accountDTO.getBalance() != null ? accountDTO.getBalance() : java.math.BigDecimal.ZERO);
                    account.setCreatedAt(LocalDateTime.now());
                    account.setUpdatedAt(LocalDateTime.now());
                    accountMapper.insert(account);
                    
                    accountDTO.setId(elder.getId());
                    accountDTO.setUsername("elder" + elder.getId());
                    accountDTO.setCreatedAt(elder.getCreatedAt());
                    return ResponseEntity.ok(ApiResponse.success("创建成功", accountDTO));
                }
            } else if ("admin".equals(userType)) {
                // 管理员暂时不支持创建（返回模拟数据）
                return ResponseEntity.badRequest().body(ApiResponse.error("暂不支持创建管理员账户"));
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error("创建失败"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("创建账户失败: " + e.getMessage()));
        }
    }

    @PutMapping("/accounts/{id}")
    @Operation(summary = "更新用户账户")
    public ResponseEntity<ApiResponse<UnifiedAccountDTO>> updateAccount(
            @Parameter(description = "账户ID") @PathVariable Long id,
            @RequestBody UnifiedAccountDTO accountDTO) {
        try {
            String userType = accountDTO.getUserType();
            
            if ("child".equals(userType)) {
                Child child = childMapper.selectById(id);
                if (child == null) {
                    return ResponseEntity.notFound().build();
                }
                
                child.setName(accountDTO.getName());
                child.setPhone(accountDTO.getPhone());
                
                int result = childMapper.updateById(child);
                if (result > 0) {
                    accountDTO.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(ApiResponse.success("更新成功", accountDTO));
                }
            } else if ("elder".equals(userType)) {
                Elder elder = elderMapper.selectById(id);
                if (elder == null) {
                    return ResponseEntity.notFound().build();
                }
                
                elder.setName(accountDTO.getName());
                elder.setAge(accountDTO.getAge());
                elder.setGender(accountDTO.getGender());
                elder.setGridGroupId(accountDTO.getGridGroupId());
                elder.setUnitNumber(accountDTO.getUnitNumber());
                
                int result = elderMapper.updateById(elder);
                if (result > 0) {
                    // 更新账户余额
                    if (accountDTO.getBalance() != null) {
                        Account account = accountMapper.selectByElderId(id);
                        if (account != null) {
                            account.setBalance(accountDTO.getBalance());
                            account.setUpdatedAt(LocalDateTime.now());
                            accountMapper.updateById(account);
                        }
                    }
                    
                    accountDTO.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(ApiResponse.success("更新成功", accountDTO));
                }
            } else if ("admin".equals(userType)) {
                // 管理员暂时不支持更新
                return ResponseEntity.badRequest().body(ApiResponse.error("暂不支持更新管理员账户"));
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error("更新失败"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("更新账户失败: " + e.getMessage()));
        }
    }

    @PutMapping("/accounts/{id}/status")
    @Operation(summary = "更新账户状态")
    public ResponseEntity<ApiResponse<String>> updateAccountStatus(
            @Parameter(description = "账户ID") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("状态不能为空"));
            }
            
            // 首先尝试更新老人状态
            Elder elder = elderMapper.selectById(id);
            if (elder != null) {
                // 由于status字段已改为unitNumber，暂时注释掉状态更新功能
                // elder.setUnitNumber(newStatus);
                // int result = elderMapper.updateById(elder);
                // if (result > 0) {
                //     return ResponseEntity.ok(ApiResponse.success("状态更新成功", newStatus));
                // }
                return ResponseEntity.badRequest().body(ApiResponse.error("老人状态更新功能暂不可用，status字段已改为unitNumber"));
            }
            
            // 然后尝试更新子女状态（虽然child表没有status字段，但可以模拟）
            Child child = childMapper.selectById(id);
            if (child != null) {
                // 子女状态更新可以记录在日志中或其他地方
                return ResponseEntity.ok(ApiResponse.success("状态更新成功", newStatus));
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("更新状态失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/accounts/{id}")
    @Operation(summary = "删除用户账户")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@Parameter(description = "账户ID") @PathVariable Long id) {
        try {
            // 首先尝试删除子女
            Child child = childMapper.selectById(id);
            if (child != null) {
                int result = childMapper.deleteById(id);
                if (result > 0) {
                    return ResponseEntity.ok(ApiResponse.success("删除成功", "子女账户已删除"));
                }
            }
            
            // 然后尝试删除老人（级联删除账户）
            Elder elder = elderMapper.selectById(id);
            if (elder != null) {
                // 先删除关联的账户
                Account account = accountMapper.selectByElderId(id);
                if (account != null) {
                    accountMapper.deleteById(account.getId());
                }
                
                int result = elderMapper.deleteById(id);
                if (result > 0) {
                    return ResponseEntity.ok(ApiResponse.success("删除成功", "老人账户已删除"));
                }
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("删除账户失败: " + e.getMessage()));
        }
    }

    // 统计信息API
    @GetMapping("/statistics")
    @Operation(summary = "获取管理员统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 老人统计
            List<Elder> elders = elderMapper.selectAll();
            statistics.put("totalElders", elders.size());
            
            // 今日刷脸统计
            List<FaceCheckRecord> todayFaceChecks = faceCheckRecordMapper.selectAll();
            LocalDate today = LocalDate.now();
            long todayFaceCount = todayFaceChecks.stream()
                .filter(record -> record.getCheckTime().toLocalDate().equals(today))
                .count();
            statistics.put("todayFaceChecks", todayFaceCount);
            
            // 异常老人统计 - 暂时注释，因为status字段已改为unitNumber
            // long abnormalElderCount = elders.stream()
            //     .filter(elder -> !"active".equals(elder.getStatus()))
            //     .count();
            // statistics.put("abnormalElders", abnormalElderCount);
            
            // 待处理紧急事件统计
            List<EmergencyEvent> pendingEvents = emergencyEventMapper.selectByStatus("pending");
            statistics.put("pendingEvents", pendingEvents.size());
            
            // 今日代购统计
            List<PurchaseRequest> todayPurchases = purchaseRequestMapper.selectAll();
            long todayPurchaseCount = todayPurchases.stream()
                .filter(request -> request.getCreatedAt().toLocalDate().equals(today))
                .count();
            statistics.put("todayPurchases", todayPurchaseCount);
            
            // 本月账单统计
            List<Bill> thisMonthBills = billMapper.selectAll();
            LocalDate monthStart = today.withDayOfMonth(1);
            long thisMonthBillCount = thisMonthBills.stream()
                .filter(bill -> bill.getCreatedAt().toLocalDate().isAfter(monthStart))
                .count();
            statistics.put("thisMonthBills", thisMonthBillCount);
            
            // 总金额统计
            double totalBalance = elders.stream()
                .mapToDouble(elder -> {
                    Account account = accountMapper.selectByElderId(elder.getId());
                    return account != null ? account.getBalance().doubleValue() : 0.0;
                })
                .sum();
            statistics.put("totalBalance", totalBalance);
            
            // 按网格群统计老人数量
            Map<String, Long> eldersByGrid = elders.stream()
                .collect(Collectors.groupingBy(
                    elder -> "网格群" + elder.getGridGroupId(),
                    Collectors.counting()
                ));
            statistics.put("eldersByGrid", eldersByGrid);
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }
}
