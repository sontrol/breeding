package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Animal;
import com.breeding.mapper.AnimalMapper;
import com.breeding.service.AnimalService;
import org.springframework.stereotype.Service;

@Service
public class AnimalServiceImpl extends ServiceImpl<AnimalMapper, Animal> implements AnimalService {

    @Override
    public Page<Animal> getAnimalPage(int pageNum, int pageSize, String earTag, Integer status) {
        Page<Animal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<>();
        
        if (earTag != null && !earTag.isEmpty()) {
            wrapper.like(Animal::getEarTag, earTag);
        }
        if (status != null) {
            wrapper.eq(Animal::getStatus, status);
        }
        
        wrapper.orderByDesc(Animal::getCreateTime);
        return this.page(page, wrapper);
    }
}
