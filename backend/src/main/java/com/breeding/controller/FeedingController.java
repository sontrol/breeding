package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.FeedingPlan;
import com.breeding.entity.FeedingRecord;
import com.breeding.service.FeedingPlanService;
import com.breeding.service.FeedingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feeding")
public class FeedingController {

    @Autowired
    private FeedingPlanService planService;

    @Autowired
    private FeedingRecordService recordService;

    // ----- 计划管理 -----
    @GetMapping("/plan/page")
    @PreAuthorize("hasAnyAuthority('feeding:plan:list', 'ROLE_ADMIN', 'ROLE_BREEDER')")
    public Result<Page<FeedingPlan>> getPlanPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shedId,
            @RequestParam(required = false) Integer status) {
        return Result.success(planService.getPlanPage(page, size, shedId, status));
    }

    @PostMapping("/plan")
    @PreAuthorize("hasAnyAuthority('feeding:plan:add', 'ROLE_ADMIN', 'ROLE_BREEDER')")
    public Result<Boolean> addPlan(@RequestBody FeedingPlan plan) {
        return planService.save(plan) ? Result.success() : Result.error("新增计划失败");
    }

    @PutMapping("/plan")
    @PreAuthorize("hasAnyAuthority('feeding:plan:edit', 'ROLE_ADMIN', 'ROLE_BREEDER')")
    public Result<Boolean> updatePlan(@RequestBody FeedingPlan plan) {
        return planService.updateById(plan) ? Result.success() : Result.error("修改计划失败");
    }

    // ----- 记录管理 -----
    @GetMapping("/record/page")
    @PreAuthorize("hasAnyAuthority('feeding:record:list', 'ROLE_ADMIN', 'ROLE_BREEDER')")
    public Result<Page<FeedingRecord>> getRecordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shedId,
            @RequestParam(required = false) Long operatorId) {
        return Result.success(recordService.getRecordPage(page, size, shedId, operatorId));
    }

    @PostMapping("/record")
    @PreAuthorize("hasAnyAuthority('feeding:record:add', 'ROLE_ADMIN', 'ROLE_BREEDER')")
    public Result<Boolean> addRecord(@RequestBody FeedingRecord record) {
        return recordService.save(record) ? Result.success() : Result.error("新增记录失败");
    }
}
