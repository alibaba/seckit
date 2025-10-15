package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

public class ImpalaFilter extends SemicolonSeparatedUrlFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:impala:"
    ));
    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "UID",
            "PWD",
            "AuthMech",
            "KrbRealm",
            "KrbHostFQDN",
            "KrbServiceName",
            "LoginTimeout",
            "Auth_Client_ID",
            "Auth_Client_Secret",
            "Auth_Scope",
            "Auth_Token_Expiry_Buffer",
            "Auth_AccessToken",
            "Auth_Flow",
            "MaskAccessTokenClientInfoValue",
            "ssl",
            "CAIssuedCertNamesMismatch",
            "AllowSelfSignedCerts",
            "AllowAllHostNames",
            "SubjectAlternativeNamesHostNames",
            "TemporarilyUnavailableRetry",
            "RateLimitRetry",
            "HTTPRetry",
            "HttpEmulatedError",
            "IgnoreTransactions",

            // some properties in HiveJDBCPropertyKey
            "ConnSchema",
            "ConnCatalog",
            "DefaultStringColumnLength",
            "DelegationUID",
            "CatalogSchemaSwitch",
            "UseNativeQuery",
            "DisableCustomUseNativeQueryDefault",
            "DatabaseType",
            "SocketTimeOut",
            "SocketTimeout",
            "transportMode",
            "httpPath",
            "KrbAuthType",
            "LowerCaseResultSetColumnName",
            "OptimizedInsert",
            "PreparedMetaLimitZero",
            "RowsFetchedPerBlock",
            "StripCatalogName",
            "SupportTimeOnlyTimestamp",
            "UpperCaseResultSetColName",
            "UseSasl",

            "principal"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "UID", "PWD", "DelegationUID"
    ));

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();
    static {
        Pattern httpPathPattern = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\]/]+$");
        propertyPatterns.put("httpPath".toLowerCase(), httpPathPattern);
    }

    @Override
    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

}
