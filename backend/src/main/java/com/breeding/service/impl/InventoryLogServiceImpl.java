package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.InventoryLog;
import com.breeding.mapper.InventoryLogMapper;
import com.breeding.service.InventoryLogService;
import org.springframework.stereotype.Service;

@Service
public class InventoryLogServiceImpl extends ServiceImpl<InventoryLogMapper, InventoryLog> implements InventoryLogService {

    @Override
    public Page<InventoryLog> getLogPage(int pageNum, int pageSize, Long inventoryId) {
        Page<InventoryLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (inventoryId != null) {
            wrapper.eq(InventoryLog::getInventoryId, inventoryId);
        }
        wrapper.orderByDesc(InventoryLog::getOperateTime);
        return this.page(page, wrapper);
    }
}
