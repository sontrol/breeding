package com.breeding.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterAuditDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String auditRemark;
}
