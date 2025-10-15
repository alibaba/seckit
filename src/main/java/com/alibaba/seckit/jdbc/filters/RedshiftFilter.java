package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.RedshiftParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RedshiftFilter extends DefaultFilter {
    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:redshift:", "jdbc:redshift:iam:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "AccessKeyID",
            "AllowDBUserOverride",
            "App_ID",
            "App_Name",
            "ApplicationName",
            "AutoCreate",
            "Client_ID",
            "Client_Secret",
            "ClusterID",
            "connectTimeout",
            "databaseMetadataCurrentDbOnly",
            "DbUser",
            "DbGroups",
            "defaultRowFetchSize",
            "DisableIsValidQuery",
            "enableFetchReadAndProcessBuffers",
            "enableFetchRingBuffer",
            "enableMultiSqlSupport",
            "fetchRingBufferSize",
            "ForceLowercase",
            "groupFederation",
            "IAMDisableCache",
            "IAMDuration",
            "IdP_Host",
            "IdP_Port",
            "IdP_Tenant",
            "IdP_Response_Timeout",
            "isServerless",
            "OverrideSchemaPatternType",
            "Partner_SPID",
            "Password",
            "Preferred_Role",
            "queryGroup",
            "readOnly",
            "Region",
            "reWriteBatchedInserts",
            "reWriteBatchedInsertsSize",
            "roleArn",
            "roleSessionName",
            "scope",
            "SecretAccessKey",
            "SessionToken",
            "serverlessAcctId",
            "serverlessWorkGroup",
            "socketTimeout",
            "SSL",
            "SSL_Insecure",
            "SSLMode",
            "StsEndpointUrl",
            "tcpKeepAlive",
            "UID",
            "User",
            "webIdentityToken"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "App_ID",
            "App_Name",
            "ApplicationName",
            "Client_ID",
            "Client_Secret",
            "ClusterID",
            "DbUser",
            "DbGroups",
            "IdP_Host",
            "IdP_Port",
            "IdP_Tenant",
            "Password",
            "Preferred_Role",
            "queryGroup",
            "Region",
            "roleArn",
            "roleSessionName",
            "scope",
            "SecretAccessKey",
            "SessionToken",
            "scope",
            "SecretAccessKey",
            "SessionToken",
            "StsEndpointUrl",
            "UID",
            "User"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new RedshiftParser(url);
    }

    @Override
    public boolean isIgnoreCase() {
        return true;
    }
}
