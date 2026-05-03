package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Inventory;
import com.breeding.service.InvalidDataService;
import com.breeding.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('inventory:view')")
    public Result<Page<Inventory>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) Integer itemType) {
        Page<Inventory> resultPage = inventoryService.getInventoryPage(page, size, itemName, itemType);
        return Result.success(resultPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:view')")
    public Result<Inventory> getById(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:add')")
    public Result<Boolean> add(@Valid @RequestBody Inventory inventory) {
        return inventoryService.save(inventory) ? Result.success() : Result.error("入库失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('inventory:edit')")
    public Result<Boolean> update(@Valid @RequestBody Inventory inventory) {
        Inventory existing = inventoryService.getById(inventory.getId());
        if (existing == null) {
            return Result.error("库存记录不存在");
        }
        inventory.setItemType(existing.getItemType());
        return inventoryService.updateById(inventory) ? Result.success() : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:invalidate')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return invalidate(id);
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('inventory:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = LoginUser.getCurrentUser();
            boolean success = invalidDataService.invalidate("inventory", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
