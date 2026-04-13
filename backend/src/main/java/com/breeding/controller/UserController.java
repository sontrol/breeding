package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.User;
import com.breeding.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyAuthority('sys:user:list', 'ROLE_ADMIN')")
    public Result<Page<User>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName) {
        Page<User> resultPage = userService.getUserPage(page, size, username, realName);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('sys:user:add', 'ROLE_ADMIN')")
    public Result<Boolean> add(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.save(user) ? Result.success() : Result.error("新增用户失败");
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('sys:user:edit', 'ROLE_ADMIN')")
    public Result<Boolean> update(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return userService.updateById(user) ? Result.success() : Result.error("修改用户失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('sys:user:delete', 'ROLE_ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return userService.removeById(id) ? Result.success() : Result.error("删除用户失败");
    }
}
