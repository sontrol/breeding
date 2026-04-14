package com.breeding.controller;

import com.breeding.common.JwtUtils;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin // 允许跨域
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        try {
            // 1. 用户认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );

            // 2. 如果认证通过，生成JWT
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            String token = jwtUtils.generateToken(loginUser);

            // 3. 返回Token及用户信息
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", loginUser.getUser().getId());
            data.put("username", loginUser.getUsername());
            data.put("realName", loginUser.getUser().getRealName());
            data.put("roles", loginUser.getPermissions().stream().filter(p -> p.startsWith("ROLE_")).toList());
            data.put("permissions", loginUser.getPermissions().stream().filter(p -> !p.startsWith("ROLE_")).toList());

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.success();
    }
}

@Data
class LoginDTO {
    private String username;
    private String password;
}
