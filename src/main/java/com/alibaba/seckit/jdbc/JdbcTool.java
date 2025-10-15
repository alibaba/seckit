package com.alibaba.seckit.jdbc;


public interface JdbcTool {
    String filterConnectionSource(String url) throws JdbcURLException;
    FilterResult filterConnectionSourceWithResult(String url) throws JdbcURLException;
    void registerFilter(Filter filter);
}
