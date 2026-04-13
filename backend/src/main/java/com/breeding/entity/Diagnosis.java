package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("diagnosis")
public class Diagnosis {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long symptomId;
    private Long animalId;
    private Long vetId;
    private String diseaseName;
    private Integer severity; // 1:轻微, 2:中度, 3:严重
    private Date diagnoseTime;
    private Integer status; // 0:治疗中, 1:已治愈, 2:死亡
    private Date createTime;
}
