package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.Alert;
import com.breeding.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/alert")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('alert:view')")
    public Result<Page<Alert>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Integer status) {
        Page<Alert> resultPage = alertService.getAlertPage(page, size, ruleType, status);
        return Result.success(resultPage);
    }

    @PutMapping("/handle/{id}")
    @PreAuthorize("hasAuthority('alert:handle')")
    public Result<Boolean> handleAlert(@PathVariable Long id) {
        Alert alert = alertService.getById(id);
        if (alert == null) {
            return Result.error("预警记录不存在");
        }
        
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        alert.setStatus(1); // 已处理
        alert.setHandleTime(LocalDateTime.now());
        alert.setHandlerId(loginUser.getUser().getId());
        
        return alertService.updateById(alert) ? Result.success() : Result.error("处理失败");
    }

    @PostMapping("/trigger-check")
    @PreAuthorize("hasAuthority('alert:check') or hasAuthority('system:*')")
    public Result<Boolean> manualTriggerCheck() {
        alertService.checkInventoryExpire();
        return Result.success();
    }
}
