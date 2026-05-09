package com.cloudmall.framework.models.audit.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Collection;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/7
 */
@Data
public class AuditSubmitDTO<T> {
    private String bizType;
    private Long bizPid;

    @NotEmpty
    private Collection<T> items;

    // ==================== Factory Methods ====================

    /**
     * 创建审核提交DTO
     *
     * @param bizType 业务类型代码
     * @param bizObjects 待审核的业务对象集合
     * @param <T> 业务对象类型
     * @return AuditSubmitDTO实例
     */
    public static <T> AuditSubmitDTO<T> of(String bizType, Collection<T> bizObjects) {
        return of(bizType, null, bizObjects);
    }

    /**
     * 创建审核提交DTO
     *
     * @param bizType 业务类型代码
     * @param bizPid 业务父ID（如秒杀活动ID）
     * @param bizObjects 待审核的业务对象集合
     * @param <T> 业务对象类型
     * @return AuditSubmitDTO实例
     */
    public static <T> AuditSubmitDTO<T> of(String bizType, Long bizPid, Collection<T> bizObjects) {
        AuditSubmitDTO<T> dto = new AuditSubmitDTO<>();
        dto.setBizType(bizType);
        dto.setBizPid(bizPid);
        dto.setItems(bizObjects);
        return dto;
    }
}
