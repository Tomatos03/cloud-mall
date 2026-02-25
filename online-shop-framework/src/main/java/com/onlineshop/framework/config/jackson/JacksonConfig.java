package com.onlineshop.framework.config.jackson;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/22
 */
@Configuration
public class JacksonConfig {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            builder.serializerByType(
                    LocalDateTime.class,
                    new LocalDateTimeSerializer(formatter)
            );
            // 添加 BigDecimal 序列化为字符串
            builder.serializerByType(
                    BigDecimal.class,
                    new ToStringSerializer()
            );
            // 在已有模块上添加 LongIdToStringModule
            builder.modulesToInstall(new LongIdToStringModule());
        };
    }
}