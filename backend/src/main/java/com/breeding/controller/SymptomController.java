package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.Symptom;
import com.breeding.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/symptom")
public class SymptomController {

    @Autowired
    private SymptomService symptomService;

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
    public Result<Boolean> add(@RequestBody Symptom symptom) {
        return symptomService.save(symptom) ? Result.success() : Result.error("上报症状失败");
    }
}
