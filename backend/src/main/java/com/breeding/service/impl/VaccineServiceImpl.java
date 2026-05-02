package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Vaccine;
import com.breeding.mapper.VaccineMapper;
import com.breeding.service.VaccineService;
import org.springframework.stereotype.Service;

@Service
public class VaccineServiceImpl extends ServiceImpl<VaccineMapper, Vaccine> implements VaccineService {

    @Override
    public Page<Vaccine> getVaccinePage(int pageNum, int pageSize, String name, String targetDisease) {
        Page<Vaccine> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Vaccine> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Vaccine::getName, name);
        }
        if (targetDisease != null && !targetDisease.isEmpty()) {
            wrapper.like(Vaccine::getTargetDisease, targetDisease);
        }
        wrapper.orderByDesc(Vaccine::getCreateTime);
        return this.page(page, wrapper);
    }
}
