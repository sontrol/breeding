package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.FeedingPlan;

public interface FeedingPlanService extends IService<FeedingPlan> {
    Page<FeedingPlan> getPlanPage(int page, int size, Long shedId, Integer status);
}
