package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.FeedingRecord;
import com.breeding.mapper.FeedingRecordMapper;
import com.breeding.service.FeedingRecordService;
import com.breeding.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedingRecordServiceImpl extends ServiceImpl<FeedingRecordMapper, FeedingRecord> implements FeedingRecordService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public Page<FeedingRecord> getRecordPage(int pageNum, int pageSize, Long shedId, Long operatorId) {
        Page<FeedingRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FeedingRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (shedId != null) {
            wrapper.eq(FeedingRecord::getShedId, shedId);
        }
        if (operatorId != null) {
            wrapper.eq(FeedingRecord::getOperatorId, operatorId);
        }
        
        wrapper.orderByDesc(FeedingRecord::getTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRecordWithInventory(FeedingRecord record, Long operatorId) {
        if (record.getInventoryId() != null) {
            inventoryService.deductInventory(
                record.getInventoryId(),
                record.getTotalAmount(),
                operatorId,
                "饲养投喂消耗 - 栏舍ID:" + record.getShedId()
            );
        } else if (record.getFeedType() != null) {
            inventoryService.deductByItemName(
                record.getFeedType(),
                record.getTotalAmount(),
                operatorId,
                "饲养投喂消耗 - 栏舍ID:" + record.getShedId()
            );
        }
        record.setOperatorId(operatorId);
        return this.save(record);
    }
}
