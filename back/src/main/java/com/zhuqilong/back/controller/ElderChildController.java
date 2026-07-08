package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.ElderChild;
import com.zhuqilong.back.mapper.ElderChildMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/elder-child")
@Tag(name = "老人子女关系管理", description = "老人和子女绑定关系相关接口")
@CrossOrigin(origins = "*")
public class ElderChildController {

    @Autowired
    private ElderChildMapper elderChildMapper;

    @GetMapping
    @Operation(summary = "获取所有老人子女关系")
    public ResponseEntity<List<ElderChild>> getAllElderChildren() {
        try {
            List<ElderChild> elderChildren = elderChildMapper.selectAll();
            return ResponseEntity.ok(elderChildren);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取老人子女关系")
    public ResponseEntity<ElderChild> getElderChildById(@Parameter(description = "关系ID") @PathVariable Long id) {
        try {
            ElderChild elderChild = elderChildMapper.selectById(id);
            if (elderChild != null) {
                return ResponseEntity.ok(elderChild);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "获取老人的所有子女关系")
    public ResponseEntity<List<ElderChild>> getChildrenByElder(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            List<ElderChild> elderChildren = elderChildMapper.selectByElderId(elderId);
            return ResponseEntity.ok(elderChildren);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/child/{childId}")
    @Operation(summary = "获取子女的所有老人关系")
    public ResponseEntity<List<ElderChild>> getEldersByChild(@Parameter(description = "子女ID") @PathVariable Long childId) {
        try {
            List<ElderChild> elderChildren = elderChildMapper.selectByChildId(childId);
            return ResponseEntity.ok(elderChildren);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping
    @Operation(summary = "创建老人子女关系")
    public ResponseEntity<ElderChild> createElderChild(@RequestBody ElderChild elderChild) {
        try {
            elderChild.setCreatedAt(LocalDateTime.now());
            int result = elderChildMapper.insert(elderChild);
            if (result > 0) {
                return ResponseEntity.ok(elderChild);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新老人子女关系")
    public ResponseEntity<ElderChild> updateElderChild(
            @Parameter(description = "关系ID") @PathVariable Long id,
            @RequestBody ElderChild elderChild) {
        try {
            elderChild.setId(id);
            int result = elderChildMapper.updateById(elderChild);
            if (result > 0) {
                return ResponseEntity.ok(elderChild);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除老人子女关系")
    public ResponseEntity<Void> deleteElderChild(@Parameter(description = "关系ID") @PathVariable Long id) {
        try {
            int result = elderChildMapper.deleteById(id);
            if (result > 0) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/check/{elderId}")
    @Operation(summary = "检查老人是否有签约关系")
    public ResponseEntity<Boolean> checkElderContract(@Parameter(description = "老人ID") @PathVariable Long elderId) {
        try {
            List<ElderChild> elderChildren = elderChildMapper.selectByElderId(elderId);
            return ResponseEntity.ok(!elderChildren.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(false);
        }
    }
}
