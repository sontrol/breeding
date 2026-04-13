package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Symptom;

public interface SymptomService extends IService<Symptom> {
    Page<Symptom> getSymptomPage(int page, int size, Long animalId, Integer status);
}
