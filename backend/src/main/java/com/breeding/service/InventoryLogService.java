package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.InventoryLog;

public interface InventoryLogService extends IService<InventoryLog> {
    Page<InventoryLog> getLogPage(int page, int size, Long inventoryId);
}
