package com.breeding.controller;

import com.breeding.common.Result;
import com.breeding.service.ai.AIService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()") // 所有登录用户均可访问AI助手，但AI内部有严格的数据级权限隔离
    public Result<String> chat(@RequestBody ChatDTO request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.error("查询内容不能为空");
        }
        String response = aiService.chatWithAI(request.getMessage());
        return Result.success(response);
    }
}

@Data
class ChatDTO {
    private String message;
}
