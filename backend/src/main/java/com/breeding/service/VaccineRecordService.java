package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.VaccineRecord;

public interface VaccineRecordService extends IService<VaccineRecord> {
    boolean addWithEvent(VaccineRecord vaccineRecord);

    Page<VaccineRecord> getRecordPage(int page, int size, Long animalId, Long vaccineId);
}
