package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank(message = "物品名称不能为空")
    private String itemName;
    @NotNull(message = "物品分类不能为空")
    private Integer itemType;
    @NotBlank(message = "批次号不能为空")
    private String batchNumber;
    private BigDecimal quantity;
    private String unit;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate produceDate;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate expireDate;
    @TableLogic
    private Integer deleted;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;
}
