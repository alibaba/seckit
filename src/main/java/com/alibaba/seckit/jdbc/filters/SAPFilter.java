package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.DefaultUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SAPFilter extends DefaultFilter {

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\];]+$");

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:sap:"
    ));
    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "reconnect",
            "databaseName",
            "currentSchema",
            "trustServerCertificate"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>();

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        DefaultUrlParser parser = new DefaultUrlParser(url);
        parser.setHostPattern(HOST_PATTERN);
        return parser;
    }

    @Getter
    @Setter
    private boolean ignoreCase = true;

}
