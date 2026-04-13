package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Diagnosis;
import com.breeding.mapper.DiagnosisMapper;
import com.breeding.service.DiagnosisService;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisServiceImpl extends ServiceImpl<DiagnosisMapper, Diagnosis> implements DiagnosisService {

    @Override
    public Page<Diagnosis> getDiagnosisPage(int pageNum, int pageSize, Long animalId, String diseaseName) {
        Page<Diagnosis> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Diagnosis> wrapper = new LambdaQueryWrapper<>();
        
        if (animalId != null) {
            wrapper.eq(Diagnosis::getAnimalId, animalId);
        }
        if (diseaseName != null && !diseaseName.isEmpty()) {
            wrapper.like(Diagnosis::getDiseaseName, diseaseName);
        }
        
        wrapper.orderByDesc(Diagnosis::getDiagnoseTime);
        return this.page(page, wrapper);
    }
}
