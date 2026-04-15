package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Inventory;

import java.math.BigDecimal;

public interface InventoryService extends IService<Inventory> {
    Page<Inventory> getInventoryPage(int page, int size, String itemName, Integer ItemType);

    boolean deductInventory(Long inventoryId, BigDecimal quantity, Long operatorId, String remark);

    boolean deductByItemName(String itemName, BigDecimal quantity, Long operatorId, String remark);
}
