package com.cloudmall.framework.models.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.framework.models.audit.entity.Audit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核日志数据访问接口
 */
@Mapper
public interface AuditMapper extends BaseMapper<Audit> {
}