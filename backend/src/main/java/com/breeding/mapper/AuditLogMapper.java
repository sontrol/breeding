package com.breeding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.breeding.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
