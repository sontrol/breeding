package com.breeding.dto.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertCreateDTO {
    @NotNull(message = "预警类型不能为空")
    private Integer ruleType;

    private Long animalId;
    private Long shedId;
    private Long inventoryId;

    @NotBlank(message = "预警内容不能为空")
    private String alertMsg;
}
