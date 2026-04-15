package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("diagnosis")
public class Diagnosis {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long symptomId;
    private Long animalId;
    private Long vetId;
    private String diseaseName;
    private Integer severity; // 严重程度
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime diagnoseTime;
    private Integer status; // 状态
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
