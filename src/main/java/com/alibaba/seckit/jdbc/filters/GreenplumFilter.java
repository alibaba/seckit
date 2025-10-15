package com.alibaba.seckit.jdbc.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <a href="https://docs.progress.com/zh-CN/bundle/datadirect-pivotal-greenplum-jdbc-60/page/Connection-property-descriptions.html">parameters reference </a>
 */
public class GreenplumFilter extends SemicolonSeparatedUrlFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:pivotal:greenplum:",
            "jdbc:pivotal:greenplum:http:",
            "jdbc:pivotal:greenplum:https:"
    ));
    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "User",
            "Password",
            "AuthenticationMethod",
            "DatabaseName",
            "ServicePrincipalName",
            "CryptoProtocolVersion",
            "EncryptionMethod",
            "HostNameInCertificate",
            "ValidateServerCertificate",
            "BulkLoadBatchSize",
            "ConnectionRetryCount",
            "ConnectionRetryDelay",
            "DatabaseName",
            "LoadBalancing",
            "EnableCancelTimeout",
            "LoginTimeout",
            "QueryTimeout",
            "CatalogOptions",
            "ConvertNull",
            "Database",
            "ExtendedColumnMetadata",
            "InsensitiveResultSetBufferSize",
            "JavaDoubleToString",
            "MaxLongVarcharSize",
            "MaxNumericPrecision",
            "MaxNumericScale",
            "MaxStatements",
            "MaxVarcharSize",
            "PrepareThreshold",
            "ResultSetMetaDataOptions",
            "SupportsCatalogs",
            "TransactionErrorBehavior",
            "VarcharClobThreshold"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "password",
            "ServicePrincipalName"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = true;

}
