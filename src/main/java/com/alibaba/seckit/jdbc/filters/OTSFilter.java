package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mz.zmy
 * @date 2023/11/3 10:12
 */
public class OTSFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:ots:http:", "jdbc:ots:https:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "enableRequestCompression",
            "enableResponseCompression",
            "ioThreadCount",
            "maxConnections",
            "socketTimeoutInMillisecond",
            "connectionTimeoutInMillisecond",
            "retryThreadCount",
            "syncClientWaitFutureTimeoutInMillis",
            "connectionRequestTimeoutInMillisecond",
            "user",
            "password"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "password"
    ));

}
