package com.breeding.service;

import java.util.List;
import java.util.Map;

public interface InvalidDataService {

    boolean invalidate(String dataType, Long dataId, Long operatorId, String operatorName);

    boolean restore(String dataType, Long dataId, Long operatorId);

    List<Map<String, Object>> getInvalidDataList(String dataType, int page, int size);

    long countInvalidData(String dataType);
}
