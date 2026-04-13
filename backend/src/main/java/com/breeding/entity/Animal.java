package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("animal")
public class Animal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String earTag;
    private String species;
    private String variety;
    private Date birthDate;
    private Integer gender;
    private Long shedId;
    private Integer status; // 1:健康, 2:患病, 3:隔离, 4:死亡, 5:出栏
    private Date createTime;
    private Date updateTime;
}
