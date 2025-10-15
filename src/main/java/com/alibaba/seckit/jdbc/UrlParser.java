package com.alibaba.seckit.jdbc;

import java.util.Map;

public interface UrlParser {

    String getInitialUrl();

    void parse() throws JdbcURLException;

    Map<String, String> getProperties();
}
