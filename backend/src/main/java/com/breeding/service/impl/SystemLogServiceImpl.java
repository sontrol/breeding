package com.breeding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.SystemLog;
import com.breeding.mapper.SystemLogMapper;
import com.breeding.service.SystemLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Override
    public void logAIAccess(Long userId, String queryContent, String accessedModules, String responseContent) {
        SystemLog log = new SystemLog();
        log.setLogType(1);
        log.setUserId(userId);
        log.setModule("AI助手");
        log.setAction("查询");
        log.setQueryContent(queryContent);
        log.setAccessedModules(accessedModules);
        log.setResponseContent(responseContent);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }

    @Override
    public void logOperation(Long userId, String module, String action, String method, String params, String ip) {
        SystemLog log = new SystemLog();
        log.setLogType(2);
        log.setUserId(userId);
        log.setModule(module);
        log.setAction(action);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }
}
