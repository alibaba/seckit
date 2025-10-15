package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class As400Filter extends SemicolonSeparatedUrlFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:as400:"
    ));
    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "naming",
            "errors",
            "date format",
            "data compression",
            "block size",
            "data truncation",
            "decimal data errors"
    ));

}
