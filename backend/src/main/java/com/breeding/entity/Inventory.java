﻿package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String itemName;
    private Integer itemType; // 1:饲料, 2:药品, 3:疫苗, 4:器械
    private String batchNumber;
    private BigDecimal quantity;
    private String unit;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate produceDate;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate expireDate;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
