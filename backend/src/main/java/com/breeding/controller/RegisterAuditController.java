package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.dto.user.RegisterAuditDTO;
import com.breeding.service.RegisterAuditService;
import com.breeding.vo.RegisterAuditVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register-audit")
public class RegisterAuditController {

    @Autowired
    private RegisterAuditService registerAuditService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:register:view')")
    public Result<Page<RegisterAuditVO>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String applyRoleCode) {
        return Result.success(registerAuditService.getPage(page, size, username, realName, auditStatus, applyRoleCode));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('system:register:approve')")
    public Result<Void> approve(@Valid @RequestBody RegisterAuditDTO auditDTO) {
        registerAuditService.approve(auditDTO, getCurrentUserId());
        return Result.success();
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('system:register:reject')")
    public Result<Void> reject(@Valid @RequestBody RegisterAuditDTO auditDTO) {
        registerAuditService.reject(auditDTO, getCurrentUserId());
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUser().getId();
        }
        throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("未授权的访问或认证已失效");
    }
}
