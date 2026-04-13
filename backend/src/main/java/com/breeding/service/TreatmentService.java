package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Treatment;

public interface TreatmentService extends IService<Treatment> {
    Page<Treatment> getTreatmentPage(int page, int size, Long animalId, Long diagnosisId);
}
