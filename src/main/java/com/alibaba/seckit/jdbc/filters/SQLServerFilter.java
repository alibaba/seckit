package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.SQLServerUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

public class SQLServerFilter extends DefaultFilter {

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new SQLServerUrlParser(url);
    }

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:sqlserver:"
    ));
    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "trustServerCertificate",
            "user",
            "database",
            "ApplicationIntent",
            "schemaName",
            "instanceName",
            "databaseName",
            "encrypt",
            "password",
            "loginTimeout",
            "socketTimeout",
            "queryTimeout",
            "language",
            "hostNameInCertificate",
            "sendStringParametersAsUnicode",
            "failoverPartner"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = true;

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();

    static {
        final Pattern wordsPattern = Pattern.compile("^[a-zA-Z0-9_\\-\\.\\p{script=Han}$@\\[\\]{}() +]+$");
        propertyPatterns.put("database".toLowerCase(), wordsPattern);
        propertyPatterns.put("databaseName".toLowerCase(), wordsPattern);
        propertyPatterns.put("schemaName".toLowerCase(), wordsPattern);
    }

    public Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

}
