package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Treatment;
import com.breeding.mapper.TreatmentMapper;
import com.breeding.service.TreatmentService;
import org.springframework.stereotype.Service;

@Service
public class TreatmentServiceImpl extends ServiceImpl<TreatmentMapper, Treatment> implements TreatmentService {

    @Override
    public Page<Treatment> getTreatmentPage(int pageNum, int pageSize, Long animalId, Long diagnosisId) {
        Page<Treatment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Treatment> wrapper = new LambdaQueryWrapper<>();
        
        if (animalId != null) {
            wrapper.eq(Treatment::getAnimalId, animalId);
        }
        if (diagnosisId != null) {
            wrapper.eq(Treatment::getDiagnosisId, diagnosisId);
        }
        
        wrapper.orderByDesc(Treatment::getTreatmentTime);
        return this.page(page, wrapper);
    }
}
