package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.Map;

public class DB2Parser extends ColonSeparatedUrlParser {

    public DB2Parser(String url) throws JdbcURLException {
        super(url);
    }

    @Override
    protected String propertiesToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append(this.getParamSeparator());
        }
        return sb.toString();
    }
}
