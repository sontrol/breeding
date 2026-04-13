package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

@Data
@TableName("feeding_plan")
public class FeedingPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shedId;
    private String feedType;
    private BigDecimal amountPerAnimal;
    private Time feedingTime;
    private Integer status; // 1:启用, 0:停用
    private Date createTime;
}
