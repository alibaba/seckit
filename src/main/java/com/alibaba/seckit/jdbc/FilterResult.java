package com.alibaba.seckit.jdbc;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class FilterResult {

    public FilterResult(String before) {
        this.before = before;
        this.deleted = new HashMap<>();
        this.added = new HashMap<>();
        this.safe = true;
    }

    @Setter
    private boolean safe;

    private final String before;
    @Setter
    private String after;

    private final Map<String, String> deleted;
    private final Map<String, String> added;

    public void propertyDelete(String key, String value) {
        deleted.put(key, value);
        this.safe = false;
    }

    public void propertyAdd(String key, String value) {
        added.put(key, value);
        this.safe = false;
    }

}
