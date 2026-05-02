package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Animal;
import com.breeding.service.AnimalService;
import com.breeding.service.InvalidDataService;
import com.breeding.vo.AnimalDetailVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/animal")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<java.util.List<Animal>> list() {
        return Result.success(animalService.list());
    }

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

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<AnimalDetailVO> getDetail(@PathVariable Long id) {
        AnimalDetailVO detail = animalService.getAnimalDetail(id);
        if (detail.getAnimal() == null) {
            return Result.error("动物不存在");
        }
        return Result.success(detail);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('animal:view')")
    public Result<Animal> getById(@PathVariable Long id) {
        Animal animal = animalService.getById(id);
        return Result.success(animal);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('animal:add')")
    public Result<Boolean> add(@Valid @RequestBody Animal animal) {
        boolean saved = animalService.save(animal);
        return saved ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('animal:edit')")
    public Result<Boolean> update(@Valid @RequestBody Animal animal) {
        boolean updated = animalService.updateById(animal);
        return updated ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('animal:invalidate')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return invalidate(id);
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('animal:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("animal", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
