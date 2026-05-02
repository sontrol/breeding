package com.breeding.controller;

import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.service.InvalidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invalid-data")
public class InvalidDataController {

    private static final Logger log = LoggerFactory.getLogger(InvalidDataController.class);

    private final InvalidDataService invalidDataService;

    public InvalidDataController(InvalidDataService invalidDataService) {
        this.invalidDataService = invalidDataService;
    }

    private static final List<String> ALL_DATA_TYPES = List.of(
            "animal", "feeding_plan", "feeding_record", "symptom",
            "diagnosis", "treatment", "inventory", "alert");

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:invalid:view')")
    public Result<Map<String, Object>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String dataType) {

        List<Map<String, Object>> allRecords = new ArrayList<>();
        List<String> typesToQuery = (dataType != null && !dataType.isEmpty())
                ? List.of(dataType) : ALL_DATA_TYPES;

        for (String dt : typesToQuery) {
            try {
                allRecords.addAll(invalidDataService.getInvalidDataList(dt, 1, 1000));
            } catch (RuntimeException e) {
                log.warn("查询作废数据失败, dataType={}", dt, e);
            }
        }

        int total = allRecords.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<Map<String, Object>> pageRecords = start < total ? allRecords.subList(start, end) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageRecords);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        return Result.success(result);
    }

    @PutMapping("/restore")
    @PreAuthorize("hasAuthority('system:invalid:restore')")
    public Result<Boolean> restore(@RequestParam String dataType, @RequestParam Long dataId) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return invalidDataService.restore(dataType, dataId, loginUser.getUser().getId()) ? Result.success() : Result.error("恢复失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
