package com.onlineshop.framework.config.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/10
 */
public class LongIdToStringModule extends SimpleModule {
    public LongIdToStringModule() {
        super("LongIdToStringModule");
        this.setSerializerModifier(new LongIdToStringSerializerModifier());
    }

    static class LongIdToStringSerializerModifier extends BeanSerializerModifier {
        @Override
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
            for (BeanPropertyWriter writer : beanProperties) {
                if (isIdField(writer)) {
                    // 将该字段的序列化器设置为 ToStringSerializer
                    writer.assignSerializer(ToStringSerializer.instance);
                }
            }
            return beanProperties;
        }

        private boolean isIdField(BeanPropertyWriter writer) {
            String name = writer.getName()
                                .toLowerCase();
            Class<?> type = writer.getType()
                                  .getRawClass();
            // 字段名包含"id"且类型为Long/long
            return name.contains("id") && (type == Long.class || type == long.class);
        }
    }
}
