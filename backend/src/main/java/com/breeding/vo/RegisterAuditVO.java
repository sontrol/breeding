package com.breeding.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterAuditVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String applyRoleCode;
    private String applyRoleName;
    private Integer auditStatus;
    private String auditRemark;
    private Long auditBy;
    private String auditByName;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime auditTime;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
}
