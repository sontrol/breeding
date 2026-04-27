package com.breeding.service;

public interface InvalidDataService {

    boolean invalidate(String dataType, Long dataId, Long operatorId, String operatorName);

    boolean restore(String dataType, Long dataId, Long operatorId);
}
