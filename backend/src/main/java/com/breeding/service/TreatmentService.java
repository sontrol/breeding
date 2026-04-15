package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Treatment;
import com.breeding.vo.TreatmentVO;

public interface TreatmentService extends IService<Treatment> {
    Page<TreatmentVO> getTreatmentPage(int page, int size, Long animalId, Long diagnosisId);
}
