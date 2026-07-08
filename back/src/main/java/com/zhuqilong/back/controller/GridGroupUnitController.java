package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.GridGroupUnit;
import com.zhuqilong.back.service.GridGroupUnitService;
import com.zhuqilong.back.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 临时禁用此控制器
//@RestController
//@RequestMapping("/api/grid-group-units")
@Tag(name = "网格群单元管理", description = "网格群单元相关接口")
@CrossOrigin(origins = "*")
public class GridGroupUnitController {

    @Autowired
    private GridGroupUnitService gridGroupUnitService;

    @GetMapping("/grid-group/{gridGroupId}")
    @Operation(summary = "获取网格群的所有单元")
    public ResponseEntity<List<GridGroupUnit>> getUnitsByGridGroupId(
            @Parameter(description = "网格群ID") @PathVariable Long gridGroupId) {
        List<GridGroupUnit> units = gridGroupUnitService.getUnitsWithDetailsByGridGroupId(gridGroupId);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取单元")
    public ResponseEntity<GridGroupUnit> getUnitById(
            @Parameter(description = "单元ID") @PathVariable Long id) {
        GridGroupUnit unit = gridGroupUnitService.getUnitById(id);
        if (unit != null) {
            return ResponseEntity.ok(unit);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "创建单元")
    public ResponseEntity<ApiResponse<GridGroupUnit>> createUnit(@RequestBody GridGroupUnit unit) {
        try {
            int result = gridGroupUnitService.createUnit(unit);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success("单元创建成功", unit));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error("单元创建失败"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("单元创建失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新单元")
    public ResponseEntity<ApiResponse<GridGroupUnit>> updateUnit(
            @Parameter(description = "单元ID") @PathVariable Long id,
            @RequestBody GridGroupUnit unit) {
        try {
            unit.setId(id);
            int result = gridGroupUnitService.updateUnit(unit);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success("单元更新成功", unit));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("单元更新失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除单元")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(
            @Parameter(description = "单元ID") @PathVariable Long id) {
        try {
            int result = gridGroupUnitService.deleteUnit(id);
            if (result > 0) {
                return ResponseEntity.ok(ApiResponse.success("单元删除成功", null));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("单元删除失败: " + e.getMessage()));
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建单元")
    public ResponseEntity<ApiResponse<List<GridGroupUnit>>> createUnits(
            @RequestBody List<GridGroupUnit> units) {
        try {
            for (GridGroupUnit unit : units) {
                gridGroupUnitService.createUnit(unit);
            }
            return ResponseEntity.ok(ApiResponse.success("批量创建单元成功", units));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量创建单元失败: " + e.getMessage()));
        }
    }
}
