package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("treatment")
public class Treatment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long diagnosisId;
    private Long animalId;
    private Long vetId;
    private Long medicineId;
    private BigDecimal dosage;
    private Date treatmentTime;
    private String result;
    private Date createTime;
}
