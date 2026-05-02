package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Shed;

public interface ShedService extends IService<Shed> {
    Page<Shed> getShedPage(int page, int size, String name);
    boolean deleteWithCheck(Long id);
}
