-- ==========================================================
-- 9. AI审计与权限日志
-- ==========================================================
CREATE TABLE ai_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '发起查询的用户ID',
    query_content TEXT NOT NULL COMMENT '用户的原始查询内容',
    accessed_modules VARCHAR(255) COMMENT 'AI访问的模块列表(如: animal, inventory)',
    response_content TEXT COMMENT 'AI返回的完整内容',
    query_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '查询时间'
) COMMENT 'AI助手查询审计日志表';