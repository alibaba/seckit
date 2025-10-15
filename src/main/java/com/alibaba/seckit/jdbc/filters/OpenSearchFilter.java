package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.OpenSearchParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mz.zmy
  */
public class OpenSearchFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:opensearch:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "user",
            "password",
            "fetchSize",
            "auth",
            "region",
            "requestCompression",
            "useSSL",
            "trustSelfSigned",
            "hostnameVerification"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "password"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new OpenSearchParser(url);
    }
}
