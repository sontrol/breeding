package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("event")
public class Event {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long animalId;
    private String eventType; // feeding, disease, treatment, vaccine, death, sale, transfer
    private Date eventTime;
    private Long operatorId;
    private String description;
    private Long relatedId;
    private Date createTime;
}
