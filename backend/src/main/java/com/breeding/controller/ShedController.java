package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.Result;
import com.breeding.entity.Shed;
import com.breeding.service.ShedService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shed")
public class ShedController {

    @Autowired
    private ShedService shedService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<Page<Shed>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        return Result.success(shedService.getShedPage(page, size, name));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<Shed> getById(@PathVariable Long id) {
        return Result.success(shedService.getById(id));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<java.util.List<Shed>> list() {
        return Result.success(shedService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('animal:add')")
    public Result<Boolean> add(@Valid @RequestBody Shed shed) {
        return shedService.save(shed) ? Result.success() : Result.error("新增栏舍失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('animal:edit')")
    public Result<Boolean> update(@Valid @RequestBody Shed shed) {
        return shedService.updateById(shed) ? Result.success() : Result.error("修改栏舍失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('animal:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return shedService.deleteWithCheck(id) ? Result.success() : Result.error("删除栏舍失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
