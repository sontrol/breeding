package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.entity.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.breeding.entity.InventoryLog;
import com.breeding.mapper.InventoryLogMapper;
import com.breeding.mapper.InventoryMapper;
import com.breeding.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addWithLog(Inventory inventory) {
        boolean saved = super.save(inventory);
        if (saved && inventory.getId() != null) {
            Long operatorId = resolveOperatorId();
            InventoryLog invLog = new InventoryLog();
            invLog.setInventoryId(inventory.getId());
            invLog.setOperationType(1); // 入库
            invLog.setQuantity(inventory.getQuantity());
            invLog.setOperatorId(operatorId);
            invLog.setOperateTime(LocalDateTime.now());
            invLog.setRemark("入库登记: " + inventory.getItemName() + " (批次:" + inventory.getBatchNumber() + ")");
            inventoryLogMapper.insert(invLog);
        }
        return saved;
    }

    private Long resolveOperatorId() {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return loginUser.getUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductInventory(Long inventoryId, BigDecimal quantity, Long operatorId, String remark) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("库存不足，当前库存: " + inventory.getQuantity() + inventory.getUnit());
        }
        
        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
        this.updateById(inventory);
        
        InventoryLog log = new InventoryLog();
        log.setInventoryId(inventoryId);
        log.setOperationType(2); // 出库
        log.setQuantity(quantity);
        log.setOperatorId(operatorId);
        log.setOperateTime(LocalDateTime.now());
        log.setRemark(remark);
        inventoryLogMapper.insert(log);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductByItemName(String itemName, BigDecimal quantity, Long operatorId, String remark) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getItemName, itemName)
               .eq(Inventory::getDeleted, 0)
               .orderByAsc(Inventory::getExpireDate)
               .last("LIMIT 1");
        Inventory inventory = this.getOne(wrapper);
        
        if (inventory == null) {
            throw new BusinessException("未找到 [" + itemName + "] 的库存记录");
        }
        
        return deductInventory(inventory.getId(), quantity, operatorId, remark);
    }
}
