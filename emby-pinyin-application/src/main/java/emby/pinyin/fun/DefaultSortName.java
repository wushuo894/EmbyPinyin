package emby.pinyin.fun;

public class DefaultSortName implements BaseSortName {
    @Override
    public String getSortName(String name) {
        return name;
    }
}
