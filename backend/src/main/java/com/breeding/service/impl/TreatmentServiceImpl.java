package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Treatment;
import com.breeding.entity.TreatmentItem;
import com.breeding.mapper.TreatmentItemMapper;
import com.breeding.mapper.TreatmentMapper;
import com.breeding.service.TreatmentService;
import com.breeding.vo.TreatmentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TreatmentServiceImpl extends ServiceImpl<TreatmentMapper, Treatment> implements TreatmentService {

    @Autowired
    private TreatmentItemMapper treatmentItemMapper;

    @Override
    public Page<TreatmentVO> getTreatmentPage(int pageNum, int pageSize, Long animalId, Long diagnosisId) {
        Page<Treatment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Treatment> wrapper = new LambdaQueryWrapper<>();
        
        if (animalId != null) {
            wrapper.eq(Treatment::getAnimalId, animalId);
        }
        if (diagnosisId != null) {
            wrapper.eq(Treatment::getDiagnosisId, diagnosisId);
        }
        
        wrapper.orderByDesc(Treatment::getTime);
        Page<Treatment> result = this.page(page, wrapper);
        
        Page<TreatmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        
        Map<Long, List<TreatmentItem>> itemMap;
        if (!result.getRecords().isEmpty()) {
            List<Long> treatmentIds = result.getRecords().stream()
                .map(Treatment::getId)
                .collect(Collectors.toList());
            
            LambdaQueryWrapper<TreatmentItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(TreatmentItem::getTreatmentId, treatmentIds);
            List<TreatmentItem> allItems = treatmentItemMapper.selectList(itemWrapper);
            
            itemMap = allItems.stream()
                .collect(Collectors.groupingBy(TreatmentItem::getTreatmentId));
        } else {
            itemMap = Map.of();
        }
        
        List<TreatmentVO> voList = result.getRecords().stream().map(t -> {
            TreatmentVO vo = new TreatmentVO();
            BeanUtils.copyProperties(t, vo);
            vo.setItems(itemMap.getOrDefault(t.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
}
