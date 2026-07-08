package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.entity.FaceCheckRecord;
import com.zhuqilong.back.entity.GridGroup;
import com.zhuqilong.back.mapper.ElderMapper;
import com.zhuqilong.back.mapper.FaceCheckRecordMapper;
import com.zhuqilong.back.mapper.GridGroupMapper;
import com.zhuqilong.back.utils.ApiResponse;
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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/face-check")
@Tag(name = "刷脸记录管理", description = "刷脸记录相关接口")
@CrossOrigin(origins = "*")
public class FaceCheckRecordController {

    @Autowired
    private FaceCheckRecordMapper faceCheckRecordMapper;
    
    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private GridGroupMapper gridGroupMapper;

    @PostMapping("/elder/{elderId}")
    @Operation(summary = "记录老人刷脸")
    public ResponseEntity<ApiResponse<FaceCheckRecord>> recordFaceCheck(
            @Parameter(description = "老人ID") @PathVariable Long elderId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String photoData) {
        try {
            FaceCheckRecord record = new FaceCheckRecord();
            record.setElderId(elderId);
            record.setCheckTime(LocalDateTime.now());
            record.setResult(result != null ? result : "success");
            
            // 保存照片数据（如果有）
            if (photoData != null && !photoData.isEmpty()) {
                record.setPhotoData(photoData);
            }
            
            int insertResult = faceCheckRecordMapper.insert(record);
            if (insertResult > 0) {
                // 如果有照片数据，更新photoUrl
                if (photoData != null && !photoData.isEmpty() && record.getId() != null) {
                    record.setPhotoUrl("/api/face-check/photo/" + record.getId());
                    faceCheckRecordMapper.updateById(record);
                }
                return ResponseEntity.ok(ApiResponse.success(record));
            }
            return ResponseEntity.ok(ApiResponse.error("刷脸记录失败"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("刷脸记录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日刷脸记录")
    public ResponseEntity<List<Map<String, Object>>> getTodayFaceChecks() {
        try {
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            
            // 获取今日所有记录
            List<FaceCheckRecord> allRecords = faceCheckRecordMapper.selectAll();
            List<FaceCheckRecord> todayRecords = allRecords.stream()
                .filter(record -> record.getCheckTime().isAfter(todayStart) && record.getCheckTime().isBefore(todayEnd))
                .collect(Collectors.toList());
            
            // 按老人ID分组，每组只保留最后一条记录
            Map<Long, List<FaceCheckRecord>> groupedByElder = todayRecords.stream()
                .collect(Collectors.groupingBy(FaceCheckRecord::getElderId));
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<Long, List<FaceCheckRecord>> entry : groupedByElder.entrySet()) {
                List<FaceCheckRecord> elderRecords = entry.getValue();
                // 按时间排序，取最后一条
                FaceCheckRecord latestRecord = elderRecords.stream()
                    .max(Comparator.comparing(FaceCheckRecord::getCheckTime))
                    .orElse(null);
                
                if (latestRecord != null) {
                    Map<String, Object> item = new HashMap<>();
                    Elder elder = elderMapper.selectElderWithCommunity(latestRecord.getElderId());
                    
                    item.put("id", latestRecord.getId());
                    item.put("checkTime", latestRecord.getCheckTime());
                    item.put("elderId", latestRecord.getElderId());
                    item.put("elderName", elder != null ? elder.getName() : "未知老人");
                    item.put("elderAge", elder != null ? elder.getAge() : null);
                    item.put("elderGender", elder != null ? elder.getGender() : null);
                    item.put("communityName", elder != null && elder.getCommunityName() != null ? elder.getCommunityName() : "未知社区");
                    item.put("result", latestRecord.getResult());
                    item.put("photoUrl", latestRecord.getPhotoUrl());
                    item.put("photoData", latestRecord.getPhotoData());
                    
                    result.add(item);
                }
            }
            
            // 按时间倒序排序
            result.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("checkTime");
                LocalDateTime timeB = (LocalDateTime) b.get("checkTime");
                return timeB.compareTo(timeA);
            });
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/elder/{elderId}/today-status")
    @Operation(summary = "检查老人今日刷脸状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkTodayFaceCheckStatus(
            @Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            // 查询今日刷脸记录
            List<FaceCheckRecord> records = faceCheckRecordMapper.selectByElderId(elderId);
            
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            
            // 查找今日的刷脸记录
            List<FaceCheckRecord> todayRecords = records.stream()
                .filter(record -> record.getCheckTime().isAfter(todayStart) && record.getCheckTime().isBefore(todayEnd))
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("todayChecked", !todayRecords.isEmpty());
            result.put("checkCount", todayRecords.size());
            
            if (!todayRecords.isEmpty()) {
                result.put("lastFaceCheckTime", todayRecords.get(todayRecords.size() - 1).getCheckTime());
                result.put("lastFaceCheckResult", todayRecords.get(todayRecords.size() - 1).getResult());
                result.put("photoUrl", todayRecords.get(todayRecords.size() - 1).getPhotoUrl());
            } else {
                result.put("lastFaceCheckTime", null);
                result.put("lastFaceCheckResult", null);
                result.put("photoUrl", null);
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<Map<String, Object>>error("检查刷脸状态失败: " + e.getMessage()));
        }
    }

    @GetMapping("/abnormal-today")
    @Operation(summary = "获取本月未刷脸老人")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEldersWithoutTodayCheck() {
        try {
            List<Map<String, Object>> abnormalElders = new ArrayList<>();
            
            // 查找本月未刷脸的老人
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            
            // 获取本月所有刷脸记录
            List<FaceCheckRecord> monthFaceChecks = faceCheckRecordMapper.selectAll();
            Set<Long> monthCheckedElderIds = monthFaceChecks.stream()
                .filter(record -> record.getCheckTime() != null && 
                              record.getCheckTime().isAfter(monthStart) && 
                              record.getCheckTime().isBefore(monthEnd))
                .map(FaceCheckRecord::getElderId)
                .collect(Collectors.toSet());
            
            // 获取所有老人信息
            List<Elder> allElders = elderMapper.selectAllEldersWithCommunity();
            for (Elder elder : allElders) {
                Map<String, Object> elderInfo = new HashMap<>();
                elderInfo.put("id", elder.getId());
                elderInfo.put("name", elder.getName());
                elderInfo.put("age", elder.getAge());
                elderInfo.put("gender", elder.getGender());
                elderInfo.put("unitNumber", elder.getUnitNumber());
                elderInfo.put("roomNumber", elder.getRoomNumber());
                elderInfo.put("communityName", elder.getCommunityName());
                elderInfo.put("createdAt", elder.getCreatedAt());
                
                // 查询该老人本月是否有打卡记录
                boolean monthChecked = monthCheckedElderIds.contains(elder.getId());
                elderInfo.put("monthChecked", monthChecked);
                
                abnormalElders.add(elderInfo);
            }
            
            return ResponseEntity.ok(ApiResponse.success(abnormalElders));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取未刷脸老人失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/today-all")
    @Operation(summary = "获取今日完整打卡状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayAllCheckStatus() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取今日时间范围
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            LocalDateTime now = LocalDateTime.now();
            
            // 获取所有老人
            List<Elder> allElders = elderMapper.selectAllEldersWithCommunity();
            
            // 获取今日所有打卡记录
            List<FaceCheckRecord> allRecords = faceCheckRecordMapper.selectAll();
            List<FaceCheckRecord> todayRecords = allRecords.stream()
                .filter(record -> record.getCheckTime().isAfter(todayStart) && record.getCheckTime().isBefore(todayEnd))
                .collect(Collectors.toList());
            
            // 按老人ID分组，取最新记录
            Map<Long, FaceCheckRecord> latestRecordsByElder = new HashMap<>();
            for (FaceCheckRecord record : todayRecords) {
                Long elderId = record.getElderId();
                if (!latestRecordsByElder.containsKey(elderId) || 
                    record.getCheckTime().isAfter(latestRecordsByElder.get(elderId).getCheckTime())) {
                    latestRecordsByElder.put(elderId, record);
                }
            }
            
            // 分类处理：已打卡和未打卡
            List<Map<String, Object>> checkedRecords = new ArrayList<>();
            List<Map<String, Object>> uncheckedRecords = new ArrayList<>();
            
            for (Elder elder : allElders) {
                FaceCheckRecord record = latestRecordsByElder.get(elder.getId());
                
                if (record != null) {
                    // 已打卡老人
                    Map<String, Object> checkedInfo = new HashMap<>();
                    checkedInfo.put("elderId", elder.getId());
                    checkedInfo.put("elderName", elder.getName());
                    checkedInfo.put("elderAge", elder.getAge());
                    checkedInfo.put("elderGender", elder.getGender());
                    checkedInfo.put("communityName", elder.getCommunityName());
                    checkedInfo.put("unitNumber", elder.getUnitNumber());
                    checkedInfo.put("roomNumber", elder.getRoomNumber());
                    checkedInfo.put("checkTime", record.getCheckTime());
                    checkedInfo.put("result", record.getResult());
                    checkedInfo.put("photoUrl", record.getPhotoUrl());
                    checkedInfo.put("status", getCheckStatus(record.getResult()));
                    checkedRecords.add(checkedInfo);
                } else {
                    // 未打卡老人
                    Map<String, Object> uncheckedInfo = new HashMap<>();
                    uncheckedInfo.put("elderId", elder.getId());
                    uncheckedInfo.put("elderName", elder.getName());
                    uncheckedInfo.put("elderAge", elder.getAge());
                    uncheckedInfo.put("elderGender", elder.getGender());
                    uncheckedInfo.put("communityName", elder.getCommunityName());
                    uncheckedInfo.put("unitNumber", elder.getUnitNumber());
                    uncheckedInfo.put("roomNumber", elder.getRoomNumber());
                    
                    // 判断是否异常（当前时间超过24点）
                    boolean isAbnormal = now.isAfter(todayEnd);
                    uncheckedInfo.put("status", isAbnormal ? "异常" : "未打卡");
                    uncheckedInfo.put("isAbnormal", isAbnormal);
                    uncheckedRecords.add(uncheckedInfo);
                }
            }
            
            // 构建返回结果
            result.put("date", todayStart.toLocalDate().toString());
            result.put("currentTime", now.toString());
            result.put("totalElders", allElders.size());
            result.put("checkedElders", checkedRecords.size());
            result.put("uncheckedElders", uncheckedRecords.size());
            result.put("checkedRecords", checkedRecords);
            result.put("uncheckedRecords", uncheckedRecords);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取今日打卡状态失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/month-all")
    @Operation(summary = "获取本月完整打卡状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthAllCheckStatus() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取本月时间范围
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            LocalDateTime now = LocalDateTime.now();
            
            // 获取所有老人
            List<Elder> allElders = elderMapper.selectAllEldersWithCommunity();
            
            // 获取本月所有打卡记录
            List<FaceCheckRecord> allRecords = faceCheckRecordMapper.selectAll();
            List<FaceCheckRecord> monthRecords = allRecords.stream()
                .filter(record -> record.getCheckTime().isAfter(monthStart) && record.getCheckTime().isBefore(monthEnd))
                .collect(Collectors.toList());
            
            // 按老人ID分组，取最新记录
            Map<Long, FaceCheckRecord> latestRecordsByElder = new HashMap<>();
            for (FaceCheckRecord record : monthRecords) {
                Long elderId = record.getElderId();
                if (!latestRecordsByElder.containsKey(elderId) || 
                    record.getCheckTime().isAfter(latestRecordsByElder.get(elderId).getCheckTime())) {
                    latestRecordsByElder.put(elderId, record);
                }
            }
            
            // 分类处理：已打卡和未打卡
            List<Map<String, Object>> checkedRecords = new ArrayList<>();
            List<Map<String, Object>> uncheckedRecords = new ArrayList<>();
            
            for (Elder elder : allElders) {
                FaceCheckRecord record = latestRecordsByElder.get(elder.getId());
                
                if (record != null) {
                    // 已打卡老人
                    Map<String, Object> checkedInfo = new HashMap<>();
                    checkedInfo.put("elderId", elder.getId());
                    checkedInfo.put("elderName", elder.getName());
                    checkedInfo.put("elderAge", elder.getAge());
                    checkedInfo.put("elderGender", elder.getGender());
                    checkedInfo.put("communityName", elder.getCommunityName());
                    checkedInfo.put("unitNumber", elder.getUnitNumber());
                    checkedInfo.put("roomNumber", elder.getRoomNumber());
                    checkedInfo.put("checkTime", record.getCheckTime());
                    checkedInfo.put("result", record.getResult());
                    checkedInfo.put("photoUrl", record.getPhotoUrl());
                    checkedInfo.put("status", getCheckStatus(record.getResult()));
                    checkedRecords.add(checkedInfo);
                } else {
                    // 未打卡老人
                    Map<String, Object> uncheckedInfo = new HashMap<>();
                    uncheckedInfo.put("elderId", elder.getId());
                    uncheckedInfo.put("elderName", elder.getName());
                    uncheckedInfo.put("elderAge", elder.getAge());
                    uncheckedInfo.put("elderGender", elder.getGender());
                    uncheckedInfo.put("communityName", elder.getCommunityName());
                    uncheckedInfo.put("unitNumber", elder.getUnitNumber());
                    uncheckedInfo.put("roomNumber", elder.getRoomNumber());
                    
                    // 判断是否异常（当前时间超过月末最后一天24点）
                    boolean isAbnormal = now.isAfter(monthEnd);
                    uncheckedInfo.put("status", isAbnormal ? "异常" : "未打卡");
                    uncheckedInfo.put("isAbnormal", isAbnormal);
                    uncheckedRecords.add(uncheckedInfo);
                }
            }
            
            // 构建返回结果
            result.put("date", monthStart.toLocalDate().toString());
            result.put("currentTime", now.toString());
            result.put("totalElders", allElders.size());
            result.put("checkedElders", checkedRecords.size());
            result.put("uncheckedElders", uncheckedRecords.size());
            result.put("checkedRecords", checkedRecords);
            result.put("uncheckedRecords", uncheckedRecords);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("获取本月打卡状态失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/elder/{elderId}/photos")
    @Operation(summary = "获取老人所有打卡照片")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getElderPhotos(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            // 查询该老人的所有打卡记录
            List<FaceCheckRecord> photos = faceCheckRecordMapper.selectPhotosByElder(elderId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> photoList = new ArrayList<>();
            for (FaceCheckRecord photo : photos) {
                Map<String, Object> photoInfo = new HashMap<>();
                photoInfo.put("id", photo.getId());
                photoInfo.put("elderId", photo.getElderId());
                photoInfo.put("elderName", photo.getElderName());
                photoInfo.put("checkTime", photo.getCheckTime());
                photoInfo.put("photoUrl", photo.getPhotoUrl());
                photoInfo.put("photoData", photo.getPhotoData());
                photoInfo.put("elderAge", photo.getElderAge());
                photoInfo.put("elderGender", photo.getElderGender());
                photoInfo.put("result", photo.getResult());
                
                photoList.add(photoInfo);
            }
            
            return ResponseEntity.ok(ApiResponse.success(photoList));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<List<Map<String, Object>>>error("获取老人照片失败: " + e.getMessage()));
        }
    }

    @GetMapping("/photo/{recordId}")
    @Operation(summary = "获取刷脸照片")
    public ResponseEntity<byte[]> getFaceCheckPhoto(@PathVariable Long recordId) {
        try {
            FaceCheckRecord record = faceCheckRecordMapper.selectById(recordId);
            if (record != null && record.getPhotoData() != null) {
                // 解码Base64照片数据
                String base64Data = record.getPhotoData();
                if (base64Data.startsWith("data:image/")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                byte[] photoBytes = java.util.Base64.getDecoder().decode(base64Data);
                return ResponseEntity.ok(photoBytes);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new byte[0]);
        }
    }

    @GetMapping("/elder/{elderId}/recheck-status")
    @Operation(summary = "检查重新打卡请求状态")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRecheckStatus(
            @Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            // 查询今日的所有记录
            List<FaceCheckRecord> todayRecords = faceCheckRecordMapper.selectTodayByElderId(elderId);
            
            // 过滤出pending_recheck状态的记录
            List<FaceCheckRecord> recheckRecords = todayRecords.stream()
                .filter(record -> "pending_recheck".equals(record.getResult()))
                .collect(java.util.stream.Collectors.toList());
            
            boolean hasRequest = !recheckRecords.isEmpty();
            Map<String, Object> response = new HashMap<>();
            response.put("hasRequest", hasRequest);
            
            if (hasRequest) {
                FaceCheckRecord latestRecord = recheckRecords.get(0);
                response.put("requestTime", latestRecord.getCheckTime().toString());
                response.put("requestReason", latestRecord.getRecheckReason());
            }
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("检查重新打卡状态失败: " + e.getMessage()));
        }
    }
    
    // 辅助方法：根据打卡结果返回状态描述
    private String getCheckStatus(String result) {
        if ("success".equals(result)) {
            return "正常";
        } else if ("pending_recheck".equals(result)) {
            return "需重检";
        } else if ("failed".equals(result)) {
            return "失败";
        } else {
            return "未知";
        }
    }
    
    // 临时测试接口：添加今日测试打卡数据
    @PostMapping("/add-today-test-data")
    @Operation(summary = "添加今日测试打卡数据")
    public ResponseEntity<String> addTodayTestData() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 为老人ID 3,4,5,1添加今日打卡记录
            Long[] elderIds = {3L, 4L, 5L, 1L};
            String[] results = {"success", "pending_recheck", "success", "success"};
            
            for (int i = 0; i < elderIds.length; i++) {
                FaceCheckRecord record = new FaceCheckRecord();
                record.setElderId(elderIds[i]);
                record.setCheckTime(now.minusHours(8 - i)); // 不同的打卡时间
                record.setResult(results[i]);
                record.setPhotoUrl("/api/face-check/photo/test" + elderIds[i]);
                record.setPhotoData(null);
                
                faceCheckRecordMapper.insert(record);
            }
            
            return ResponseEntity.ok("今日测试数据添加成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("添加测试数据失败: " + e.getMessage());
        }
    }
}
