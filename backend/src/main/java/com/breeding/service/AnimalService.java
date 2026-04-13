package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Animal;

public interface AnimalService extends IService<Animal> {
    Page<Animal> getAnimalPage(int page, int size, String earTag, Integer status);
}
