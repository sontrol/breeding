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
@TableName("animal")
public class Animal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String earTag;
    private String species;
    private String variety;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate birthDate;
    private Integer gender;
    private Long shedId;
    private Integer status; // 1:健康, 2:患病, 3:隔离, 4:死亡, 5:出栏
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;
}
