package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.common.BusinessException;
import com.breeding.dto.treatment.TreatmentAddDTO;
import com.breeding.entity.Animal;
import com.breeding.entity.Diagnosis;
import com.breeding.entity.Inventory;
import com.breeding.entity.Treatment;
import com.breeding.entity.TreatmentItem;
import com.breeding.mapper.TreatmentItemMapper;
import com.breeding.mapper.TreatmentMapper;
import com.breeding.service.AnimalService;
import com.breeding.service.DiagnosisService;
import com.breeding.service.InventoryService;
import com.breeding.service.TreatmentService;
import com.breeding.vo.TreatmentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TreatmentServiceImpl extends ServiceImpl<TreatmentMapper, Treatment> implements TreatmentService {

    @Autowired
    private TreatmentItemMapper treatmentItemMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private AnimalService animalService;

    @Override
    public Page<TreatmentVO> getTreatmentPage(int pageNum, int pageSize, Long animalId, Long diagnosisId) {
        Page<Treatment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Treatment> wrapper = new LambdaQueryWrapper<>();
        
        if (animalId != null) {
            wrapper.eq(Treatment::getAnimalId, animalId);
        }
        if (diagnosisId != null) {
            wrapper.eq(Treatment::getDiagnosisId, diagnosisId);
        }
        
        wrapper.orderByDesc(Treatment::getTime);
        Page<Treatment> result = this.page(page, wrapper);
        
        Page<TreatmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        
        Map<Long, List<TreatmentItem>> itemMap;
        if (!result.getRecords().isEmpty()) {
            List<Long> treatmentIds = result.getRecords().stream()
                .map(Treatment::getId)
                .collect(Collectors.toList());
            
            LambdaQueryWrapper<TreatmentItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(TreatmentItem::getTreatmentId, treatmentIds);
            List<TreatmentItem> allItems = treatmentItemMapper.selectList(itemWrapper);
            
            itemMap = allItems.stream()
                .collect(Collectors.groupingBy(TreatmentItem::getTreatmentId));
        } else {
            itemMap = Map.of();
        }
        
        List<TreatmentVO> voList = result.getRecords().stream().map(t -> {
            TreatmentVO vo = new TreatmentVO();
            BeanUtils.copyProperties(t, vo);
            vo.setItems(itemMap.getOrDefault(t.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTreatmentWithItems(TreatmentAddDTO dto, Long operatorId) {
        Treatment treatment = new Treatment();
        treatment.setDiagnosisId(dto.getDiagnosisId());
        treatment.setAnimalId(dto.getAnimalId());
        treatment.setOperatorId(operatorId);
        treatment.setMedicineId(dto.getMedicineId());
        treatment.setTime(dto.getTime());
        treatment.setResult(dto.getResult());

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            if (dto.getMedicineId() != null && dto.getDosage() != null) {
                TreatmentItem singleItem = new TreatmentItem();
                singleItem.setInventoryId(dto.getMedicineId());
                singleItem.setDosage(dto.getDosage());
                // 从库存填充物品名称和类型
                Inventory inv = inventoryService.getById(dto.getMedicineId());
                if (inv != null) {
                    singleItem.setItemName(inv.getItemName());
                    singleItem.setItemType(inv.getItemType());
                }
                dto.setItems(List.of(singleItem));
            }
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (TreatmentItem item : dto.getItems()) {
                if (item == null || item.getInventoryId() == null) {
                    throw new BusinessException("治疗药品明细数据不完整");
                }
                inventoryService.deductInventory(
                    item.getInventoryId(),
                    item.getDosage(),
                    operatorId,
                    "治疗消耗 - 动物ID:" + dto.getAnimalId()
                );
            }
        }
        
        boolean saved = this.save(treatment);
        
        if (saved && dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (TreatmentItem item : dto.getItems()) {
                if (item != null) {
                    item.setTreatmentId(treatment.getId());
                    treatmentItemMapper.insert(item);
                }
            }
        }

        if (saved && dto.getDiagnosisId() != null && dto.getDiagnosisStatus() != null) {
            Diagnosis diagnosis = diagnosisService.getById(dto.getDiagnosisId());
            if (diagnosis != null) {
                diagnosis.setStatus(dto.getDiagnosisStatus());
                diagnosisService.updateById(diagnosis);

                if (dto.getAnimalId() != null) {
                    Animal animal = animalService.getById(dto.getAnimalId());
                    if (animal != null) {
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
        
        return saved;
    }
}
