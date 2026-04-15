package com.breeding.vo;

import com.breeding.entity.TreatmentItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TreatmentVO {
    private Long id;
    private Long diagnosisId;
    private Long animalId;
    private Long vetId;
    private Long medicineId; // 兼容旧数据
    private BigDecimal dosage; // 兼容旧数据
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime treatmentTime;
    private String result;
    private List<TreatmentItem> items; // 治疗明细
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
