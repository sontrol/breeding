package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.BusinessException;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.dto.treatment.TreatmentAddDTO;
import com.breeding.service.InvalidDataService;
import com.breeding.service.TreatmentService;
import com.breeding.vo.TreatmentVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/treatment")
public class TreatmentController {

    private static final Logger log = LoggerFactory.getLogger(TreatmentController.class);

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private InvalidDataService invalidDataService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('disease:view')")
    public Result<Page<TreatmentVO>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Long diagnosisId) {
        Page<TreatmentVO> resultPage = treatmentService.getTreatmentPage(page, size, animalId, diagnosisId);
        return Result.success(resultPage);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('treatment:add')")
    public Result<Boolean> add(@Valid @RequestBody TreatmentAddDTO dto) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean saved = treatmentService.addTreatmentWithItems(dto, loginUser.getUser().getId());
            return saved ? Result.success() : Result.error("治疗记录保存失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("治疗记录保存异常", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('treatment:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("treatment", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废治疗失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("作废治疗异常", e);
            return Result.error("系统异常，请稍后重试");
        }
    }
}
