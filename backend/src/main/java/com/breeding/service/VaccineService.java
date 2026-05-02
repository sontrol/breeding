package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Vaccine;

public interface VaccineService extends IService<Vaccine> {
    Page<Vaccine> getVaccinePage(int page, int size, String name, String targetDisease);
}
