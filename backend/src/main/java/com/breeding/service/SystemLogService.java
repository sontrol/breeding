package com.breeding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.SystemLog;

public interface SystemLogService extends IService<SystemLog> {
    void logAIAccess(Long userId, String queryContent, String accessedModules, String responseContent);
    void logOperation(Long userId, String module, String action, String method, String params, String ip);
}
