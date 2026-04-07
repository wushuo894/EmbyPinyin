package emby.pinyin.enums;

public enum PinyinMode {
    PINYIN,         // 全拼，例如 "世界" → "shijie"
    FIRST_LETTER,   // 首字母，例如 "世界" → "sj"
    PREFIX,         // 前置字母，例如 "世界" → "s_世界"
    DEFAULT         // Emby默认，例如 "世界" → "世界"（使用原始名称并锁定）
}
