package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.breeding.mapper.AnimalMapper;
import com.breeding.mapper.EventMapper;
import com.breeding.mapper.FeedingRecordMapper;
import com.breeding.mapper.SymptomMapper;
import com.breeding.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private AnimalMapper animalMapper;

    @Autowired
    private SymptomMapper symptomMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private FeedingRecordMapper feedingRecordMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 顶部核心指标统计
        // 当前总存栏量 (状态为 1:健康, 2:患病, 3:隔离 的总和)
        Long currentStock = animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>()
                .in("status", 1, 2, 3));
        result.put("currentStock", currentStock);

        // 今日新生 (出生日期为今天)
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Long newBornToday = animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>()
                .eq("birth_date", today));
        result.put("newBornToday", newBornToday);

        // 患病隔离 (状态为 2:患病 或 3:隔离)
        Long sickAndIsolated = animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>()
                .in("status", 2, 3));
        result.put("sickAndIsolated", sickAndIsolated);

        // 本月出栏 (状态为 5:出栏 且更新时间在本月)
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Long outThisMonth = animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>()
                .eq("status", 5)
                .likeRight("update_time", currentMonth));
        result.put("outThisMonth", outThisMonth);

        // 2. 饼图：动物健康状态分布
        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        statusDistribution.add(createStatusMap("健康", animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>().eq("status", 1))));
        statusDistribution.add(createStatusMap("患病", animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>().eq("status", 2))));
        statusDistribution.add(createStatusMap("隔离", animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>().eq("status", 3))));
        result.put("statusDistribution", statusDistribution);

        // 3. 折线图：近7天核心事件统计
        List<String> dates = new ArrayList<>();
        List<BigDecimal> feedingAmounts = new ArrayList<>();
        List<Long> diseaseCounts = new ArrayList<>();
        List<Long> outCounts = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            dates.add(dateStr);

            // 投喂量统计
            QueryWrapper<com.breeding.entity.FeedingRecord> feedingQuery = new QueryWrapper<>();
            feedingQuery.select("IFNULL(SUM(total_amount), 0) as total").likeRight("time", dateStr);
            Map<String, Object> feedingRes = feedingRecordMapper.selectMaps(feedingQuery).stream().findFirst().orElse(null);
            feedingAmounts.add(feedingRes != null && feedingRes.get("total") != null ? new BigDecimal(feedingRes.get("total").toString()) : BigDecimal.ZERO);

            // 发病数统计 (symptom表按发现时间)
            diseaseCounts.add(symptomMapper.selectCount(new QueryWrapper<com.breeding.entity.Symptom>().likeRight("observe_time", dateStr)));

            // 出栏数统计 (animal状态变更为5的时间)
            outCounts.add(animalMapper.selectCount(new QueryWrapper<com.breeding.entity.Animal>()
                    .eq("status", 5)
                    .likeRight("update_time", dateStr)));
        }

        result.put("chartDates", dates);
        result.put("feedingAmounts", feedingAmounts);
        result.put("diseaseCounts", diseaseCounts);
        result.put("outCounts", outCounts);

        return result;
    }

    private Map<String, Object> createStatusMap(String name, Long value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }
}