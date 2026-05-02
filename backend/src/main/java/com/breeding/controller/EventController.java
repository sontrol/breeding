package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.Result;
import com.breeding.entity.Event;
import com.breeding.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('event:view')")
    public Result<Page<Event>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Integer eventType) {
        Page<Event> resultPage = eventService.getEventPage(page, size, animalId, eventType);
        return Result.success(resultPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('event:view')")
    public Result<Event> getById(@PathVariable Long id) {
        return Result.success(eventService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('event:add')")
    public Result<Boolean> add(@Valid @RequestBody Event event) {
        return eventService.save(event) ? Result.success() : Result.error("记录事件失败");
    }
}
