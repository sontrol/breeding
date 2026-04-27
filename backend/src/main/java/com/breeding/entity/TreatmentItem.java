package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@TableName("treatment_item")
public class TreatmentItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long treatmentId;
    private Long inventoryId;
    private String itemName;
    private Integer itemType;
    private BigDecimal dosage;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
