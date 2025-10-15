package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InformixSqliFilter extends ColonSeparatedUrlFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:informix-sqli:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Collections.singletonList(
            "INFORMIXSERVER"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>();

}
