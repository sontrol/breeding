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
    @PreAuthorize("hasAuthority('system:view')")
    public Result<Page<User>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName) {
        Page<User> resultPage = userService.getUserPage(page, size, username, realName);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Boolean> add(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.save(user) ? Result.success() : Result.error("新增用户失败");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Boolean> update(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return userService.updateById(user) ? Result.success() : Result.error("修改用户失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return userService.removeById(id) ? Result.success() : Result.error("删除用户失败");
    }
}
