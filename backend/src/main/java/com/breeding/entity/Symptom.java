package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("symptom")
public class Symptom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long animalId;
    private Long observerId;
    private String symptomDesc;
    private Date observeTime;
    private Integer status; // 0:待诊断, 1:已诊断
    private Date createTime;
}
