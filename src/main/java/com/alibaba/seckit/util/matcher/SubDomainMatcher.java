package com.alibaba.seckit.util.matcher;

import java.util.ArrayList;
import java.util.List;

public class SubDomainMatcher extends BasicSuffixMatcher {
    public SubDomainMatcher(String[] patterns) {
        super(expandPatterns(patterns), true);
    }

    private static String[] expandPatterns(String[] patterns) {
        if (patterns == null) {
            return null;
        }
        List<String> expanded = new ArrayList<>();
        for (String pattern : patterns) {
            expanded.add(pattern);
            if (pattern != null && pattern.startsWith("*.")) {
                expanded.add(pattern.substring(2));
            }
        }
        return expanded.toArray(new String[0]);
    }
}
