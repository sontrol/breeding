package com.breeding.vo;

import com.breeding.entity.Animal;
import com.breeding.entity.Symptom;
import com.breeding.entity.Treatment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnimalDetailVO {
    private Animal animal;
    private boolean canViewDiseaseRecords;
    private List<Symptom> symptomList = new ArrayList<>();
    private List<Treatment> treatmentList = new ArrayList<>();
}
