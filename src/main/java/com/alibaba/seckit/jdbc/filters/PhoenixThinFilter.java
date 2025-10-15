package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.PhoenixThinUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mingyi
 * @date 2024/4/11
 */
public class PhoenixThinFilter extends LindormFilter {
    @Getter
    @Setter
    private Set<String> internalAcceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:phoenix:thin:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "url",
            "serialization",
            "authentication",
            "timeZone",
            "avatica_user",
            "avatica_password",
            "fetch_size",
            "http_connection_timeout",
            "http_response_timeout",
            "user",
            "password"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "url", "timeZone", "avatica_password", "avatica_user", "user", "password"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new PhoenixThinUrlParser(url);
    }
}
