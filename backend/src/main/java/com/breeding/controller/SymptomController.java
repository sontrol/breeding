package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Symptom;
import com.breeding.service.InvalidDataService;
import com.breeding.service.SymptomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/symptom")
public class SymptomController {

    @Autowired
    private SymptomService symptomService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Page<Symptom>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Integer status) {
        Page<Symptom> resultPage = symptomService.getSymptomPage(page, size, animalId, status);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('disease:add')")
    public Result<Boolean> add(@Valid @RequestBody Symptom symptom) {
        LoginUser loginUser = LoginUser.getCurrentUser();
        symptom.setObserverId(loginUser.getUser().getId());
        symptom.setObserveTime(LocalDateTime.now());
        return symptomService.save(symptom) ? Result.success() : Result.error("上报症状失败");
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('symptom:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = LoginUser.getCurrentUser();
            boolean success = invalidDataService.invalidate("symptom", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废症状失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
