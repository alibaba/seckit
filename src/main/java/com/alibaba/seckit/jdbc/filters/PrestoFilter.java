package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.HttpPathUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

/**
 * reference <a href="https://prestodb.io/docs/current/installation/jdbc.html">Presto JDBC</a>
 */
public class PrestoFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:presto:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "user",
            "password",
            "protocols",
            "applicationNamePrefix",
            "accessToken",
            "timeZoneId",
            "SSL",
            "KerberosRemoteServiceName",
            "KerberosPrincipal",
            "KerberosUseCanonicalHostname",
            "extraCredentials"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "password",
            "KerberosPrincipal"
    ));

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();

    static {
        Pattern headerPattern = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\];]+$");
        propertyPatterns.put("timeZoneId".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\]/+]+$"));
        propertyPatterns.put("extraCredentials".toLowerCase(), headerPattern);
    }

    @Override
    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new HttpPathUrlParser(url);
    }
}
