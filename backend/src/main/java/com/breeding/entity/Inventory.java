package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

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
    private Date produceDate;
    private Date expireDate;
    private Date createTime;
}
