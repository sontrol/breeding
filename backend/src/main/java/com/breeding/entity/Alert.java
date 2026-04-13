package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("alert")
public class Alert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleType; // medicine_expire, no_food_long, death_rate_high
    private Long targetId;
    private String alertMsg;
    private Integer status; // 0:未处理, 1:已处理
    private Date createTime;
    private Date handleTime;
    private Long handlerId;
}
