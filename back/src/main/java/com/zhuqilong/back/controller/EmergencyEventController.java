package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.EmergencyEvent;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.FaceCheckRecord;
import com.zhuqilong.back.mapper.EmergencyEventMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.mapper.FaceCheckRecordMapper;
import com.zhuqilong.back.utils.ApiResponse;
import com.zhuqilong.back.dto.RecheckRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emergency")
@Tag(name = "紧急事件管理", description = "紧急事件相关接口")
@CrossOrigin(origins = "*")
public class EmergencyEventController {

    @Autowired
    private EmergencyEventMapper emergencyEventMapper;
    
    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private FaceCheckRecordMapper faceCheckRecordMapper;

    @PostMapping
    @Operation(summary = "创建紧急事件")
    public ResponseEntity<ApiResponse<EmergencyEvent>> createEvent(@RequestBody EmergencyEvent event) {
        try {
            event.setCreatedAt(LocalDateTime.now());
            event.setStatus("pending");
            int result = emergencyEventMapper.insert(event);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success(event));
            }
            return ResponseEntity.ok(ApiResponse.error("创建紧急事件失败"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("创建紧急事件失败: " + e.getMessage()));
        }
    }

    @GetMapping("/unhandled")
    @Operation(summary = "获取未处理的紧急事件")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUnhandledEvents() {
        try {
            List<EmergencyEvent> events = emergencyEventMapper.selectByStatus("pending");
            
            // 转换为包含老人信息的完整数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (EmergencyEvent event : events) {
                Map<String, Object> item = new HashMap<>();
                Elder elder = elderMapper.selectById(event.getElderId());
                
                item.put("id", event.getId());
                item.put("eventType", event.getEventType());
                item.put("status", event.getStatus());
                item.put("createdAt", event.getCreatedAt());
                item.put("handledAt", event.getHandledAt());
                item.put("description", event.getDescription());
                item.put("elderId", event.getElderId());
                item.put("elderName", elder != null ? elder.getName() : "未知老人");
                item.put("elderAge", elder != null ? elder.getAge() : null);
                item.put("elderGender", elder != null ? elder.getGender() : null);
                
                result.add(item);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取未处理事件失败: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有紧急事件")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllEvents() {
        try {
            List<EmergencyEvent> events = emergencyEventMapper.selectAll();
            
            // 转换为包含老人信息的完整数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (EmergencyEvent event : events) {
                Map<String, Object> item = new HashMap<>();
                Elder elder = elderMapper.selectById(event.getElderId());
                
                item.put("id", event.getId());
                item.put("eventType", event.getEventType());
                item.put("status", event.getStatus());
                item.put("createdAt", event.getCreatedAt());
                item.put("handledAt", event.getHandledAt());
                item.put("description", event.getDescription());
                item.put("elderId", event.getElderId());
                item.put("elderName", elder != null ? elder.getName() : "未知老人");
                item.put("elderAge", elder != null ? elder.getAge() : null);
                item.put("elderGender", elder != null ? elder.getGender() : null);
                
                result.add(item);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取所有事件失败: " + e.getMessage()));
        }
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "获取老人的紧急事件")
    public ResponseEntity<List<EmergencyEvent>> getEventsByElder(
            @Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            List<EmergencyEvent> events = emergencyEventMapper.selectByElderId(elderId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取紧急事件统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 未处理事件数量
            List<EmergencyEvent> pendingEvents = emergencyEventMapper.selectByStatus("pending");
            statistics.put("unhandled", pendingEvents.size());
            
            // 本月事件数量
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            
            List<EmergencyEvent> allEvents = emergencyEventMapper.selectAll();
            long monthCount = allEvents.stream()
                .filter(event -> event.getCreatedAt() != null && 
                              event.getCreatedAt().isAfter(monthStart) && 
                              event.getCreatedAt().isBefore(monthEnd))
                .count();
            statistics.put("month", monthCount);
            
            // 本周事件数量
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            long weekCount = allEvents.stream()
                .filter(event -> event.getCreatedAt() != null && 
                              event.getCreatedAt().isAfter(weekStart))
                .count();
            statistics.put("week", weekCount);
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{eventId}/handle")
    @Operation(summary = "处理紧急事件")
    public ResponseEntity<ApiResponse<String>> handleEvent(@Parameter(description = "事件ID") @PathVariable Long eventId) {
        try {
            EmergencyEvent event = emergencyEventMapper.selectById(eventId);
            if (event != null) {
                event.setStatus("handled");
                event.setHandledAt(LocalDateTime.now());
                int result = emergencyEventMapper.updateById(event);
                if (result > 0) {
                    return ResponseEntity.ok(ApiResponse.success("事件处理成功"));
                } else {
                    return ResponseEntity.status(500).body(ApiResponse.error("更新失败"));
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("处理事件失败: " + e.getMessage()));
        }
    }

    @PostMapping("/handle-face-check-exception")
    @Operation(summary = "处理未刷脸异常")
    public ResponseEntity<ApiResponse<String>> handleFaceCheckException(@RequestBody Map<String, Object> request) {
        try {
            Long elderId = Long.valueOf(request.get("elderId").toString());
            
            // 查询今日是否有该老人的未刷脸异常记录
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            
            List<EmergencyEvent> todayEvents = emergencyEventMapper.selectAll();
            List<EmergencyEvent> faceCheckExceptions = todayEvents.stream()
                .filter(event -> event.getElderId().equals(elderId) 
                    && "FACE_CHECK_EXCEPTION".equals(event.getEventType())
                    && event.getCreatedAt().isAfter(todayStart) 
                    && event.getCreatedAt().isBefore(todayEnd))
                .collect(Collectors.toList());
            
            if (!faceCheckExceptions.isEmpty()) {
                // 处理最新的未刷脸异常记录
                EmergencyEvent latestException = faceCheckExceptions.stream()
                    .max(Comparator.comparing(EmergencyEvent::getCreatedAt))
                    .orElse(null);
                
                if (latestException != null) {
                    latestException.setStatus("handled");
                    latestException.setHandledAt(LocalDateTime.now());
                    emergencyEventMapper.updateById(latestException);
                    
                    return ResponseEntity.ok(ApiResponse.success("未刷脸异常已处理"));
                }
            }
            
            return ResponseEntity.ok(ApiResponse.error("未找到未刷脸异常记录"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("处理未刷脸异常失败: " + e.getMessage()));
        }
    }

    @PostMapping("/recheck")
    @Operation(summary = "创建重新打卡请求")
    public ResponseEntity<ApiResponse<String>> createRecheckRequest(@RequestBody RecheckRequest request) {
        try {
            System.out.println("收到重新打卡请求 - elderId: " + request.getElderId());
            System.out.println("管理员协助: " + request.getAdminAssisted());
            
            // 查询今日是否已有打卡记录
            List<FaceCheckRecord> todayRecords = faceCheckRecordMapper.selectTodayByElderId(request.getElderId());
            
            FaceCheckRecord record;
            if (!todayRecords.isEmpty()) {
                // 覆盖今日最新的记录
                record = todayRecords.get(0);
                System.out.println("覆盖今日打卡记录，ID: " + record.getId());
            } else {
                // 创建新记录
                record = new FaceCheckRecord();
                record.setElderId(request.getElderId());
                System.out.println("创建新的重新打卡记录");
            }
            
            // 更新记录为重新打卡状态
            record.setCheckTime(LocalDateTime.now());
            record.setResult("pending_recheck"); // 待重新打卡状态
            record.setAdminAssisted(true); // 标记为管理员协助
            record.setRecheckReason(request.getRecheckReason());
            record.setDescription("管理员请求老人重新打卡");
            
            int updateResult;
            if (record.getId() != null) {
                // 更新现有记录
                updateResult = faceCheckRecordMapper.updateById(record);
                System.out.println("更新重新打卡记录结果: " + updateResult);
            } else {
                // 插入新记录
                updateResult = faceCheckRecordMapper.insert(record);
                System.out.println("插入重新打卡记录结果: " + updateResult);
            }
            
            if (updateResult > 0) {
                System.out.println("重新打卡请求处理成功，ID: " + record.getId());
                
                // 这里可以添加通知逻辑，比如发送短信、推送通知等
                // notifyElderForRecheck(request.getElderId());
                
                return ResponseEntity.ok(ApiResponse.success("重新打卡请求已发送"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("重新打卡请求失败"));
            }
        } catch (Exception e) {
            System.err.println("创建重新打卡请求失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("重新打卡请求失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/checkin-notify")
    @Operation(summary = "发送打卡提醒通知")
    public ResponseEntity<ApiResponse<EmergencyEvent>> sendCheckInNotify(@RequestBody Map<String, Object> request) {
        try {
            // 创建紧急事件记录，作为打卡提醒
            EmergencyEvent event = new EmergencyEvent();
            event.setElderId(Long.valueOf(request.get("elderId").toString()));
            event.setEventType("CHECKIN_NOTIFY");
            event.setDescription(request.get("description") != null ? request.get("description").toString() : "请进行今日打卡");
            event.setCreatedAt(LocalDateTime.now());
            event.setStatus("pending"); // 待处理状态
            
            int result = emergencyEventMapper.insert(event);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success(event));
            }
            return ResponseEntity.ok(ApiResponse.error("发送打卡提醒失败"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("发送打卡提醒失败: " + e.getMessage()));
        }
    }
}
