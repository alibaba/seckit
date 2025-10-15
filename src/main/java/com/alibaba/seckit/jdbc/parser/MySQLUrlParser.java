package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.regex.Pattern;

public class MySQLUrlParser extends DefaultUrlParser {

    public MySQLUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    // mysql 的特殊配置，允许数据库名中存在空格、=、#符号
    private static final Pattern DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-.\\p{script=Han}$ =#]+$");

    @Override
    protected Pattern getDatabasePattern() {
        return DATABASE_PATTERN;
    }
}
