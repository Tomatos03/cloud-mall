
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 消息类型枚举
 *
 * @author : Tomatos
 * @date : 2026/2/17
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    /**
     * 文本消息
     */
    TEXT("TEXT", "文本消息"),

    /**
     * 图片消息
     */
    IMAGE("IMAGE", "图片消息");

    private final String code;
    private final String description;

    public static void check(String code) {
        of(code);
    }

    /**
     * 根据 code 获取消息类型
     *
     * @param code 消息类型代码
     * @return 消息类型枚举
     */
    public static MessageType of(String code) {
        return Arrays.stream(values())
                     .filter(type -> type.code.equals(code))
                     .findFirst()
                     .orElseThrow();
    }

    /**
     * 判断是否为文本消息
     *
     * @return true-文本消息, false-非文本消息
     */
    public boolean isText() {
        return this == TEXT;
    }

    /**
     * 判断是否为图片消息
     *
     * @return true-图片消息, false-非图片消息
     */
    public boolean isImage() {
        return this == IMAGE;
    }
}