package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HiveFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:hive:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>();

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>();

}
