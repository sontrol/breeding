package com.breeding.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;

@Data
public class FeedingPlanVO {
    private Long id;
    private Long shedId;
    private Long inventoryId;
    private String feedType;
    private String batchNumber; // 库存批次号
    private BigDecimal amountPerAnimal;
    private Time feedingTime;
    private Integer status;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
