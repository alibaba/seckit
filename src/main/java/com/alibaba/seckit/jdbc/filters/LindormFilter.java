package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.LindormLikeUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LindormFilter extends DefaultFilter {

    @Getter
    @Setter
    protected Set<String> acceptedSchemes = new HashSet<>();

    @Getter
    @Setter
    protected Set<String> internalAcceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:lindorm:table:",
            "jdbc:lindorm:search:",
            "jdbc:lindorm:phoenix:",
            "jdbc:lindorm:analytics:",
            "jdbc:lindorm:tsdb:"
    ));

    @Override
    public boolean acceptURL(String url) {
        if (url == null) {
            return false;
        }
        for (String scheme :
                getInternalAcceptedSchemes()) {
            if (url.startsWith(scheme)) {
                return true;
            }
        }
        return false;
    }

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            // "factory",
            "schema",
            "timeZone",
            "url",
            "serialization",
            "authentication",
            "avatica_user",
            "avatica_password",
            "hostname_verification",
            "retryWhenMissConnection",
            "avatica.statement.retries",
            "user",
            "username",
            "password",
            "database",
            "failover",
            "maxRetries",
            "interval",
            "maxDelay",
            "lindorm.tsdb.driver.connect.timeout",
            "lindorm.tsdb.driver.connection.request.timeout",
            "lindorm.tsdb.driver.socket.timeout",
            "lindorm.tsdb.driver.http.compression"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "url", "timeZone", "avatica_password", "avatica_user", "user", "username", "password"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new LindormLikeUrlParser(url);
    }
}
