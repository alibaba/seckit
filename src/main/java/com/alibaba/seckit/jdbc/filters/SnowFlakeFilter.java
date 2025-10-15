package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.SnowFlakeParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * available properties at <a href="https://docs.snowflake.com/en/developer-guide/jdbc/jdbc-parameters">here</a>
 */

public class SnowFlakeFilter extends DefaultFilter {

    @Getter
    private final Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:snowflake:"
    ));

    //白名单参数
    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "user",
            "allowUnderscoresInHost",
            "passcode",
            "passcodeInPassword",
            "password",
            "db",
            "database",
            "role",
            "schema",
            "warehouse",
            "loginTimeout",
            "networkTimeout",
            "queryTimeout",
            "application",
            "maxHttpRetries",
            "net.snowflake.jdbc.max_connections",
            "net.snowflake.jdbc.max_connections_per_route",
            "ocspFailOpen",
            "putGetMaxRetries",
            "stringsQuotedForColumnDef",
            "ACCOUNT"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = true;

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new SnowFlakeParser(url);
    }

}
