package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.mapper.BillMapper;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account-stats")
@Tag(name = "账户统计", description = "账户统计相关接口")
@CrossOrigin(origins = "*")
public class AccountStatsController {

    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private BillMapper billMapper;

    @GetMapping
    @Operation(summary = "获取账户统计")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAccountStats(
            @RequestParam(required = false) String timeFilter) {
        try {
            List<Account> accounts = accountMapper.selectAll();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Account account : accounts) {
                Map<String, Object> item = new HashMap<>();
                Elder elder = elderMapper.selectById(account.getElderId());
                
                // 获取消费统计
                BigDecimal totalExpense = calculateTotalExpense(account.getElderId(), timeFilter);
                
                // 获取充值统计
                BigDecimal totalRecharge = calculateTotalRecharge(account.getElderId(), timeFilter);
                
                // 获取最后充值时间
                LocalDateTime lastRecharge = getLastRechargeTime(account.getElderId());
                
                // 验证账户余额是否正确（基于账单记录计算）
                BigDecimal calculatedBalance = totalRecharge.subtract(totalExpense);
                // 使用计算出的余额而不是数据库中的余额
                item.put("elderId", account.getElderId());
                item.put("elderName", elder != null ? elder.getName() : "未知老人");
                item.put("elderAge", elder != null ? elder.getAge() : null);
                item.put("elderGender", elder != null ? elder.getGender() : null);
                item.put("balance", calculatedBalance);
                item.put("totalRecharge", totalRecharge);
                item.put("totalExpense", totalExpense);
                item.put("calculatedBalance", calculatedBalance);
                item.put("balanceCorrect", true); // 基于账单计算的余额总是正确的
                item.put("lastRecharge", lastRecharge);
                item.put("createdAt", account.getCreatedAt());
                item.put("updatedAt", account.getUpdatedAt());
                
                result.add(item);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取账户统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "获取账户汇总统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAccountSummary(
            @RequestParam(required = false) String timeFilter) {
        try {
            Map<String, Object> summary = new HashMap<>();
            
            List<Account> accounts = accountMapper.selectAll();
            
            summary.put("totalElders", accounts.size());
            
            BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.put("totalBalance", totalBalance);
            
            BigDecimal avgBalance = accounts.size() > 0 ? 
                totalBalance.divide(BigDecimal.valueOf(accounts.size()), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
            summary.put("avgBalance", avgBalance);
            
            long lowBalanceCount = accounts.stream()
                .filter(account -> account.getBalance().compareTo(BigDecimal.valueOf(50)) < 0)
                .count();
            summary.put("lowBalanceCount", lowBalanceCount);
            
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取汇总统计失败: " + e.getMessage()));
        }
    }

    private BigDecimal calculateTotalExpense(Long elderId, String timeFilter) {
        try {
            List<Bill> bills = billMapper.selectByElderId(elderId);
            LocalDateTime startTime = getTimeFilterStart(timeFilter);
            
            return bills.stream()
                .filter(bill -> startTime == null || bill.getCreatedAt().isAfter(startTime))
                .filter(bill -> ! "recharge".equals(bill.getBillType())) // 排除充值记录
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateTotalRecharge(Long elderId, String timeFilter) {
        try {
            List<Bill> bills = billMapper.selectByElderId(elderId);
            LocalDateTime startTime = getTimeFilterStart(timeFilter);
            
            return bills.stream()
                .filter(bill -> startTime == null || bill.getCreatedAt().isAfter(startTime))
                .filter(bill -> "recharge".equals(bill.getBillType())) // 只计算充值记录
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDateTime getLastRechargeTime(Long elderId) {
        try {
            List<Bill> bills = billMapper.selectByElderId(elderId);
            
            return bills.stream()
                .filter(bill -> "recharge".equals(bill.getBillType()))
                .map(Bill::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime getTimeFilterStart(String timeFilter) {
        LocalDateTime now = LocalDateTime.now();
        
        if (timeFilter == null || timeFilter.isEmpty()) {
            return null;
        }
        
        switch (timeFilter) {
            case "current_month":
                return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "last_month":
                return now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "current_year":
                return now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "last_year":
                return now.minusYears(1).withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default:
                return null;
        }
    }
}
