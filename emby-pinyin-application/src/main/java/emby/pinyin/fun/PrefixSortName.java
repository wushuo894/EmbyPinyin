package emby.pinyin.fun;

import cn.hutool.core.util.StrUtil;
import emby.pinyin.util.PinyinUtils;

/**
 * 前置字母，例如 "世界" → "s_世界"
 */
public class PrefixSortName implements BaseSortName {
    @Override
    public String getSortName(String name) {
        String first = PinyinUtils.getFirstLetter(name, "");
        String initial = StrUtil.isNotBlank(first)
                ? first.substring(0, 1).toLowerCase()
                : "";
        return initial + "_" + name;
    }
}
