package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.InvalidRecord;
import com.breeding.service.InvalidDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invalid-data")
public class InvalidDataController {

    private final InvalidDataService invalidDataService;

    public InvalidDataController(InvalidDataService invalidDataService) {
        this.invalidDataService = invalidDataService;
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:invalid:view')")
    public Result<Page<InvalidRecord>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String dataType) {
        return Result.success(invalidDataService.getInvalidPage(page, size, dataType));
    }

    @PutMapping("/restore/{id}")
    @PreAuthorize("hasAuthority('system:invalid:restore')")
    public Result<Boolean> restore(@PathVariable Long id) {
        try {
            return invalidDataService.restore(id) ? Result.success() : Result.error("恢复失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
