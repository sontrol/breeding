package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.Animal;
import com.breeding.entity.Symptom;
import com.breeding.entity.Treatment;
import com.breeding.mapper.AnimalMapper;
import com.breeding.mapper.SymptomMapper;
import com.breeding.mapper.TreatmentMapper;
import com.breeding.service.AnimalService;
import com.breeding.vo.AnimalDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AnimalServiceImpl extends ServiceImpl<AnimalMapper, Animal> implements AnimalService {

    @Autowired
    private SymptomMapper symptomMapper;

    @Autowired
    private TreatmentMapper treatmentMapper;

    @Override
    public Page<Animal> getAnimalPage(int pageNum, int pageSize, String earTag, Integer status) {
        Page<Animal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Animal> wrapper = new LambdaQueryWrapper<>();
        
        if (earTag != null && !earTag.isEmpty()) {
            wrapper.like(Animal::getEarTag, earTag);
        }
        if (status != null) {
            wrapper.eq(Animal::getStatus, status);
        }
        
        wrapper.orderByDesc(Animal::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public AnimalDetailVO getAnimalDetail(Long id) {
        Animal animal = this.getById(id);
        AnimalDetailVO detail = new AnimalDetailVO();
        detail.setAnimal(animal);

        if (animal == null) {
            detail.setSymptomList(Collections.emptyList());
            detail.setTreatmentList(Collections.emptyList());
            return detail;
        }

        boolean canViewDiseaseRecords = canViewDiseaseRecords();
        detail.setCanViewDiseaseRecords(canViewDiseaseRecords);

        if (!canViewDiseaseRecords) {
            detail.setSymptomList(Collections.emptyList());
            detail.setTreatmentList(Collections.emptyList());
            return detail;
        }

        List<Symptom> symptomList = symptomMapper.selectList(
                new LambdaQueryWrapper<Symptom>()
                        .eq(Symptom::getAnimalId, id)
                        .orderByDesc(Symptom::getObserveTime)
        );
        List<Treatment> treatmentList = treatmentMapper.selectList(
                new LambdaQueryWrapper<Treatment>()
                        .eq(Treatment::getAnimalId, id)
                        .orderByDesc(Treatment::getTime)
        );

        detail.setSymptomList(symptomList);
        detail.setTreatmentList(treatmentList);
        return detail;
    }

    private boolean canViewDiseaseRecords() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(authority ->
                "disease:view".equals(authority.getAuthority())
                        || "system:*".equals(authority.getAuthority())
                        || "ROLE_admin".equals(authority.getAuthority())
        );
    }
}
