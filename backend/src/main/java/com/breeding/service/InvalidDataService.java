package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.InvalidRecord;

public interface InvalidDataService extends IService<InvalidRecord> {
    Page<InvalidRecord> getInvalidPage(int pageNum, int pageSize, String dataType);

    boolean invalidate(String dataType, Long dataId, Long operatorId, String operatorName);

    boolean restore(Long invalidRecordId);
}
