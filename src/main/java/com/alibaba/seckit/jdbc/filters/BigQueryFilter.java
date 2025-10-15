package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.FilterResult;
import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.BigQueryParser;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class BigQueryFilter extends DefaultFilter {

    @Getter
    private final Set<String> acceptedSchemes = new HashSet<String>(Arrays.asList(
            "jdbc:bigquery:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "ProjectId", "OAuthType",
            "AdditionalProjects",
            "AllowLargeResults",
            "DefaultDataset",
            "EnableHighThroughputAPI",
            "EnableSession",
            "FilterTablesOnDefaultDataset",
            "HighThroughputActivationRatio",
            "HighThroughputMinTableSize",
            "KMSKeyName",
            "LargeResultDataset",
            "LargeResultsDatasetExpirationTime",
            "LargeResultTable",
            "Location",
            "MaxResults",
            "MetaDataFetchThreadCount",
            "OAuthAccessToken",
            "OAuthClientId",
            "OAuthClientSecret",
            "OAuthPvtKey",
            "OAuthRefreshToken",
            "OAuthServiceAcctEmail",
            "OAuthType",
            "QueryDialect",
            "QueryProperties",
            "RequestGoogleDriveScope",
            "StringColumnLength",
            "Timeout",
            "TimestampFallback",
            "UnsupportedHTAPIFallback",
            "useQueryCache"
    ));

    @Getter
    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();

    static {
        propertyPatterns.put("AdditionalProjects".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-\\.:,]+$"));
        propertyPatterns.put("OAuthServiceAcctEmail".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-\\.:,@]+$"));
        propertyPatterns.put("QueryProperties".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-\\.:,=]+$"));
        propertyPatterns.put("OAuthClientSecret".toLowerCase(), Pattern.compile("^[a-zA-Z0-9_\\-\\.:,/=+]+$"));
    }

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList("OAuthPvtKey"));

    @Override
    public Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    @Override
    public FilterResult checkAndFilterProperties(UrlParser parser) {
        FilterResult result = super.checkAndFilterProperties(parser);
        String pvtKey = parser.getProperties().get("OAuthPvtKey");
        if (pvtKey == null || pvtKey.isEmpty()) {
            return result;
        }
        pvtKey = pvtKey.trim();
        if (pvtKey.contains("../") || pvtKey.startsWith("/") || !pvtKey.startsWith("{")) {
            parser.getProperties().remove("OAuthPvtKey");

            log.info("OAuthPvtKey is removed by jdbc url filter: {}", pvtKey);
            result.propertyDelete("OAuthPvtKey", pvtKey);
        }
        return result;
    }

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new BigQueryParser(url);
    }


}
