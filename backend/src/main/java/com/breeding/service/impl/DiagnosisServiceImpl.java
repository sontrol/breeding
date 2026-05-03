package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Animal;
import com.breeding.entity.Diagnosis;
import com.breeding.entity.Symptom;
import com.breeding.mapper.DiagnosisMapper;
import com.breeding.service.AnimalService;
import com.breeding.service.DiagnosisService;
import com.breeding.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class DiagnosisServiceImpl extends ServiceImpl<DiagnosisMapper, Diagnosis> implements DiagnosisService {

    @Autowired
    private SymptomService symptomService;

    @Autowired
    private AnimalService animalService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addWithDiagnosis(Diagnosis diagnosis) {
        if (diagnosis.getDiagnoseTime() == null) {
            diagnosis.setDiagnoseTime(LocalDateTime.now());
        }
        boolean saved = super.save(diagnosis);
        if (saved) {
            if (diagnosis.getSymptomId() != null) {
                Symptom symptom = symptomService.getById(diagnosis.getSymptomId());
                if (symptom != null) {
                    symptom.setStatus(1);
                    symptomService.updateById(symptom);
                }
            }
            if (diagnosis.getAnimalId() != null) {
                Animal animal = animalService.getById(diagnosis.getAnimalId());
                // 仅在动物为"健康"(1)时才改为"患病"(2)；隔离(3)/死亡(4)/出栏(5)保持原状态
                if (animal != null && animal.getStatus() == 1) {
                    animal.setStatus(2);
                    animalService.updateById(animal);
                }
            }
        }
        return saved;
    }
}
