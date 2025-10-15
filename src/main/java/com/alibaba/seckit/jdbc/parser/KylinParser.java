package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

/**
 * @author mingyi
 * @date 2024/2/27
 */
public class KylinParser extends DefaultUrlParser {

    public KylinParser(String url) throws JdbcURLException {
        super(url);
    }
    @Override
    protected String getParamSeparator() {
        return ";";
    }
}
