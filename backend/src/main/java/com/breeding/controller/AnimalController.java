package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.Animal;
import com.breeding.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/animal")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<Page<Animal>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String earTag,
            @RequestParam(required = false) Integer status) {
        Page<Animal> resultPage = animalService.getAnimalPage(page, size, earTag, status);
        return Result.success(resultPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<Animal> getById(@PathVariable Long id) {
        Animal animal = animalService.getById(id);
        return Result.success(animal);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('animal:add')")
    public Result<Boolean> add(@RequestBody Animal animal) {
        boolean saved = animalService.save(animal);
        return saved ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('animal:edit')")
    public Result<Boolean> update(@RequestBody Animal animal) {
        boolean updated = animalService.updateById(animal);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('animal:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = animalService.removeById(id);
        return removed ? Result.success() : Result.error("删除失败");
    }
}