package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

/**
 * @author mingyi
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
