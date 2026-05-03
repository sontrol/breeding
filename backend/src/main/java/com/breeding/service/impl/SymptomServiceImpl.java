package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Symptom;
import com.breeding.mapper.SymptomMapper;
import com.breeding.service.SymptomService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SymptomServiceImpl extends ServiceImpl<SymptomMapper, Symptom> implements SymptomService {

    @Override
    public Page<Symptom> getSymptomPage(int pageNum, int pageSize, Long animalId, Integer status) {
        Page<Symptom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Symptom> wrapper = new LambdaQueryWrapper<>();

        if (animalId != null) {
            wrapper.eq(Symptom::getAnimalId, animalId);
        }
        if (status != null) {
            wrapper.eq(Symptom::getStatus, status);
        }

        wrapper.orderByDesc(Symptom::getObserveTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean save(Symptom symptom) {
        if (symptom.getObserveTime() == null) {
            symptom.setObserveTime(LocalDateTime.now());
        }
        return super.save(symptom);
    }
}
