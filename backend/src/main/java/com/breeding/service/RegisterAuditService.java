package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.dto.user.RegisterAuditDTO;
import com.breeding.vo.RegisterAuditVO;

public interface RegisterAuditService {
    Page<RegisterAuditVO> getPage(int page, int size, String username, String realName, Integer auditStatus, String applyRoleCode);

    void approve(RegisterAuditDTO auditDTO, Long auditorId);

    void reject(RegisterAuditDTO auditDTO, Long auditorId);
}
