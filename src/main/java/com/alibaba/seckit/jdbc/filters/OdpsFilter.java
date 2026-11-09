package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OdpsFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:odps:http:", "jdbc:odps:https:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "accessId",
            "accessKey",
            "project",
            "executeProject",
            "charset",
            "logview",
            "tunnelEndpoint",
            "tunnelRetryTime",
            "interactiveMode",
            "interactiveServiceName",
            "majorVersion",
            "enableOdpsLogger",
            "tableList",
            "fallbackForUnknownError",
            "fallbackForResourceNotEnough",
            "fallbackForUpgrading",
            "fallbackForRunningTimeout",
            "fallbackForUnsupportedFeature",
            "alwaysFallback",
            "disableFallback",
            "fallbackQuota",
            "attachTimeout",
            "autoSelectLimit",
            "instanceTunnelMaxRecord",
            "instanceTunnelMaxSize",
            "stsToken",
            "disableConnectionSetting",
            "useProjectTimeZone",
            "enableLimit",
            "autoLimitFallback",
            "settings",
            "odpsNamespaceSchema",
            "schema",
            "readTimeout",
            "connectTimeout",
            "enableCommandApi",
            "httpsCheck",
            "logLevel",
            "tunnelReadTimeout",
            "tunnelConnectTimeout",
            "quotaName"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "accessId", "accessKey", "tunnelEndpoint", "stsToken", "logview"
    ));

}
