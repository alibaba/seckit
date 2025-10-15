package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mz.zmy
  */
public class OpenSearchParser extends DefaultUrlParser {

    public OpenSearchParser(String url) throws JdbcURLException {
        super(url);
    }

    private final static Pattern HTTP_URL_PATTERN = Pattern.compile("^(https?://)(.*)");
    private static final Pattern URL_DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\./%]+$");

    private String connType = "";

    @Override
    protected Pattern getDatabasePattern() {
        return URL_DATABASE_PATTERN;
    }

    @Override
    public void parse() throws JdbcURLException {

        int separator = initialUrl.indexOf("//");
        if (separator == -1) {
            throw new JdbcURLUnsafeException(
                    "url parsing error : '//' is not present in the url " + initialUrl);
        }
        this.scheme = initialUrl.substring(0, separator);
        String urlSecondPart = initialUrl.substring(separator + 2);
        Matcher matcher = HTTP_URL_PATTERN.matcher(urlSecondPart);
        if (matcher.find()) {
            this.connType = matcher.group(1);
            this.parseUrlWithoutScheme(matcher.group(2));
        } else {
            this.parseUrlWithoutScheme(urlSecondPart);
        }
    }


    @Override
    public String toString() {
        return this.scheme + "//" +
                this.connType +
                this.host +
                (this.port > -1 ? ":" + this.port : "") +
                (this.database != null ? "/" + this.database : "/") +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString());
    }
}
