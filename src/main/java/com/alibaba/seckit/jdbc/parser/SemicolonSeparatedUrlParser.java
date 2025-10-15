package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.regex.Pattern;

public class SemicolonSeparatedUrlParser extends DefaultUrlParser {

    public SemicolonSeparatedUrlParser(String url) throws JdbcURLException {
        super(url);
    }
    private static final Pattern URL_PARAMETER =
            Pattern.compile("(\\/([^;]*))?(;(.+))*", Pattern.DOTALL);

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\]\\\\]+$");

    @Override
    protected Pattern getHostPattern() {
        return HOST_PATTERN;
    }

    @Override
    protected String getParamMarker() {
        return ";";
    }

    @Override
    protected String getParamSeparator() {
        return ";";
    }

    @Override
    protected Pattern getUrlParameterPattern() {
        return URL_PARAMETER;
    }

}
