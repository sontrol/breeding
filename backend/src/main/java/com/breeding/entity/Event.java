package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("event")
public class Event {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long animalId;
    private String eventType; 
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime eventTime;
    private Long operatorId;
    private String description;
    private Long relatedId;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
