package com.breeding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.LoginUser;
import com.breeding.common.Result;
import com.breeding.dto.treatment.TreatmentAddDTO;
import com.breeding.entity.Animal;
import com.breeding.entity.Diagnosis;
import com.breeding.entity.Treatment;
import com.breeding.entity.TreatmentItem;
import com.breeding.mapper.TreatmentItemMapper;
import com.breeding.service.AnimalService;
import com.breeding.service.DiagnosisService;
import com.breeding.service.InventoryService;
import com.breeding.service.InvalidDataService;
import com.breeding.service.TreatmentService;
import com.breeding.vo.TreatmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/treatment")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private TreatmentItemMapper treatmentItemMapper;

    @Autowired
    private InvalidDataService invalidDataService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private AnimalService animalService;

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
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> add(@RequestBody TreatmentAddDTO dto) {
        Treatment treatment = new Treatment();
        treatment.setDiagnosisId(dto.getDiagnosisId());
        treatment.setAnimalId(dto.getAnimalId());
        treatment.setOperatorId(dto.getOperatorId());
        treatment.setMedicineId(dto.getMedicineId());
        treatment.setTime(dto.getTime());
        treatment.setResult(dto.getResult());

        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            if (dto.getMedicineId() != null && dto.getDosage() != null) {
                TreatmentItem singleItem = new TreatmentItem();
                singleItem.setInventoryId(dto.getMedicineId());
                singleItem.setDosage(dto.getDosage());
                dto.setItems(List.of(singleItem));
            }
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (TreatmentItem item : dto.getItems()) {
                inventoryService.deductInventory(
                    item.getInventoryId(),
                    item.getDosage(),
                    loginUser.getUser().getId(),
                    "治疗消耗 - 动物ID:" + dto.getAnimalId()
                );
            }
        }
        
        boolean saved = treatmentService.save(treatment);
        
        if (saved && dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (TreatmentItem item : dto.getItems()) {
                item.setTreatmentId(treatment.getId());
                treatmentItemMapper.insert(item);
            }
        }

        if (saved && dto.getDiagnosisId() != null) {
            Diagnosis diagnosis = diagnosisService.getById(dto.getDiagnosisId());
            if (diagnosis != null) {
                if (dto.getDiagnosisStatus() != null) {
                    diagnosis.setStatus(dto.getDiagnosisStatus());
                    diagnosisService.updateById(diagnosis);
                }

                if (dto.getAnimalId() != null) {
                    Animal animal = animalService.getById(dto.getAnimalId());
                    if (animal != null) {
                        if (dto.getDiagnosisStatus() != null) {
                            if (dto.getDiagnosisStatus() == 1) {
                                animal.setStatus(1);
                            } else if (dto.getDiagnosisStatus() == 2) {
                                animal.setStatus(4);
                            }
                            animalService.updateById(animal);
                        }
                    }
                }
            }
        }
        
        return saved ? Result.success() : Result.error("治疗记录保存失败");
    }

    @PutMapping("/invalidate/{id}")
    @PreAuthorize("hasAuthority('treatment:invalidate')")
    public Result<Boolean> invalidate(@PathVariable Long id) {
        try {
            LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean success = invalidDataService.invalidate("treatment", id, loginUser.getUser().getId(), loginUser.getUser().getRealName());
            return success ? Result.success() : Result.error("作废治疗失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
