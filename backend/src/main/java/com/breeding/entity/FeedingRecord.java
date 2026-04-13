package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("feeding_record")
public class FeedingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long shedId;
    private Long operatorId;
    private String feedType;
    private BigDecimal totalAmount;
    private Date executeTime;
    private Date createTime;
}
