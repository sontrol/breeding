package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.FeedingPlan;
import com.breeding.vo.FeedingPlanVO;

public interface FeedingPlanService extends IService<FeedingPlan> {
    Page<FeedingPlanVO> getPlanPage(int page, int size, Long shedId, Integer status);
}
