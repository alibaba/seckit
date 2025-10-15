package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parse jdbc url for db2/informix, format jdbc:scheme://host[:port]/database:[key=value][key2=value2]
 */
public class ColonSeparatedUrlParser extends DefaultUrlParser {

    public ColonSeparatedUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final Pattern URL_PARAMETER =
            Pattern.compile("(\\/([^:]*))?(:(.+))*", Pattern.DOTALL);

    @Getter
    private final Set<String> acceptedSchemes = new HashSet<>();

    @Override
    protected String getParamMarker() {
        return ":";
    }

    @Override
    protected String getParamSeparator() {
        return ";";
    }

    @Override
    protected Pattern getUrlParameterPattern() {
        return URL_PARAMETER;
    }

    @Override
    protected void parseUrlWithoutScheme(String urlSecondPart) throws JdbcURLException {
        int dbIndex = urlSecondPart.indexOf("/");
        if (dbIndex == -1) {
            throw new JdbcURLUnsafeException("Invalid URL: " + urlSecondPart + " no database provided");
        }
        String hostAddressesString = urlSecondPart.substring(0, dbIndex);
        String additionalParameters = urlSecondPart.substring(dbIndex);

        this.parseHost(hostAddressesString);
        this.parseAdditionalParameter(additionalParameters);
    }
}
