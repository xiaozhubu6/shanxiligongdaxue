package com.zhuqilong.back.controller;

import com.zhuqilong.back.entity.Community;
import com.zhuqilong.back.entity.GridGroup;
import com.zhuqilong.back.mapper.CommunityMapper;
import com.zhuqilong.back.mapper.GridGroupMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/community")
@Tag(name = "社区管理", description = "社区和网格群管理相关接口")
@CrossOrigin(origins = "*")
public class CommunityController {

    @Autowired
    private CommunityMapper communityMapper;
    
    @Autowired
    private GridGroupMapper gridGroupMapper;

    @GetMapping
    @Operation(summary = "获取所有社区")
    public ResponseEntity<List<Community>> getAllCommunities() {
        List<Community> communities = communityMapper.selectAll();
        return ResponseEntity.ok(communities);
    }

    @GetMapping("/{communityId}")
    @Operation(summary = "根据ID获取社区")
    public ResponseEntity<Community> getCommunityById(@Parameter(description = "社区ID") @PathVariable Long communityId) {
        Community community = communityMapper.selectById(communityId);
        if (community != null) {
            return ResponseEntity.ok(community);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "创建社区")
    public ResponseEntity<Community> createCommunity(@RequestBody Community community) {
        community.setCreatedAt(LocalDateTime.now());
        int result = communityMapper.insert(community);
        if (result > 0) {
            return ResponseEntity.ok(community);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{communityId}/update")
    @Operation(summary = "更新社区信息")
    public ResponseEntity<Community> updateCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId,
            @RequestBody Community community) {
        community.setId(communityId);
        int result = communityMapper.updateById(community);
        if (result > 0) {
            return ResponseEntity.ok(community);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{communityId}")
    @Operation(summary = "删除社区")
    public ResponseEntity<Void> deleteCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId) {
        int result = communityMapper.deleteById(communityId);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{communityId}/grid-groups")
    @Operation(summary = "获取社区的网格群列表")
    public ResponseEntity<List<GridGroup>> getGridGroupsByCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId) {
        List<GridGroup> gridGroups = gridGroupMapper.selectWithUnitCountByCommunityId(communityId);
        return ResponseEntity.ok(gridGroups);
    }

    @PostMapping("/{communityId}/grid-groups")
    @Operation(summary = "为社区创建网格群")
    public ResponseEntity<GridGroup> createGridGroup(
            @Parameter(description = "社区ID") @PathVariable Long communityId,
            @RequestBody GridGroup gridGroup) {
        gridGroup.setCommunityId(communityId);
        gridGroup.setCreatedAt(LocalDateTime.now());
        // 临时设置默认unitNumber，与数据库保持一致
        if (gridGroup.getUnitNumber() == null) {
            gridGroup.setUnitNumber("默认单元");
        }
        int result = gridGroupMapper.insert(gridGroup);
        if (result > 0) {
            return ResponseEntity.ok(gridGroup);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/grid-groups/{groupId}/update")
    @Operation(summary = "更新网格群信息")
    public ResponseEntity<GridGroup> updateGridGroup(
            @Parameter(description = "网格群ID") @PathVariable Long groupId,
            @RequestBody GridGroup gridGroup) {
        gridGroup.setId(groupId);
        int result = gridGroupMapper.updateById(gridGroup);
        if (result > 0) {
            return ResponseEntity.ok(gridGroup);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/grid-groups/{groupId}")
    @Operation(summary = "删除网格群")
    public ResponseEntity<Void> deleteGridGroup(
            @Parameter(description = "网格群ID") @PathVariable Long groupId) {
        int result = gridGroupMapper.deleteById(groupId);
        if (result > 0) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/grid-groups")
    @Operation(summary = "获取所有网格群（带单元统计）")
    public ResponseEntity<List<GridGroup>> getAllGroupsWithUnitCount() {
        List<GridGroup> gridGroups = gridGroupMapper.selectAllWithUnitCount();
        return ResponseEntity.ok(gridGroups);
    }
}
