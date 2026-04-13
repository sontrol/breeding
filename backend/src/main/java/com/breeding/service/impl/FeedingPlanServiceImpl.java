package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.FeedingPlan;
import com.breeding.mapper.FeedingPlanMapper;
import com.breeding.service.FeedingPlanService;
import org.springframework.stereotype.Service;

@Service
public class FeedingPlanServiceImpl extends ServiceImpl<FeedingPlanMapper, FeedingPlan> implements FeedingPlanService {

    @Override
    public Page<FeedingPlan> getPlanPage(int pageNum, int pageSize, Long shedId, Integer status) {
        Page<FeedingPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FeedingPlan> wrapper = new LambdaQueryWrapper<>();
        
        if (shedId != null) {
            wrapper.eq(FeedingPlan::getShedId, shedId);
        }
        if (status != null) {
            wrapper.eq(FeedingPlan::getStatus, status);
        }
        
        wrapper.orderByDesc(FeedingPlan::getCreateTime);
        return this.page(page, wrapper);
    }
}
