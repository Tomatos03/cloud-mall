package com.onlineshop.framework.models.file;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 存储类型转换器
 * 将配置文件中的字符串转换为 StorageType 枚举
 * 支持大小写不敏感：local、LOCAL、Local 都可以转换为 StorageType.LOCAL
 *
 * @author : Tomatos
 * @date : 2026/03/03
 */
@Component
public class StorageTypeConverter implements Converter<String, StorageType> {

    @Override
    public StorageType convert(String source) {
        // 如果源为 null 或空字符串，使用默认值 LOCAL
        if (source.trim().isEmpty()) {
            return StorageType.LOCAL;
        }
        
        try {
            // 将输入转换为大写后匹配枚举值
            return StorageType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 提供友好的错误提示
            String validValues = Arrays.stream(StorageType.values())
                    .map(t -> t.name().toLowerCase())
                    .collect(Collectors.joining(", "));
            
            throw new IllegalArgumentException(
                String.format(
                    "Invalid storage-type: '%s'. Must be one of: %s",
                    source,
                    validValues
                ),
                e
            );
        }
    }
}
