package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Diagnosis;
import com.breeding.service.InvalidDataService;
import com.breeding.service.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Page<Diagnosis>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) String diseaseName) {
        Page<Diagnosis> resultPage = diagnosisService.getDiagnosisPage(page, size, animalId, diseaseName);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('diagnosis:add')")
    public Result<Boolean> add(@RequestBody Diagnosis diagnosis) {
        return diagnosisService.save(diagnosis) ? Result.success() : Result.error("诊断记录保存失败");
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('diagnosis:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("diagnosis", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废诊断失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
