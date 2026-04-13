package com.breeding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.AuditLog;

public interface AuditLogService extends IService<AuditLog> {
    void logAIAccess(Long userId, String queryContent, String accessedModules, String responseContent);
}
