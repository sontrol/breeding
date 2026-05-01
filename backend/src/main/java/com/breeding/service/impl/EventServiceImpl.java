package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Event;
import com.breeding.mapper.EventMapper;
import com.breeding.service.EventService;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

    @Override
    public Page<Event> getEventPage(int pageNum, int pageSize, Long animalId, Integer eventType) {
        Page<Event> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<>();
        
        if (animalId != null) {
            wrapper.eq(Event::getAnimalId, animalId);
        }
        if (eventType != null) {
            wrapper.eq(Event::getEventType, eventType);
        }
        
        wrapper.orderByDesc(Event::getEventTime);
        return this.page(page, wrapper);
    }
}
