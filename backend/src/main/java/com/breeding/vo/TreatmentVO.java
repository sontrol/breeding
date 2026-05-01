package com.breeding.vo;

import com.breeding.entity.TreatmentItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TreatmentVO {
    private Long id;
    private Long diagnosisId;
    private Long animalId;
    private Long operatorId;
    private Long medicineId;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime time;
    private String result;
    private List<TreatmentItem> items;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
