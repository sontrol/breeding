package com.breeding.controller;

import com.breeding.common.Result;
import com.breeding.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(dashboardService.getDashboardStats());
    }
}
