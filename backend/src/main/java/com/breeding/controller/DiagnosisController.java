package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.Diagnosis;
import com.breeding.service.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

    @Autowired
    private DiagnosisService diagnosisService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('disease:list', 'ROLE_ADMIN', 'ROLE_VET')")
    public Result<Page<Diagnosis>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) String diseaseName) {
        Page<Diagnosis> resultPage = diagnosisService.getDiagnosisPage(page, size, animalId, diseaseName);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('disease:diagnose', 'ROLE_ADMIN', 'ROLE_VET')")
    public Result<Boolean> add(@RequestBody Diagnosis diagnosis) {
        return diagnosisService.save(diagnosis) ? Result.success() : Result.error("诊断记录保存失败");
    }
}
