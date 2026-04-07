package emby.pinyin.enums;

import emby.pinyin.fun.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PinyinMode {
    PINYIN(PinyinSortName.class),
    FIRST_LETTER(FirstLetterSortName.class),
    PREFIX(PrefixSortName.class),
    DEFAULT(DefaultSortName.class);

    @Getter
    private final Class<? extends BaseSortName> aClass;
}
