package com.breeding.dto.treatment;

import com.breeding.entity.TreatmentItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TreatmentAddDTO {
    @NotNull(message = "诊断ID不能为空")
    private Long diagnosisId;
    @NotNull(message = "动物ID不能为空")
    private Long animalId;
    private Long operatorId;
    private Long medicineId;
    private BigDecimal dosage;
    @NotNull(message = "治疗时间不能为空")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime time;
    private String result;
    @Valid
    private List<TreatmentItem> items;
    private Integer diagnosisStatus;
}
