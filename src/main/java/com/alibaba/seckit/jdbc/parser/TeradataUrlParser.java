package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.regex.Pattern;

public class TeradataUrlParser extends DefaultUrlParser {

    public TeradataUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final Pattern URL_PARAMETER =
            Pattern.compile("(())(/(.+))", Pattern.DOTALL);

    @Override
    protected String getParamMarker() {
        return "/";
    }

    @Override
    protected String getParamSeparator() {
        return ",";
    }

    @Override
    protected Pattern getUrlParameterPattern() {
        return URL_PARAMETER;
    }

    @Override
    protected void parseUrlWithoutScheme(String urlSecondPart) throws JdbcURLException {
        int paramIndex = urlSecondPart.indexOf('/');
        String host, additionalParameters = null;
        if (paramIndex < 0) {
            host = urlSecondPart;
        } else {
            host = urlSecondPart.substring(0, paramIndex);
            additionalParameters = urlSecondPart.substring(paramIndex);
        }
        parseHost(host);
        if (paramIndex < urlSecondPart.length() - 1) {
            parseAdditionalParameter(additionalParameters);
        }
    }
}
