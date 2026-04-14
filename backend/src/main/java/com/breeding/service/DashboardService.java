package com.breeding.service;

import java.util.Map;

public interface DashboardService {
    /**
     * 获取数据看板的统计数据
     */
    Map<String, Object> getDashboardStats();
}
