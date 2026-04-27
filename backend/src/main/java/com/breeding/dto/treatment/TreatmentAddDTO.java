package com.breeding.dto.treatment;

import com.breeding.entity.TreatmentItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TreatmentAddDTO {
    private Long diagnosisId;
    private Long animalId;
    private Long vetId;
    private Long medicineId;
    private BigDecimal dosage;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime treatmentTime;
    private String result;
    private List<TreatmentItem> items;
    private Integer diagnosisStatus;
}
