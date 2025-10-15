package com.alibaba.seckit.jdbc;

import java.util.Set;

public interface Filter {
    Set<String> getAcceptedSchemes();

    Set<String> getAcceptedPropertyKeys();
    void setAcceptedPropertyKeys(Set<String> propertyKeys);
    void addAcceptedPropertyKey(String... keys);

    Set<String> getPropertyKeyWhiteList();
    void setPropertyKeyWhiteList(Set<String> propertyKeyWhiteList);
    void addPropertyKeyWhiteList(String... keys);

    // alternative method for special case when acceptedSchemes can not be used.
    boolean acceptURL(String url);

    UrlParser createUrlParser(String url) throws JdbcURLException;

    String filterProperties(String url) throws JdbcURLException;

    FilterResult filterPropertiesWithResult(String url) throws JdbcURLException;

    FilterResult checkAndFilterProperties(UrlParser parser);

}
