package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Alert;

public interface AlertService extends IService<Alert> {
    Page<Alert> getAlertPage(int page, int size, String ruleType, Integer status);
    
    // 定时任务：检查过期物品
    void checkInventoryExpire();
}
