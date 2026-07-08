package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.ServiceReview;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.Child;
import com.zhuqilong.back.mapper.ServiceReviewMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.mapper.ChildMapper;
import com.zhuqilong.back.service.ElderService;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
@Tag(name = "服务评价管理", description = "服务评价相关接口")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ServiceReviewMapper serviceReviewMapper;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private ChildMapper childMapper;
    
    @Autowired
    private ElderService elderService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/tasks")
    @Operation(summary = "获取评价任务列表")
    public ResponseEntity<List<Map<String, Object>>> getReviewTasks() {
        try {
            List<ServiceReview> reviews = serviceReviewMapper.selectAll();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (ServiceReview review : reviews) {
                Map<String, Object> item = new HashMap<>();
                Elder elder = elderMapper.selectById(review.getElderId());
                Child child = childMapper.selectById(review.getChildId());
                
                item.put("id", review.getId());
                item.put("reviewMonth", review.getReviewMonth());
                item.put("elderId", review.getElderId());
                item.put("elderName", elder != null ? elder.getName() : "未知老人");
                item.put("childId", review.getChildId());
                item.put("childName", child != null ? child.getName() : "未知子女");
                item.put("type", review.getType());
                item.put("status", review.getStatus());
                item.put("createdAt", review.getCreatedAt());
                item.put("completedAt", review.getCompletedAt());
                
                // 添加分项评分字段
                item.put("attitude", review.getAttitude());
                item.put("response", review.getResponse());
                item.put("quality", review.getQuality());
                item.put("satisfaction", review.getSatisfaction());
                item.put("score", review.getScore());
                item.put("comment", review.getComment());
                
                result.add(item);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "删除所有评价任务")
    public ResponseEntity<ApiResponse<String>> deleteAllReviews() {
        try {
            serviceReviewMapper.deleteAll();
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }

    @GetMapping("/test-contracts")
    @Operation(summary = "测试获取所有签约关系")
    public ResponseEntity<List<Map<String, Object>>> testGetContracts() {
        try {
            // 查询elder_child表
            List<Map<String, Object>> contracts = jdbcTemplate.queryForList(
                "SELECT id, child_id, elder_id, created_at FROM elder_child"
            );
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "获取老人的评价列表")
    public ResponseEntity<List<ServiceReview>> getReviewsByElder(@PathVariable Long elderId) {
        try {
            List<ServiceReview> reviews = serviceReviewMapper.selectByElderId(elderId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/child/{childId}")
    @Operation(summary = "获取子女的评价列表")
    public ResponseEntity<List<ServiceReview>> getReviewsByChild(@PathVariable Long childId) {
        try {
            List<ServiceReview> reviews = serviceReviewMapper.selectByChildId(childId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/test-children/{elderId}")
    @Operation(summary = "测试获取老人关联的子女")
    public ResponseEntity<List<Child>> testGetChildren(@PathVariable Long elderId) {
        try {
            List<Child> children = childMapper.selectByElderId(elderId);
            return ResponseEntity.ok(children);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @PostMapping("/push-elder-tasks")
    @Operation(summary = "推送老人评价任务")
    public ResponseEntity<ApiResponse<String>> pushElderReviewTasks(@RequestBody Map<String, Object> request) {
        try {
            String reviewMonth = (String) request.get("reviewMonth");
            if (reviewMonth == null) {
                reviewMonth = YearMonth.now().toString(); // 默认当前月份
            }
            
            // 获取所有老人
            List<Elder> elders = elderService.getAllElders();
            
            int pushedCount = 0;
            for (Elder elder : elders) {
                // 检查是否已存在该月的老人评价任务
                List<ServiceReview> existingReviews = serviceReviewMapper.selectByElderAndMonth(elder.getId(), reviewMonth);
                
                // 检查是否有老人类型的评价任务
                boolean hasElderReview = existingReviews.stream()
                    .anyMatch(r -> "ELDER".equals(r.getType()));
                
                if (!hasElderReview) {
                    // 创建老人评价任务
                    ServiceReview elderReview = new ServiceReview();
                    elderReview.setElderId(elder.getId());
                    elderReview.setReviewMonth(reviewMonth);
                    elderReview.setType("ELDER");
                    elderReview.setStatus("PUSHED");
                    elderReview.setCreatedAt(LocalDateTime.now());
                    serviceReviewMapper.insert(elderReview);
                    pushedCount++;
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("成功推送 " + pushedCount + " 个老人评价任务"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("推送老人评价任务失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear-month/{reviewMonth}")
    @Operation(summary = "清空指定月份的评价任务")
    public ResponseEntity<ApiResponse<String>> clearMonthReviews(@PathVariable String reviewMonth) {
        try {
            // 删除指定月份的所有评价任务
            List<ServiceReview> reviews = serviceReviewMapper.selectByMonth(reviewMonth);
            int deletedCount = 0;
            
            for (ServiceReview review : reviews) {
                int result = serviceReviewMapper.deleteById(review.getId());
                if (result > 0) {
                    deletedCount++;
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("成功清空 " + deletedCount + " 个" + reviewMonth + " 月的评价任务"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("清空评价任务失败: " + e.getMessage()));
        }
    }

    @PostMapping("/push-child-tasks")
    @Operation(summary = "推送子女评价任务")
    public ResponseEntity<ApiResponse<String>> pushChildReviewTasks(@RequestBody Map<String, Object> request) {
        try {
            String reviewMonth = (String) request.get("reviewMonth");
            if (reviewMonth == null) {
                reviewMonth = YearMonth.now().toString(); // 默认当前月份
            }
            
            // 获取所有有签约关系的老人
            List<Elder> elders = elderService.getAllElders();
            
            int pushedCount = 0;
            for (Elder elder : elders) {
                // 检查是否已存在该月的子女评价任务
                List<ServiceReview> existingReviews = serviceReviewMapper.selectByElderAndMonth(elder.getId(), reviewMonth);
                
                // 检查是否有子女类型的评价任务
                boolean hasChildReview = existingReviews.stream()
                    .anyMatch(r -> "CHILD".equals(r.getType()));
                
                if (!hasChildReview) {
                    // 获取该老人的所有子女
                    List<Child> children = childMapper.selectByElderId(elder.getId());
                    
                    for (Child child : children) {
                        // 创建子女评价任务
                        ServiceReview childReview = new ServiceReview();
                        childReview.setElderId(elder.getId());
                        childReview.setChildId(child.getId());
                        childReview.setReviewMonth(reviewMonth);
                        childReview.setType("CHILD");
                        childReview.setStatus("PUSHED");
                        childReview.setCreatedAt(LocalDateTime.now());
                        serviceReviewMapper.insert(childReview);
                        pushedCount++;
                    }
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("成功推送 " + pushedCount + " 个子女评价任务"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("推送子女评价任务失败: " + e.getMessage()));
        }
    }

    @PostMapping("/push-tasks")
    @Operation(summary = "推送月度评价任务")
    public ResponseEntity<ApiResponse<String>> pushMonthlyReviewTasks(@RequestBody Map<String, Object> request) {
        try {
            String reviewMonth = (String) request.get("reviewMonth");
            if (reviewMonth == null) {
                reviewMonth = YearMonth.now().toString(); // 默认当前月份
            }
            
            // 获取所有有签约关系的老人
            List<Elder> elders = elderService.getAllElders();
            
            int pushedCount = 0;
            for (Elder elder : elders) {
                // 检查是否已存在该月的评价任务
                List<ServiceReview> existingReviews = serviceReviewMapper.selectByElderAndMonth(elder.getId(), reviewMonth);
                
                // 检查是否有未完成的PUSHED任务
                boolean hasUnfinishedTask = existingReviews.stream()
                    .anyMatch(review -> "PUSHED".equals(review.getStatus()));
                
                // 只有在没有未完成任务时才创建新任务
                if (existingReviews.isEmpty() || !hasUnfinishedTask) {
                    // 创建老人评价任务
                    ServiceReview elderReview = new ServiceReview();
                    elderReview.setElderId(elder.getId());
                    elderReview.setReviewMonth(reviewMonth);
                    elderReview.setType("ELDER");
                    elderReview.setStatus("PUSHED");
                    elderReview.setCreatedAt(LocalDateTime.now());
                    serviceReviewMapper.insert(elderReview);
                    pushedCount++;
                    System.out.println("Created elder review task for elder ID: " + elder.getId());
                }
                
                // 创建子女评价任务（如果有签约关系）
                List<Child> children = childMapper.selectByElderId(elder.getId());
                for (Child child : children) {
                    // 检查是否已存在该月的子女评价任务
                    List<ServiceReview> existingChildReviews = serviceReviewMapper.selectByChildAndMonth(child.getId(), reviewMonth);
                    
                    // 只有在没有未完成子女任务时才创建新任务
                    if (existingChildReviews.isEmpty()) {
                        ServiceReview childReview = new ServiceReview();
                        childReview.setElderId(elder.getId());
                        childReview.setChildId(child.getId());
                        childReview.setReviewMonth(reviewMonth);
                        childReview.setType("CHILD");
                        childReview.setStatus("PUSHED");
                        childReview.setCreatedAt(LocalDateTime.now());
                        serviceReviewMapper.insert(childReview);
                        pushedCount++;
                        System.out.println("Created child review task for child ID: " + child.getId());
                    }
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("成功推送 " + pushedCount + " 个评价任务"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("推送评价任务失败: " + e.getMessage()));
        }
    }

    // 完成评价任务
    @PostMapping("/complete-review")
    @Operation(summary = "完成评价任务")
    public ResponseEntity<ApiResponse<String>> completeReviewTask(@RequestBody Map<String, Object> request) {
        try {
            Long reviewId = Long.valueOf(request.get("reviewId").toString());
            String reviewType = (String) request.get("reviewType"); // ELDER 或 CHILD
            
            // 查询任务
            ServiceReview review = serviceReviewMapper.selectById(reviewId);
            if (review == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("评价任务不存在"));
            }
            
            // 更新任务状态为已完成
            review.setStatus("COMPLETED");
            review.setCompletedAt(LocalDateTime.now());
            serviceReviewMapper.updateById(review);
            
            String taskType = "ELDER".equals(reviewType) ? "老人" : "子女";
            return ResponseEntity.ok(ApiResponse.success(taskType + "评价任务已完成"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("完成评价任务失败: " + e.getMessage()));
        }
    }

    @PostMapping("/submit")
    @Operation(summary = "提交评价")
    public ResponseEntity<ApiResponse<String>> submitReview(@RequestBody Map<String, Object> request) {
        try {
            Long reviewId = Long.valueOf(request.get("reviewId").toString());
            
            // 获取分数数组
            @SuppressWarnings("unchecked")
            List<Integer> scores = (List<Integer>) request.get("scores");
            
            // 获取平均分
            Double averageScore = Double.valueOf(request.get("averageScore").toString());
            
            ServiceReview review = serviceReviewMapper.selectById(reviewId);
            if (review != null) {
                review.setStatus("COMPLETED");
                review.setScore(averageScore.intValue());
                review.setCompletedAt(LocalDateTime.now());
                
                // 保存分项评分
                if (scores != null && scores.size() >= 5) {
                    review.setAttitude(scores.get(0));
                    review.setResponse(scores.get(1));
                    review.setQuality(scores.get(2));
                    review.setSatisfaction(scores.get(3));
                    // 第5个分数可以作为总体满意度的补充
                }
                
                // 生成评价意见
                StringBuilder comment = new StringBuilder();
                comment.append("服务态度: ").append(scores.get(0)).append("分\n");
                comment.append("响应速度: ").append(scores.get(1)).append("分\n");
                comment.append("服务质量: ").append(scores.get(2)).append("分\n");
                comment.append("问题解决能力: ").append(scores.get(3)).append("分\n");
                comment.append("整体满意度: ").append(scores.get(4)).append("分");
                review.setComment(comment.toString());
                
                serviceReviewMapper.updateById(review);
                return ResponseEntity.ok(ApiResponse.success("评价提交成功"));
            }
            return ResponseEntity.status(404).body(ApiResponse.error("评价任务不存在"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("提交评价失败: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics/{reviewMonth}")
    @Operation(summary = "获取月度评价统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyStatistics(@PathVariable String reviewMonth) {
        try {
            List<ServiceReview> reviews = serviceReviewMapper.selectByMonth(reviewMonth);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReviews", reviews.size());
            stats.put("completedReviews", reviews.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count());
            stats.put("averageScore", reviews.stream()
                .filter(r -> r.getScore() != null)
                .mapToInt(ServiceReview::getScore)
                .average()
                .orElse(0.0));
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取统计数据失败: " + e.getMessage()));
        }
    }
}
