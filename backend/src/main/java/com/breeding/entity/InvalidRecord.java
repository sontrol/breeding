package com.breeding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("invalid_record")
public class InvalidRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dataId;
    private String dataType;
    private String moduleName;
    private String displayName;
    private Long deletedBy;
    private String deletedByName;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime deletedTime;
}
