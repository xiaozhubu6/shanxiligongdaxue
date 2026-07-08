package com.zhuqilong.back.controller;

import com.zhuqilong.back.dto.RechargeRequest;
import com.zhuqilong.back.dto.ReviewRequest;
import com.zhuqilong.back.entity.Bill;
import com.zhuqilong.back.entity.Child;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.ServiceReview;
import com.zhuqilong.back.service.ChildService;
import com.zhuqilong.back.mapper.ServiceReviewMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/child")
@Tag(name = "子女端管理", description = "子女端相关接口")
@CrossOrigin(origins = "*")
public class ChildController {

    @Autowired
    private ChildService childService;
    
    @Autowired
    private ServiceReviewMapper serviceReviewMapper;

    // 管理员需要的CRUD接口
    @GetMapping
    @Operation(summary = "获取所有子女")
    public ResponseEntity<List<Child>> getAllChildren() {
        List<Child> children = childService.getAllChildren();
        return ResponseEntity.ok(children);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取子女")
    public ResponseEntity<Child> getChildById(@Parameter(description = "子女ID") @PathVariable Long id) {
        Child child = childService.getChildById(id);
        if (child != null) {
            return ResponseEntity.ok(child);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "创建子女")
    public ResponseEntity<Child> createChild(@RequestBody Child child) {
        child.setCreatedAt(LocalDateTime.now());
        int result = childService.createChild(child);
        if (result > 0) {
            return ResponseEntity.ok(child);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新子女信息")
    public ResponseEntity<Child> updateChild(
            @Parameter(description = "子女ID") @PathVariable Long id,
            @RequestBody Child child) {
        child.setId(id);
        int result = childService.updateChild(child);
        if (result > 0) {
            return ResponseEntity.ok(child);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除子女")
    public ResponseEntity<Void> deleteChild(@Parameter(description = "子女ID") @PathVariable Long id) {
        int result = childService.deleteChild(id);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 子女端功能接口
    @PostMapping("/login")
    @Operation(summary = "子女登录")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String phone = loginData.get("phone");
        String name = loginData.get("name");
        
        Map<String, Object> result = childService.login(phone, name);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/recharge")
    @Operation(summary = "充值")
    public ResponseEntity<Map<String, Object>> recharge(@RequestBody RechargeRequest request) {
        Map<String, Object> result = childService.recharge(request.getElderId(), request.getAmount());
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/bills")
    @Operation(summary = "获取账单")
    public ResponseEntity<List<Bill>> getBills(@RequestParam Long elderId) {
        List<Bill> bills = childService.getBills(elderId);
        return ResponseEntity.ok(bills);
    }

    @PostMapping("/review")
    @Operation(summary = "提交评价")
    public ResponseEntity<Map<String, Object>> review(@RequestBody ReviewRequest request) {
        Map<String, Object> result = childService.createReview(request);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/elders")
    @Operation(summary = "获取关联老人")
    public ResponseEntity<List<Elder>> getAssociatedElders(@RequestParam Long childId) {
        List<Elder> elders = childService.getAssociatedElders(childId);
        return ResponseEntity.ok(elders);
    }

    @GetMapping("/elder-status")
    @Operation(summary = "获取老人状态")
    public ResponseEntity<Map<String, Object>> getElderStatus(@RequestParam Long elderId) {
        Map<String, Object> status = childService.getElderStatus(elderId);
        if (status != null) {
            return ResponseEntity.ok(status);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/reviews/{childId}")
    @Operation(summary = "获取子女评价列表")
    public ResponseEntity<List<ServiceReview>> getReviewsByChild(@PathVariable Long childId) {
        try {
            List<ServiceReview> reviews = serviceReviewMapper.selectByChildId(childId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/reviews/{childId}/month/{reviewMonth}")
    @Operation(summary = "获取子女月度评价列表")
    public ResponseEntity<List<ServiceReview>> getReviewsByChildAndMonth(@PathVariable Long childId, @PathVariable String reviewMonth) {
        try {
            List<ServiceReview> reviews = serviceReviewMapper.selectByChildAndMonth(childId, reviewMonth);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
