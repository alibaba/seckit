package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.DB2Parser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DB2Filter extends ColonSeparatedUrlFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:db2:"
    ));


    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "currentSchema",
            "securityMechanism"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>();

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new DB2Parser(url);
    }
}
