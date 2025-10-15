package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.FilterResult;
import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.MySQLUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

public class MySQLFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> innerAcceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:mysql:", "jdbc:mariadb:", "jdbc:gbase:", "jdbc:oceanbase:",
            "jdbc:mysql2:", "jdbc:mysql+srv:",
            "jdbc:mysql:loadbalance:", "jdbc:mysql:replication:", "jdbc:mysql:failover:",
            "jdbc:mariadb:loadbalance:", "jdbc:mariadb:replication:", "jdbc:mariadb:failover:",
            "jdbc:oceanbase:loadbalance:", "jdbc:oceanbase:replication:", "jdbc:oceanbase:failover:",
            "jdbc:mysql+srv:loadbalance:", "jdbc:mysql+srv:replication:",
            "mysqlx:", "mysqlx+srv:",
            "jdbc:mariadb:sequential:", "jdbc:mariadb:aurora:"
    ));
    @Getter
    @Setter
    private Set<String> acceptedSchemes = null;

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();

    static {
        propertyPatterns.put("enabledTLSProtocols".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-.,]+$"));
        propertyPatterns.put("servertimezone", Pattern.compile("^[a-zA-Z0-9_\\-.:\\[\\]/+%]+$"));
    }


    @Override
    public boolean acceptURL(String url) {
        if (url == null) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return (lowerUrl.startsWith("jdbc:mysql") ||
                lowerUrl.startsWith("jdbc:mariadb") ||
                lowerUrl.startsWith("jdbc:gbase") ||
                lowerUrl.startsWith("jdbc:oceanbase") ||
                lowerUrl.startsWith("mysqlx")) && !lowerUrl.startsWith("jdbc:mysql:fabric:");
    }


    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "rewriteBatchedStatements",
            "enabledTLSProtocols",
            "maxReconnects",
            "useCompression",
            "maxAllowedPacket",
            "useCursorFetch",
            "useServerPrepStmts",
            "generateSimpleParameterMetadata",
            "connectionCollation",
            "yearIsDateType",
            "unicode",
            "allowPublicKeyRetrieval",
            "transformedBitIsBoolean",
            "useTimezone",
            "useLegacyDatetimeCode",
            "autoReconnect",
            "useLocalTransactionState",
            "allowMultiQueries",
            "useConfigs",
            "useUnicode",
            "initialTimeout",
            "failOverReadOnly",
            "defaultFetchSize",
            "com.mysql.jdbc.faultInjection.serverCharsetIndex",
            "requireSSL",
            "noAccessToProcedureBodies",
            "nullCatalogMeansCurrent",
            "user",
            "useJDBCCompliantTimezoneShift",
            "characterEncoding",
            "useOldAliasMetadataBehavior",
            "autoReconnectForPools",
            "useAffectedRows",
            "tinyInt1isBit",
            "zeroDateTimeBehavior",
            "useLocalSessionState",
            "serverTimezone",
            "useSSL",
            "createDatabaseIfNotExist",
            "com.mysql.cj.testsuite.faultInjection.serverCharsetIndex",
            "characterset",
            "failOverReadOnly",
            "verifyServerCertificate",
            "useInformationSchema",
            "sessionVariables",
            "socketTimeout",
            "connectTimeout",
            "netTimeoutForStreamingResults",
            "sslMode",
            "dontTrackOpenResources",
            "password",
            "cachePrepStmts"
    ));


    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "username",
            "pass", "password",
            "sessionVariables"
    ));

    @Override
    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new MySQLUrlParser(url);
    }

    @Override
    public FilterResult checkAndFilterProperties(UrlParser parser) {
        FilterResult result = super.checkAndFilterProperties(parser);
        Map<String, String> properties = parser.getProperties();

        for (String key : new String[]{"allowLoadLocalInfile", "allowUrlInLocalInfile", "autoDeserialize"}) {
            if ("false".equals(properties.get(key))) {
                continue;
            }
            properties.put(key, "false");
            result.propertyAdd(key, "false");
        }
        if (!parser.getInitialUrl().startsWith("jdbc:gbase:")) {
            if(!"false".equals(properties.get("allowLocalInfile"))) {
                properties.put("allowLocalInfile", "false");
                result.propertyAdd("allowLocalInfile", "false");
            }
        }
        return result;
    }
}
