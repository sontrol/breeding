package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.entity.User;
import com.breeding.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:view')")
    public Result<Page<User>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String roleCode) {
        Page<User> resultPage = userService.getUserPage(page, size, username, realName, roleCode);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Boolean> add(@Valid @RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.saveUserWithRole(user) ? Result.success() : Result.error("新增用户失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Boolean> update(@Valid @RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return userService.saveUserWithRole(user) ? Result.success() : Result.error("修改用户失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (id.equals(loginUser.getUser().getId())) {
                return Result.error("不能作废自己的账号");
            }
            boolean success = userService.invalidateUser(id);
            return success ? Result.success() : Result.error("作废用户失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
