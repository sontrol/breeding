package com.breeding.controller;

import com.breeding.common.JwtUtils;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.dto.auth.RegisterDTO;
import com.breeding.service.RegisterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin // 允许跨域
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RegisterService registerService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
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
            data.put("roles", loginUser.getPermissions().stream()
                    .filter(p -> p.startsWith("ROLE_"))
                    .map(p -> p.substring(5))
                    .toList());
            data.put("permissions", loginUser.getPermissions().stream().filter(p -> !p.startsWith("ROLE_")).toList());

            return Result.success(data);
        } catch (RuntimeException e) {
            log.warn("登录失败 username={}", loginDTO.getUsername(), e);
            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        registerService.submit(registerDTO);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.success();
    }
}

@Data
class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
