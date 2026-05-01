package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.Event;

public interface EventService extends IService<Event> {
    Page<Event> getEventPage(int page, int size, Long animalId, Integer eventType);
}
