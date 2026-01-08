package com.onlineshop.framework.models.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlineshop.framework.models.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核日志数据访问接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}