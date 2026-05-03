package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Event;
import com.breeding.entity.Symptom;
import com.breeding.mapper.SymptomMapper;
import com.breeding.service.EventService;
import com.breeding.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SymptomServiceImpl extends ServiceImpl<SymptomMapper, Symptom> implements SymptomService {

    @Autowired
    private EventService eventService;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean addWithEvent(Symptom symptom) {
        boolean saved = super.save(symptom);
        if (saved) {
            createDiseaseEvent(symptom);
        }
        return saved;
    }

    private void createDiseaseEvent(Symptom symptom) {
        Event event = new Event();
        event.setAnimalId(symptom.getAnimalId());
        event.setEventType(2); // 疾病
        event.setEventTime(symptom.getObserveTime() != null ? symptom.getObserveTime() : LocalDateTime.now());
        event.setOperatorId(symptom.getObserverId());
        event.setDescription("症状上报 - 动物ID:" + symptom.getAnimalId() + ", 症状:" + symptom.getSymptomDesc());
        event.setRelatedId(symptom.getId());
        eventService.save(event);
    }
}
