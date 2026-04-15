package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Alert;
import com.breeding.entity.Inventory;
import com.breeding.entity.User;
import com.breeding.mapper.AlertMapper;
import com.breeding.mapper.UserMapper;
import com.breeding.service.AlertService;
import com.breeding.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Alert> getAlertPage(int pageNum, int pageSize, String ruleType, Integer status) {
        Page<Alert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        
        if (ruleType != null && !ruleType.isEmpty()) {
            wrapper.eq(Alert::getRuleType, ruleType);
        }
        if (status != null) {
            wrapper.eq(Alert::getStatus, status);
        }
        
        wrapper.orderByDesc(Alert::getCreateTime);
        Page<Alert> alertPage = this.page(page, wrapper);
        alertPage.getRecords().forEach(alert -> alert.setCreatorName(resolveCreatorName(alert.getCreatorId())));
        return alertPage;
    }

    /**
     * 定时任务：每天凌晨 2 点执行
     * 扫描 b_inventory 表中已过期或 7 天内即将过期的物品，生成预警
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkInventoryExpire() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(7);

        List<Inventory> expiringList = inventoryService.list(new LambdaQueryWrapper<Inventory>()
                .le(Inventory::getExpireDate, warningDate));

        for (Inventory item : expiringList) {
            // 检查是否已经为该物品生成过未处理的过期预警，防止重复报警
            boolean exists = this.count(new LambdaQueryWrapper<Alert>()
                    .eq(Alert::getRuleType, "medicine_expire")
                    .eq(Alert::getTargetId, item.getId())
                    .eq(Alert::getStatus, 0)) > 0;
            
            if (!exists) {
                Alert alert = new Alert();
                alert.setRuleType("medicine_expire");
                alert.setTargetId(item.getId());
                
                if (item.getExpireDate().isBefore(today)) {
                    alert.setAlertMsg(String.format("物品 [%s] (批次:%s) 已过期！过期时间：%s", 
                            item.getItemName(), item.getBatchNumber(), item.getExpireDate()));
                } else {
                    alert.setAlertMsg(String.format("物品 [%s] (批次:%s) 即将过期。过期时间：%s", 
                            item.getItemName(), item.getBatchNumber(), item.getExpireDate()));
                }
                alert.setStatus(0);
                alert.setCreateTime(LocalDateTime.now());
                this.save(alert);
            }
        }
    }

    private String resolveCreatorName(Long creatorId) {
        if (creatorId == null) {
            return "系统";
        }
        User creator = userMapper.selectById(creatorId);
        if (creator == null) {
            return "系统";
        }
        if (creator.getRealName() != null && !creator.getRealName().isBlank()) {
            return creator.getRealName();
        }
        if (creator.getUsername() != null && !creator.getUsername().isBlank()) {
            return creator.getUsername();
        }
        return "系统";
    }
}
