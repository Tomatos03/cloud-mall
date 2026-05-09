package com.cloudmall.framework.models.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息日志数据映射接口
 *
 * @author : Tomatos
 * @date : 2026/1/25
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageLog> {
}
