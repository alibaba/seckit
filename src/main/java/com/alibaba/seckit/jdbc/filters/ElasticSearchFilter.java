package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.ElasticSearchParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mingyi
  */
public class ElasticSearchFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:es:",
            "jdbc:elasticsearch:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "timezone",
            "connect.timeout",
            "network.timeout",
            "page.size",
            "page.timeout",
            "query.timeout",
            "user",
            "password",
            "ssl",
            "ssl.protocol",
            "field.multi.value.leniency",
            "index.include.frozen",
            "catalog",
            "allow.partial.search.results",
            "debug",
            "validate.properties"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "password",
            "timezone"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = true;

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new ElasticSearchParser(url);
    }
}
