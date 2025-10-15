package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mz.zmy
 * @date 2023/10/18 11:31
 */
public class TDEngineFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:taos:",
            "jdbc:taos-rs:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
        "user", "password", "charset", "locale", "timezone", "batchfetch", "batchErrorIgnore",
        "useSSL", "messageWaitTimeout", "maxConcurrentRequest", "httpPoolSize", "httpKeepAlive",
        "httpConnectTimeout", "httpSocketTimeout"
    ));


    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "password"
    ));

}
