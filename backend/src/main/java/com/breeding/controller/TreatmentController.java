package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Treatment;
import com.breeding.service.InvalidDataService;
import com.breeding.service.TreatmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/treatment")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Page<Treatment>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Long diagnosisId) {
        Page<Treatment> resultPage = treatmentService.getTreatmentPage(page, size, animalId, diagnosisId);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('treatment:add')")
    public Result<Boolean> add(@RequestBody Treatment treatment) {
        return treatmentService.save(treatment) ? Result.success() : Result.error("治疗记录保存失败");
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('treatment:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("treatment", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废治疗失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
