package com.cloudmall.framework.models.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.framework.models.audit.entity.AuditItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核项目 Mapper
 */
@Mapper
public interface AuditItemMapper extends BaseMapper<AuditItem> {

}
