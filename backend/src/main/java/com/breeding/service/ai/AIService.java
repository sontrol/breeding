package com.breeding.service.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.breeding.common.LoginUser;
import com.breeding.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    @Autowired
    private AIDataAccessLayer dataAccessLayer;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 向AI助手发起聊天请求
     */
    public String chatWithAI(String userMessage) {
        // 1. 权限拦截与上下文数据获取 (基于RBAC严格隔离)
        Map<String, Object> contextData = dataAccessLayer.getAccessibleContextData();
        String accessedModules = (String) contextData.remove("accessedModules");

        // 2. 构建系统提示词，注入当前用户允许访问的上下文数据
        String systemPrompt = buildSystemPrompt(contextData);

        // 3. 构建DeepSeek API请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
        messages.add(new JSONObject().set("role", "user").set("content", userMessage));
        requestBody.set("messages", messages);

        // 4. 调用DeepSeek API
        String aiResponseContent = "";
        try {
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            if (response.isOk()) {
                JSONObject resJson = JSONUtil.parseObj(response.body());
                aiResponseContent = resJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            } else {
                aiResponseContent = "AI服务异常: " + response.getStatus() + " - " + response.body();
            }
        } catch (Exception e) {
            aiResponseContent = "调用AI接口失败: " + e.getMessage();
        }

        // 5. 记录AI访问审计日志 (动态追踪)
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        auditLogService.logAIAccess(loginUser.getUser().getId(), userMessage, accessedModules, aiResponseContent);

        return aiResponseContent;
    }

    private String buildSystemPrompt(Map<String, Object> contextData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的养殖管理系统AI助手。你的任务是基于我提供的系统数据上下文来回答用户问题。\n");
        prompt.append("【重要安全规则】：你只能基于以下提供的系统数据进行回答。如果用户询问超出以下数据范围的内容，你必须明确告知：当前用户权限不足，无法访问该数据。\n\n");
        prompt.append("【当前系统可访问的上下文数据】：\n");
        prompt.append(JSONUtil.toJsonStr(contextData)).append("\n\n");
        prompt.append("请使用专业、简洁的语言进行回答。");
        return prompt.toString();
    }
}
