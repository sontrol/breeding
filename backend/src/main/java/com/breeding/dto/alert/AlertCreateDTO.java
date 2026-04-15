package com.breeding.dto.alert;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlertCreateDTO {
    @NotBlank(message = "预警类型不能为空")
    private String ruleType;

    private Long targetId;

    @NotBlank(message = "预警内容不能为空")
    private String alertMsg;
}
