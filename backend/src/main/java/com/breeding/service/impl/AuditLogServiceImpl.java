package com.breeding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.AuditLog;
import com.breeding.mapper.AuditLogMapper;
import com.breeding.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Override
    public void logAIAccess(Long userId, String queryContent, String accessedModules, String responseContent) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setQueryContent(queryContent);
        log.setAccessedModules(accessedModules);
        log.setResponseContent(responseContent);
        log.setQueryTime(LocalDateTime.now());
        this.save(log);
    }
}
