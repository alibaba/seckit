package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.PostgresParser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

// https://jdbc.postgresql.org/documentation/use/
public class PostgresFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:postgresql:",
            "jdbc:kingbase8:",
            "jdbc:polardb:",
            "jdbc:opengauss:"
    ));

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();

    static {
        propertyPatterns.put("conf:ststoken", Pattern.compile("^[a-zA-Z0-9/+=]+$"));
    }

    @Override
    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "stringtype",
            "currentSchema",
            "tcpKeepAlive",
            "connectTimeout",
            "password",
            "user",
            "sslmode",
            "charSet",
            "reWriteBatchedInserts",
            "preferQueryMode",
            "socketTimeout",
            "oracleCase",
            "ApplicationName",
            "ssl",
            "loadBalanceHosts",
            "hostRecheckSeconds",
            "targetServerType",
            "readOnlyMode",
            "readOnly",
            "tcpNoDelay",
            "cancelSignalTimeout",
            "loginTimeout",
            "autosave",

            // polardb params
            "skipQuotesOnReturning",
            "allowMultipleQueryPerStatement",
            "polarComp",
            "autoCommit",
            "extraFloatDigits",
            "namedParam",
            "supportRowId",
            "swallowConnectException",
            "autoCommitSpecCompliant",
            "unnamedProc",
            "unknownDateType",
            "boolAsInt",
            "compMode",
            "mapDateToTimestamp",
            "skipCallFlushIfDeadlockRisk",
            "resetNlsFormat",
            "collectWarning",
            "defaultRowFetchSize",
            "defaultPolarMaxFetchSize",
            "polarFetchCrossCommit",
            "polarMaxPortals",
            "forceDriverType",
            "conf:stsToken"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new PostgresParser(url);
    }

}
