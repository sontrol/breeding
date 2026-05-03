package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Event;
import com.breeding.entity.Vaccine;
import com.breeding.entity.VaccineRecord;
import com.breeding.mapper.VaccineRecordMapper;
import com.breeding.service.EventService;
import com.breeding.service.VaccineRecordService;
import com.breeding.service.VaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VaccineRecordServiceImpl extends ServiceImpl<VaccineRecordMapper, VaccineRecord> implements VaccineRecordService {

    @Autowired
    private EventService eventService;

    @Autowired
    private VaccineService vaccineService;

    @Override
    public Page<VaccineRecord> getRecordPage(int pageNum, int pageSize, Long animalId, Long vaccineId) {
        Page<VaccineRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<VaccineRecord> wrapper = new LambdaQueryWrapper<>();
        if (animalId != null) {
            wrapper.eq(VaccineRecord::getAnimalId, animalId);
        }
        if (vaccineId != null) {
            wrapper.eq(VaccineRecord::getVaccineId, vaccineId);
        }
        wrapper.orderByDesc(VaccineRecord::getTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addWithEvent(VaccineRecord vaccineRecord) {
        boolean saved = super.save(vaccineRecord);
        if (saved) {
            createVaccineEvent(vaccineRecord);
        }
        return saved;
    }

    private void createVaccineEvent(VaccineRecord record) {
        String vaccineName = "";
        if (record.getVaccineId() != null) {
            Vaccine vaccine = vaccineService.getById(record.getVaccineId());
            if (vaccine != null) {
                vaccineName = vaccine.getName();
            }
        }
        Event event = new Event();
        event.setAnimalId(record.getAnimalId());
        event.setEventType(4); // 疫苗
        event.setEventTime(record.getTime() != null ? record.getTime() : LocalDateTime.now());
        event.setOperatorId(record.getOperatorId());
        event.setDescription("疫苗接种 - 动物ID:" + record.getAnimalId() + ", 疫苗:" + vaccineName + ", 批号:" + record.getBatchNumber());
        event.setRelatedId(record.getId());
        eventService.save(event);
    }
}
