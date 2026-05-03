package com.breeding.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterAuditDTO {
    @NotNull(message = "注册申请ID不能为空")
    private Long registerId;

    private String auditRemark;
}
