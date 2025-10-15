package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.FilterResult;
import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.Hive2Parser;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class Hive2Filter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:hive2:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            // session vars key
            "retries",
            "sasl.qop",
            "user",
            "password",
            "principal",
            "ssl",
            "hive.server2.transport.mode",
            "transportMode",
            "hive.server2.thrift.http.path",
            "httpPath",
            "serviceDiscoveryMode",
            "zooKeeperNamespace",
            "cookieAuth",
            "cookieName",

            "fetchSize",
            "applicationName",
            "ApplicationName",
            "twoWay",
            "auth",

            "token",
            "compute-group",
            "spark.driver.memory",
            "spark.driver.memoryOverhead",
            "spark.kubernetes.driver.disk.size",
            "spark.executor.cores",
            "spark.executor.memory",
            "spark.executor.memoryOverhead",
            "spark.kubernetes.executor.disk.size",

            "spark.driver.resourceTag",
            "spark.executor.resourceTag",
            "spark.kubernetes.driver.ecsModelPreference",
            "spark.kubernetes.executor.ecsModelPreference",
            "spark.kubernetes.driver.annotation.k8s.aliyun.com/eci-use-specs",
            "spark.kubernetes.executor.annotation.k8s.aliyun.com/eci-use-specs",
            "spark.driver.resource.gpu.vendor",
            "spark.executor.resource.gpu.vendor",
            "spark.driver.resource.gpu.amount",
            "spark.executor.resource.gpu.amount",
            "spark.driver.resource.gpu.discoveryScript",
            "spark.executor.resource.gpu.discoveryScript",
            "spark.kubernetes.executor.annotation.k8s.aliyun.com/eci-use-specs",

            "spark.executor.instances",
            "spark.dynamicAllocation.enabled",
            "spark.dynamicAllocation.minExecutors",
            "spark.dynamicAllocation.maxExecutors",
            "spark.dynamicAllocation.executorIdleTimeout",
            "spark.speculation",
            "spark.task.maxFailures",
            "spark.dfsLog.executor.enabled",
            "spark.default.parallelism",
            "spark.sql.shuffle.partitions"
            ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user",
            "password",
            "principal"
    ));

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();
    static {
        Pattern httpPathPattern = Pattern.compile("^[a-zA-Z0-9_\\-.:\\[\\]/]+$");
        propertyPatterns.put("httpPath".toLowerCase(), httpPathPattern);
        propertyPatterns.put("hive.server2.thrift.http.path".toLowerCase(), httpPathPattern);
    }

    @Override
    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new Hive2Parser(url);
    }


    @Override
    public FilterResult checkAndFilterProperties(UrlParser parser) {
        if (!(parser instanceof Hive2Parser)) {
            throw new RuntimeException("param error, expected Hive2Parser");
        }
        Hive2Parser hive2Parser = (Hive2Parser) parser;

        List<Map<String, String>> properties = Arrays.asList(
                hive2Parser.getSessionVars(),
                hive2Parser.getHiveConfs(),
                hive2Parser.getHiveVars()
        );
        FilterResult result = new FilterResult(hive2Parser.getInitialUrl());
        for (Map<String, String> property: properties) {
            Iterator<Map.Entry<String, String>> iterator = property.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                if (!keyIn(key, getAcceptedPropertyKeys()) && !key.startsWith("http.cookie.")) {
                    iterator.remove();
                    result.propertyDelete(entry.getKey(), entry.getValue());
                    continue;
                }

                if (entry.getValue() != null && !entry.getValue().isEmpty() &&
                        !keyIn(key, getPropertyKeyWhiteList()) ) {
                    Pattern pattern = getPropertyPatterns().get(key.toLowerCase());
                    if (pattern == null) {
                        pattern = getPropertyValuePattern();
                    }
                    if (!pattern.matcher(entry.getValue()).matches()) {
                        iterator.remove();
                        result.propertyDelete(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        if (!result.getDeleted().isEmpty()) {
            log.info("some invalid property are removed by jdbc url filter: {}, original url: {}, after filter: {}",
                    result.getDeleted(), parser.getInitialUrl(), parser.toString());
        }
        return result;
    }
}
