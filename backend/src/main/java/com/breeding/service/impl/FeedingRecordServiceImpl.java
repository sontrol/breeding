package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Event;
import com.breeding.entity.FeedingRecord;
import com.breeding.mapper.FeedingRecordMapper;
import com.breeding.service.EventService;
import com.breeding.service.FeedingRecordService;
import com.breeding.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FeedingRecordServiceImpl extends ServiceImpl<FeedingRecordMapper, FeedingRecord> implements FeedingRecordService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private EventService eventService;

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
        record.setOperatorId(operatorId);
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
        boolean saved = this.save(record);
        if (saved) {
            createFeedingEvent(record);
        }
        return saved;
    }

    private void createFeedingEvent(FeedingRecord record) {
        if (record.getAnimalId() == null) {
            return; // 无指定动物时跳过事件记录
        }
        Event event = new Event();
        event.setAnimalId(record.getAnimalId());
        event.setEventType(1); // 饲喂
        event.setEventTime(record.getTime() != null ? record.getTime() : LocalDateTime.now());
        event.setOperatorId(record.getOperatorId());
        event.setDescription("饲喂记录 - 栏舍ID:" + record.getShedId() + ", 饲料:" + record.getFeedType() + ", 用量:" + record.getTotalAmount());
        event.setRelatedId(record.getId());
        eventService.save(event);
    }
}
