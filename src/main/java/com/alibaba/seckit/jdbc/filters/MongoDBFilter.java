package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.MongoDBUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MongoDBFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:mongodb:",
            // mongodb+srv 会去取 dns 的 TXT 记录来作为参数，不过只支持 authsource 、 replicaset 两个参数
            "jdbc:mongodb+srv:",
            "mongodb:",
            "mongodb+srv:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "serverSelectionTimeoutMS",
            "localThresholdMS",
            "heartbeatFrequencyMS",
            "replicaSet",
            "ssl",
            "tls",
            "tlsInsecure",
            "sslInvalidHostNameAllowed",
            "tlsAllowInvalidHostnames",
            "connectTimeoutMS",
            "socketTimeoutMS",
            "maxIdleTimeMS",
            "maxLifeTimeMS",
            "streamType",
            "maxPoolSize",
            "waitQueueMultiple",
            "waitQueueTimeoutMS",
            "safe",
            "journal",
            "w",
            "wtimeoutMS",
            "readPreference",
            "readPreferenceTags",
            "maxStalenessSeconds",
            "authMechanism",
            "authSource",
            "authMechanismProperties",
            "gssapiServiceName",
            "appName",
            "compressors",
            "zlibCompressionLevel",
            "retryWrites",
            "retryReads",
            "uuidRepresentation"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "username",
            "pass", "password",
            "authMechanismProperties"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new MongoDBUrlParser(url);
    }

}
