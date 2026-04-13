package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Inventory;
import com.breeding.mapper.InventoryMapper;
import com.breeding.service.InventoryService;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Override
    public Page<Inventory> getInventoryPage(int pageNum, int pageSize, String itemName, Integer itemType) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        
        if (itemName != null && !itemName.isEmpty()) {
            wrapper.like(Inventory::getItemName, itemName);
        }
        if (itemType != null) {
            wrapper.eq(Inventory::getItemType, itemType);
        }
        
        wrapper.orderByDesc(Inventory::getExpireDate);
        return this.page(page, wrapper);
    }
}
