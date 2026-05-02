package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Vaccine;
import com.breeding.entity.VaccineRecord;
import com.breeding.service.InvalidDataService;
import com.breeding.service.VaccineRecordService;
import com.breeding.service.VaccineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vaccine")
public class VaccineController {

    @Autowired
    private VaccineService vaccineService;

    @Autowired
    private VaccineRecordService vaccineRecordService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<java.util.List<Vaccine>> list() {
        return Result.success(vaccineService.list());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Page<Vaccine>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String targetDisease) {
        return Result.success(vaccineService.getVaccinePage(page, size, name, targetDisease));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Vaccine> getById(@PathVariable Long id) {
        return Result.success(vaccineService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('vaccine:add')")
    public Result<Boolean> add(@Valid @RequestBody Vaccine vaccine) {
        return vaccineService.save(vaccine) ? Result.success() : Result.error("新增疫苗失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('vaccine:edit')")
    public Result<Boolean> update(@Valid @RequestBody Vaccine vaccine) {
        return vaccineService.updateById(vaccine) ? Result.success() : Result.error("修改疫苗失败");
    }

    @GetMapping("/record/page")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Page<VaccineRecord>> getRecordPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Long vaccineId) {
        return Result.success(vaccineRecordService.getRecordPage(page, size, animalId, vaccineId));
    }

    @PostMapping("/record")
    @PreAuthorize("hasAuthority('vaccine:add')")
    public Result<Boolean> addRecord(@Valid @RequestBody VaccineRecord record) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        record.setOperatorId(loginUser.getUser().getId());
        return vaccineRecordService.save(record) ? Result.success() : Result.error("新增接种记录失败");
    }

    @PutMapping("/record/invalidate/{id}")
    @PreAuthorize("hasAuthority('vaccine:invalidate')")
    public Result<Boolean> invalidateRecord(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("vaccine_record", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废接种记录失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
