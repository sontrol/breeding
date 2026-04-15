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
    private Long treatmentId; // 关联治疗记录ID
    private Long inventoryId; // 关联库存ID
    private String itemName; // 物品名称（冗余存储）
    private Integer itemType; // 1:饲料, 2:药品, 3:疫苗, 4:器械
    private BigDecimal dosage; // 使用剂量/数量
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
