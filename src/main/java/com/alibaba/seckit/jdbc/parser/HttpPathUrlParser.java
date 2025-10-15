package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.regex.Pattern;

public class HttpPathUrlParser extends DefaultUrlParser {

    public HttpPathUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final Pattern databasePattern = Pattern.compile("^[a-zA-Z0-9_\\-\\./]+$");

    @Override
    protected Pattern getDatabasePattern() {
        return databasePattern;
    }
}
