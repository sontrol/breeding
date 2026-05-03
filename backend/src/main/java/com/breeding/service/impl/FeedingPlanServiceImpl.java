package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.FeedingPlan;
import com.breeding.entity.Inventory;
import com.breeding.mapper.FeedingPlanMapper;
import com.breeding.service.FeedingPlanService;
import com.breeding.service.InventoryService;
import com.breeding.vo.FeedingPlanVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedingPlanServiceImpl extends ServiceImpl<FeedingPlanMapper, FeedingPlan> implements FeedingPlanService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public boolean save(FeedingPlan plan) {
        if (plan.getAmountPerAnimal() == null) {
            plan.setAmountPerAnimal(BigDecimal.ZERO);
        }
        if (plan.getFeedingTime() == null) {
            plan.setFeedingTime(Time.valueOf("08:00:00"));
        }
        return super.save(plan);
    }

    @Override
    public Page<FeedingPlanVO> getPlanPage(int pageNum, int pageSize, Long shedId, Integer status) {
        Page<FeedingPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FeedingPlan> wrapper = new LambdaQueryWrapper<>();
        
        if (shedId != null) {
            wrapper.eq(FeedingPlan::getShedId, shedId);
        }
        if (status != null) {
            wrapper.eq(FeedingPlan::getStatus, status);
        }
        
        wrapper.orderByDesc(FeedingPlan::getCreateTime);
        Page<FeedingPlan> result = this.page(page, wrapper);
        
        Page<FeedingPlanVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        
        List<Long> inventoryIds = result.getRecords().stream()
            .map(FeedingPlan::getInventoryId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, String> batchMap;
        if (!inventoryIds.isEmpty()) {
            List<Inventory> inventories = inventoryService.listByIds(inventoryIds);
            batchMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getId, Inventory::getBatchNumber));
        } else {
            batchMap = Map.of();
        }
        
        List<FeedingPlanVO> voList = result.getRecords().stream().map(plan -> {
            FeedingPlanVO vo = new FeedingPlanVO();
            BeanUtils.copyProperties(plan, vo);
            if (plan.getInventoryId() != null) {
                vo.setBatchNumber(batchMap.get(plan.getInventoryId()));
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
}
