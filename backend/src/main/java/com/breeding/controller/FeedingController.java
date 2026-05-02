package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.FeedingPlan;
import com.breeding.entity.FeedingRecord;
import com.breeding.service.FeedingPlanService;
import com.breeding.service.FeedingRecordService;
import com.breeding.service.InvalidDataService;
import com.breeding.vo.FeedingPlanVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feeding")
public class FeedingController {

    @Autowired
    private FeedingPlanService planService;

    @Autowired
    private FeedingRecordService recordService;

    @Autowired
    private InvalidDataService invalidDataService;

    // ----- 计划管理 -----
    @GetMapping("/plan/page")
    @PreAuthorize("hasAuthority('feeding:view')")
    public Result<Page<FeedingPlanVO>> getPlanPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shedId,
            @RequestParam(required = false) Integer status) {
        return Result.success(planService.getPlanPage(page, size, shedId, status));
    }

    @PostMapping("/plan")
    @PreAuthorize("hasAuthority('feeding:plan:add')")
    public Result<Boolean> addPlan(@Valid @RequestBody FeedingPlan plan) {
        return planService.save(plan) ? Result.success() : Result.error("新增计划失败");
    }

    @PutMapping("/plan")
    @PreAuthorize("hasAuthority('feeding:plan:edit')")
    public Result<Boolean> updatePlan(@Valid @RequestBody FeedingPlan plan) {
        return planService.updateById(plan) ? Result.success() : Result.error("修改计划失败");
    }

    @PutMapping("/plan/status")
    @PreAuthorize("hasAuthority('feeding:plan:status')")
    public Result<Boolean> updatePlanStatus(@Valid @RequestBody FeedingPlan plan) {
        FeedingPlan updatePlan = new FeedingPlan();
        updatePlan.setId(plan.getId());
        updatePlan.setStatus(plan.getStatus());
        return planService.updateById(updatePlan) ? Result.success() : Result.error("调整计划状态失败");
    }

    @DeleteMapping("/plan/{id}")
    @PreAuthorize("hasAuthority('feeding:plan:invalidate')")
    public Result<Boolean> deletePlan(@PathVariable Long id) {
        return invalidatePlan(id);
    }

    @PutMapping("/plan/invalidate/{id}")
    @PreAuthorize("hasAuthority('feeding:plan:invalidate')")
    public Result<Boolean> invalidatePlan(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("feeding_plan", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废计划失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    // ----- 记录管理 -----
    @GetMapping("/record/page")
    @PreAuthorize("hasAuthority('feeding:view')")
    public Result<Page<FeedingRecord>> getRecordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shedId,
            @RequestParam(required = false) Long operatorId) {
        return Result.success(recordService.getRecordPage(page, size, shedId, operatorId));
    }

    @PostMapping("/record")
    @PreAuthorize("hasAuthority('feeding:record:add')")
    public Result<Boolean> addRecord(@Valid @RequestBody FeedingRecord record) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return recordService.addRecordWithInventory(record, loginUser.getUser().getId()) ? Result.success() : Result.error("新增记录失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/record/invalidate/{id}")
    @PreAuthorize("hasAuthority('feeding:record:invalidate')")
    public Result<Boolean> invalidateRecord(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("feeding_record", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废记录失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
