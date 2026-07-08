package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.mapper.BillMapper;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "账单管理", description = "账单相关接口")
@CrossOrigin(origins = "*")
public class BillController {

    @Autowired
    private BillMapper billMapper;

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "获取老人的账单")
    public ResponseEntity<List<Bill>> getBillsByElder(
            @PathVariable Long elderId) {
        try {
            List<Bill> bills = billMapper.selectByElderId(elderId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/filter")
    @Operation(summary = "筛选账单")
    public ResponseEntity<ApiResponse<List<Bill>>> getBillsByFilter(
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate,
            @RequestParam(required = false) String billType) {
        try {
            List<Bill> allBills = billMapper.selectAll();
            List<Bill> filteredBills = new ArrayList<>();
            
            for (Bill bill : allBills) {
                boolean match = true;
                
                // 按老人ID筛选
                if (elderId != null && !bill.getElderId().equals(elderId)) {
                    match = false;
                }
                
                // 按开始日期筛选
                if (startDate != null && bill.getCreatedAt().toLocalDate().isBefore(
                    java.time.LocalDate.parse(startDate))) {
                    match = false;
                }
                
                // 按结束日期筛选
                if (endDate != null && bill.getCreatedAt().toLocalDate().isAfter(
                    java.time.LocalDate.parse(endDate))) {
                    match = false;
                }
                
                // 按账单类型筛选
                if (billType != null && !billType.isEmpty() && 
                    !billType.equals(bill.getBillType()) && 
                    !("recharge".equals(billType) && "充值".equals(bill.getBillType()))) {
                    match = false;
                }
                
                if (match) {
                    filteredBills.add(bill);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(filteredBills));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("筛选账单失败: " + e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "创建账单")
    public ResponseEntity<ApiResponse<Bill>> createBill(@RequestBody Bill bill) {
        try {
            bill.setCreatedAt(LocalDateTime.now());
            int result = billMapper.insert(bill);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success(bill));
            }
            return ResponseEntity.ok(ApiResponse.error("创建账单失败"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("创建账单失败: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics/{elderId}")
    @Operation(summary = "获取老人账单统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBillStatistics(@PathVariable Long elderId) {
        try {
            List<Bill> bills = billMapper.selectByElderId(elderId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            double totalIncome = bills.stream()
                .filter(bill -> "recharge".equals(bill.getBillType()))
                .mapToDouble(bill -> bill.getAmount().doubleValue())
                .sum();
            
            double totalExpense = bills.stream()
                .filter(bill -> !"recharge".equals(bill.getBillType()))
                .mapToDouble(bill -> bill.getAmount().doubleValue())
                .sum();
            
            statistics.put("totalIncome", totalIncome);
            statistics.put("totalExpense", totalExpense);
            statistics.put("netAmount", totalIncome - totalExpense);
            statistics.put("totalCount", bills.size());
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }
}
