package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Inventory;

public interface InventoryService extends IService<Inventory> {
    Page<Inventory> getInventoryPage(int page, int size, String itemName, Integer itemType);
}
