package emby.pinyin.fun;

import emby.pinyin.util.PinyinUtils;

/**
 * 首字母，例如 "世界" → "sj"
 */
public class FirstLetterSortName implements BaseSortName {
    @Override
    public String getSortName(String name) {
        return PinyinUtils.getFirstLetter(name, " ").toLowerCase();
    }
}
