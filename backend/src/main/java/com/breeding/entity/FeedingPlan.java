package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("feeding_plan")
public class FeedingPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shedId;
    private Long inventoryId;
    private String feedType;
    private BigDecimal amountPerAnimal;
    private Time feedingTime;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;
}
