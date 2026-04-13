package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Diagnosis;

public interface DiagnosisService extends IService<Diagnosis> {
    Page<Diagnosis> getDiagnosisPage(int page, int size, Long animalId, String diseaseName);
}
