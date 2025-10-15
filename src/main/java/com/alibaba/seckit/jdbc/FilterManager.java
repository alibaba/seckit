package com.alibaba.seckit.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterManager {

    private Map<String, Filter> schemaFilterMap = new HashMap<>();

    private List<Filter> specialCaseFilters = new ArrayList<>();

    synchronized public void registerFilter(Filter filter) {
        if (filter.getAcceptedSchemes() == null || filter.getAcceptedSchemes().isEmpty()) {
            specialCaseFilters.add(filter);
        } else {
            for (String scheme : filter.getAcceptedSchemes()) {
                schemaFilterMap.put(scheme, filter);
            }
        }
    }

    public Filter selectFilter(String url) throws JdbcURLException {
        if (url == null) {
            throw new JdbcURLUnsafeException("url should not be null");
        }

        // reverse search, match the last registered filter that match this url
        for (int i = specialCaseFilters.size() - 1; i >= 0; i--) {
            Filter filter = specialCaseFilters.get(i);
            if (filter.acceptURL(url)) {
                return filter;
            }
        }

        int separator = url.indexOf("//");
        if (separator == -1) {
            throw new JdbcURLUnsafeException("parsing url error : '//' is not present in the url " + url);
        }
        String scheme =  url.substring(0, separator).toLowerCase();

        if (schemaFilterMap.get(scheme) != null) {
            return schemaFilterMap.get(scheme);
        }

        if (scheme.equalsIgnoreCase("jdbc:mysql:fabric:") ||
                scheme.regionMatches(true, 0, "jdbc:jcr:jndi:", 0, "jdbc:jcr:jndi:".length())) {
            throw new JdbcURLUnsafeException("protocol is unsafe to use");
        }

        throw new JdbcURLNotSupportedException("scheme  " + scheme + " not supported, url: " + url);
    }
}
