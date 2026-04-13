package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.Treatment;
import com.breeding.service.TreatmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/treatment")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('disease:list', 'ROLE_ADMIN', 'ROLE_VET')")
    public Result<Page<Treatment>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Long diagnosisId) {
        Page<Treatment> resultPage = treatmentService.getTreatmentPage(page, size, animalId, diagnosisId);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('disease:treat', 'ROLE_ADMIN', 'ROLE_VET')")
    public Result<Boolean> add(@RequestBody Treatment treatment) {
        return treatmentService.save(treatment) ? Result.success() : Result.error("治疗记录保存失败");
    }
}
