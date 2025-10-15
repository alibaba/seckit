package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.UserPassUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RedisFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:redis:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "user",
            "password",
            "database",
            "connectionTimeout",
            "socketTimeout",
            "blockingSocketTimeout",
            "clientName",
            "ssl",
            "verifyServerCertificate"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user",
            "password"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new UserPassUrlParser(url);
    }
}
