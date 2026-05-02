package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.VaccineRecord;
import com.breeding.mapper.VaccineRecordMapper;
import com.breeding.service.VaccineRecordService;
import org.springframework.stereotype.Service;

@Service
public class VaccineRecordServiceImpl extends ServiceImpl<VaccineRecordMapper, VaccineRecord> implements VaccineRecordService {

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
}
