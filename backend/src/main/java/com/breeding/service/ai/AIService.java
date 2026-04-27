package com.breeding.service.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.breeding.common.LoginUser;
import com.breeding.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {

    private static final int AI_HTTP_TIMEOUT = 60000;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    @Autowired
    private AIDataAccessLayer dataAccessLayer;

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 向AI助手发起聊天请求
     */
    public String chatWithAI(String userMessage) {
        // 1. 权限拦截与上下文数据获取 (基于RBAC严格隔离)
        Map<String, Object> contextData = dataAccessLayer.getAccessibleContextData();
        String accessedModules = (String) contextData.remove("accessedModules");

        // 2. 构建系统提示词，注入当前用户允许访问的上下文数据
        String systemPrompt = buildSystemPrompt(contextData, accessedModules);

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
                    .timeout(AI_HTTP_TIMEOUT)
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
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("timeout")) {
                aiResponseContent = "调用AI接口超时，请稍后重试或缩小提问范围";
            } else {
                aiResponseContent = "调用AI接口失败: " + message;
            }
        }

        // 5. 记录AI访问审计日志 (动态追踪)
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        systemLogService.logAIAccess(loginUser.getUser().getId(), userMessage, accessedModules, aiResponseContent);

        return aiResponseContent;
    }

    private String buildSystemPrompt(Map<String, Object> contextData, String accessedModules) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的养殖管理系统AI助手。你的任务是基于我提供的系统数据上下文来回答用户问题。\n");
        prompt.append("【重要安全规则】：你只能基于以下提供的系统数据进行回答，绝不能编造任何数据库中不存在或当前上下文未提供的数据。\n");
        prompt.append("【权限判断规则】：\n");
        prompt.append("1. 只有当问题所属模块不在 authorized_modules 中时，才能明确告知\"当前用户权限不足\"。\n");
        prompt.append("2. 如果问题所属模块已经在 authorized_modules 中，优先使用 details 里的明细记录直接回答，再结合 metrics 做汇总说明。\n");
        prompt.append("3. 只有在模块已授权、但当前上下文确实没有对应字段或记录时，才能说明\"当前已具备该模块权限，但当前上下文没有对应明细数据\"。\n");
        prompt.append("4. 如果问题和多个模块相关，只能使用已授权模块里的上下文综合回答。\n\n");
        prompt.append("【输出格式规则】：请直接输出纯文本中文答案，不要使用 Markdown 标记，不要输出 **、#、`、- 这类格式符号作为装饰。\n\n");
        prompt.append("【当前已授权模块】：");
        prompt.append(accessedModules == null || accessedModules.isBlank() ? "无" : accessedModules).append("\n\n");
        prompt.append("【当前系统可访问的上下文数据】：\n");
        prompt.append(JSONUtil.toJsonStr(contextData)).append("\n\n");
        prompt.append("请使用专业、简洁的语言进行回答。");
        return prompt.toString();
    }
}
