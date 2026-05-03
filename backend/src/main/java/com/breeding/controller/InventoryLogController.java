package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.InventoryLog;
import com.breeding.service.InventoryLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory-log")
public class InventoryLogController {

    @Autowired
    private InventoryLogService inventoryLogService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('inventory:view')")
    public Result<Page<InventoryLog>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long inventoryId) {
        return Result.success(inventoryLogService.getLogPage(page, size, inventoryId));
    }
}
