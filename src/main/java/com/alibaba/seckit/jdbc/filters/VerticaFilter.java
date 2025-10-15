package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.FilterResult;
import com.alibaba.seckit.jdbc.UrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


/**
 * refer to <a href="https://www.vertica.com/docs/11.1.x/HTML/Content/Authoring/ConnectingToVertica/ClientJDBC/JDBCConnectionProperties.htm?tocpath=Connecting%20to%20Vertica%7CClient%20Libraries%7CProgramming%20JDBC%20Client%20Applications%7CCreating%20and%20Configuring%20a%20Connection%7C_____1">Document</a>
 */
public class VerticaFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Collections.singletonList(
            "jdbc:vertica:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "User",
            "Password",
            "SSL",
            "TLSmode",
            "LoginTimeout",
            "LoginNodeTimeout",
            "LoginNetworkTimeout",
            "NetworkTimeout",
            "AutoCommit",
            "MultipleActiveResultSets",
            "ReadOnly"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = true;


    @Override
    public FilterResult checkAndFilterProperties(UrlParser parser) {
        FilterResult result = super.checkAndFilterProperties(parser);
        if (!"true".equals(parser.getProperties().get("DisableCopyLocal"))) {
            parser.getProperties().put("DisableCopyLocal", "true");
            result.propertyAdd("DisableCopyLocal", "true");
        }
        return result;
    }
}
