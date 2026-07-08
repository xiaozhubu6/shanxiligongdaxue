package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.service.AccountService;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.mapper.BillMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.entity.Elder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/account")
@Tag(name = "账户管理", description = "账户相关接口")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;
    
    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private BillMapper billMapper;
    
    @Autowired
    private ElderMapper elderMapper;

    @GetMapping
    @Operation(summary = "获取所有账户")
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取账户")
    public ResponseEntity<Account> getAccountById(@Parameter(description = "账户ID") @PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        if (account != null) {
            return ResponseEntity.ok(account);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "创建账户")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        int result = accountService.createAccount(account);
        if (result > 0) {
            return ResponseEntity.ok(account);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新账户")
    public ResponseEntity<Account> updateAccount(
            @Parameter(description = "账户ID") @PathVariable Long id,
            @RequestBody Account account) {
        account.setId(id);
        account.setUpdatedAt(LocalDateTime.now());
        int result = accountService.updateAccount(account);
        if (result > 0) {
            return ResponseEntity.ok(account);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除账户")
    public ResponseEntity<Void> deleteAccount(@Parameter(description = "账户ID") @PathVariable Long id) {
        int result = accountService.deleteAccount(id);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 老人账户相关接口
    @GetMapping("/elder/{elderId}")
    @Operation(summary = "获取老人账户")
    public ResponseEntity<Account> getElderAccount(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            // 查找老人对应的账户
            Account account = accountMapper.selectByElderId(elderId);
            if (account != null) {
                return ResponseEntity.ok(account);
            }
            // 如果没有账户，创建一个默认账户
            Elder elder = elderMapper.selectById(elderId);
            if (elder != null) {
                Account newAccount = new Account();
                newAccount.setElderId(elderId);
                newAccount.setBalance(BigDecimal.ZERO);
                newAccount.setCreatedAt(LocalDateTime.now());
                newAccount.setUpdatedAt(LocalDateTime.now());
                accountMapper.insert(newAccount);
                return ResponseEntity.ok(newAccount);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.ok(new Account()); // 返回空账户避免前端错误
        }
    }

    @PostMapping("/elder/{elderId}/recharge")
    @Operation(summary = "老人账户充值")
    public ResponseEntity<Account> rechargeElderAccount(
            @Parameter(description = "老人ID") @PathVariable Long elderId,
            @RequestParam Double amount) {
        try {
            if (amount <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            Account account = accountMapper.selectByElderId(elderId);
            if (account != null) {
                // 更新余额
                BigDecimal currentBalance = account.getBalance();
                BigDecimal rechargeAmount = BigDecimal.valueOf(amount);
                account.setBalance(currentBalance.add(rechargeAmount));
                account.setUpdatedAt(LocalDateTime.now());
                accountMapper.updateById(account);
                
                // 创建充值账单
                Bill bill = new Bill();
                bill.setElderId(elderId);
                bill.setBillType("recharge");
                bill.setAmount(rechargeAmount);
                bill.setDescription("账户充值");
                bill.setCreatedAt(LocalDateTime.now());
                billMapper.insert(bill);
                
                return ResponseEntity.ok(account);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/elder/{elderId}/bills")
    @Operation(summary = "获取老人账单")
    public ResponseEntity<List<Bill>> getElderBills(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            List<Bill> bills = billMapper.selectByElderId(elderId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/elder/{elderId}/balance")
    @Operation(summary = "获取老人余额")
    public ResponseEntity<BigDecimal> getElderBalance(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            Account account = accountMapper.selectByElderId(elderId);
            if (account != null) {
                return ResponseEntity.ok(account.getBalance());
            }
            return ResponseEntity.ok(BigDecimal.ZERO);
        } catch (Exception e) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
    }

    @PostMapping("/elder/{elderId}/recalculate-balance")
    @Operation(summary = "重新计算老人余额")
    public ResponseEntity<String> recalculateElderBalance(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            // 获取所有账单
            List<Bill> bills = billMapper.selectByElderId(elderId);
            
            // 计算总充值和总消费
            BigDecimal totalRecharge = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;
            
            for (Bill bill : bills) {
                if ("recharge".equals(bill.getBillType()) || "RECHARGE".equals(bill.getBillType())) {
                    totalRecharge = totalRecharge.add(bill.getAmount());
                } else {
                    totalExpense = totalExpense.add(bill.getAmount());
                }
            }
            
            // 计算正确余额
            BigDecimal correctBalance = totalRecharge.subtract(totalExpense);
            
            // 更新数据库
            Account account = accountMapper.selectByElderId(elderId);
            if (account != null) {
                account.setBalance(correctBalance);
                account.setUpdatedAt(LocalDateTime.now());
                accountMapper.updateById(account);
                
                return ResponseEntity.ok("余额已重新计算并更新: " + correctBalance);
            } else {
                return ResponseEntity.status(404).body("未找到老人账户");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("重新计算余额失败: " + e.getMessage());
        }
    }
}
