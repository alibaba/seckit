package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mz.zmy
 * @date 2023/9/21 10:56
 */
public class ArrowFlightSqlFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:arrow-flight-sql:",
            "jdbc:arrow-flight:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "disableCertificateVerification",
            "threadPoolSize",
            "useEncryption",
            "useSystemTrustStore",
            "token",
            "username",
            "password"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "username",
            "password",
            "token"
    ));

}
