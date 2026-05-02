package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.FeedingRecord;

public interface FeedingRecordService extends IService<FeedingRecord> {
    Page<FeedingRecord> getRecordPage(int page, int size, Long shedId, Long operatorId);
    boolean addRecordWithInventory(FeedingRecord record, Long operatorId);
}
