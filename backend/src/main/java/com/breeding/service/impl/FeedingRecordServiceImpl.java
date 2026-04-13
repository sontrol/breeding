package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.FeedingRecord;
import com.breeding.mapper.FeedingRecordMapper;
import com.breeding.service.FeedingRecordService;
import org.springframework.stereotype.Service;

@Service
public class FeedingRecordServiceImpl extends ServiceImpl<FeedingRecordMapper, FeedingRecord> implements FeedingRecordService {

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
        
        wrapper.orderByDesc(FeedingRecord::getExecuteTime);
        return this.page(page, wrapper);
    }
}
