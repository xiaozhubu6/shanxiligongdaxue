package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.dto.ElderWithChildInfo;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.service.AccountService;
import com.zhuqilong.back.service.ElderService;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/elders")
@Tag(name = "老人管理", description = "老人信息相关接口")
@CrossOrigin(origins = "*")
public class ElderController {

    @Autowired
    private ElderService elderService;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private AccountService accountService;

    @GetMapping
    @Operation(summary = "获取所有老人")
    public ResponseEntity<List<Elder>> getAllElders() {
        List<Elder> elders = elderService.getAllElders();
        return ResponseEntity.ok(elders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取老人")
    public ResponseEntity<Elder> getElderById(@Parameter(description = "老人ID") @PathVariable Long id) {
        Elder elder = elderMapper.selectElderWithCommunity(id);
        if (elder != null) {
            return ResponseEntity.ok(elder);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/grid-group/{gridGroupId}")
    @Operation(summary = "根据网格组ID获取老人")
    public ResponseEntity<List<Elder>> getEldersByGridGroupId(@Parameter(description = "网格组ID") @PathVariable Long gridGroupId) {
        List<Elder> elders = elderService.getEldersByGridGroupId(gridGroupId);
        return ResponseEntity.ok(elders);
    }

    @PostMapping
    @Operation(summary = "创建老人")
    public ResponseEntity<Elder> createElder(@RequestBody Elder elder) {
        try {
            elder.setCreatedAt(LocalDateTime.now());
            
            int result = elderService.createElder(elder);
            if (result > 0) {
                // 创建老人成功后，自动创建对应的账户记录
                Account account = new Account();
                account.setElderId(elder.getId());
                account.setBalance(BigDecimal.ZERO);
                account.setCreatedAt(LocalDateTime.now());
                account.setUpdatedAt(LocalDateTime.now());
                accountService.createAccount(account);
                
                return ResponseEntity.ok(elder);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新老人信息")
    public ResponseEntity<Elder> updateElder(
            @Parameter(description = "老人ID") @PathVariable Long id,
            @RequestBody Elder elder) {
        elder.setId(id);
        int result = elderService.updateElder(elder);
        if (result > 0) {
            return ResponseEntity.ok(elder);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/abnormal")
    @Operation(summary = "获取异常老人列表")
    public ResponseEntity<List<Elder>> getAbnormalElders() {
        try {
            List<Elder> elders = elderService.getAbnormalElders();
            return ResponseEntity.ok(elders);
        } catch (Exception e) {
            // 如果查询失败，返回空列表
            return ResponseEntity.ok(List.of());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除老人")
    public ResponseEntity<Void> deleteElder(@Parameter(description = "老人ID") @PathVariable Long id) {
        try {
            // 先删除账户记录
            accountMapper.deleteByElderId(id);
            
            // 再删除相关的绑定关系
            elderMapper.deleteElderChildByElderId(id);
            
            // 最后删除老人
            int result = elderService.deleteElder(id);
            if (result > 0) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 记录错误日志
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/child/{childId}")
    @Operation(summary = "获取子女关联的老人列表")
    public ResponseEntity<List<Elder>> getAssociatedElders(@PathVariable Long childId) {
        try {
            List<Elder> elders = elderService.getEldersByChildId(childId);
            return ResponseEntity.ok(elders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    @GetMapping("/grid-group-unit/{gridGroupUnitId}")
    @Operation(summary = "根据单元获取老人列表")
    public ResponseEntity<List<Elder>> getEldersByGridGroupUnit(@PathVariable Long gridGroupUnitId) {
        try {
            List<Elder> elders = elderService.getEldersByGridGroupUnitId(gridGroupUnitId);
            return ResponseEntity.ok(elders);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    // 获取包含子女信息的老人列表
    @GetMapping("/with-children")
    @Operation(summary = "获取包含子女信息的老人列表")
    public ResponseEntity<ApiResponse<List<ElderWithChildInfo>>> getEldersWithChildren() {
        try {
            List<ElderWithChildInfo> elders = elderMapper.selectAllEldersWithChildInfo();
            return ResponseEntity.ok(ApiResponse.success(elders));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.<List<ElderWithChildInfo>>error("获取老人列表失败: " + e.getMessage()));
        }
    }
}
