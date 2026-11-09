package com.alibaba.seckit.util.matcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BasicSuffixMatcher implements StringMatcher {

    private final Set<String> set;
    private final List<String> suffixList;
    private final boolean caseInsensitive;

    public BasicSuffixMatcher(String[] patterns) {
        this(patterns, false);
    }

    protected BasicSuffixMatcher(String[] patterns, boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        set = new HashSet<>();
        suffixList = new ArrayList<>();
        parsePatterns(patterns);
    }

    private void parsePatterns(String[] patterns) {
        if (patterns == null) {
            return;
        }
        for (String pattern : patterns) {
            if (pattern == null) {
                continue;
            }
            if (caseInsensitive) {
                pattern = pattern.toLowerCase(Locale.ROOT);
            }
            if (pattern.startsWith("*")) {
                suffixList.add(pattern.substring(1));
            } else {
                set.add(pattern);
            }
        }
    }

    @Override
    public boolean matches(String str) {
        if (str == null) {
            return false;
        }
        if (caseInsensitive) {
            str = str.toLowerCase(Locale.ROOT);
        }
        if (set.contains(str)) {
            return true;
        }
        for (String suffix : suffixList) {
            if (str.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
