package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.TeradataUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TeradataFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:teradata:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "ACCOUNT",
            "BROWSER_TAB_TIMEOUT",
            "BROWSER_TIMEOUT",
            "CHARSET",
            "CHATTER",
            "CLIENT_CHARSET",
            "COLUMN_NAME",
            "CONNECT_FAILURE_TTL",
            "CONNECT_FUNCTION",
            "COP",
            "COPLAST",
            "DATABASE",
            "DBS_PORT",
            "DDSTATS",
            "ENCRYPTDATA",
            "ERROR_QUERY_COUNT",
            "ERROR_QUERY_INTERVAL",
            "ERROR_TABLE_1_SUFFIX",
            "ERROR_TABLE_2_SUFFIX",
            "ERROR_TABLE_DATABASE",
            "FIELD_SEP",
            "FLATTEN",
            "FINALIZE_AUTO_CLOSE",
            "GOVERN",
            "HTTPS_PORT",
            "LITERAL_UNDERSCORE",
            "LOB_SUPPORT",
            "LOB_TEMP_TABLE",
            "LOGON_SEQUENCE_NUMBER",
            "MAYBENULL",
            "MAX_MESSAGE_BODY",
            "NEW_PASSWORD",
            "PARTITION",
            "PASSWORD",
            "PREP_SUPPORT",
            "RECONNECT_COUNT",
            "RECONNECT_INTERVAL",
            "REDRIVE",
            "RUNSTARTUP",
            "SESSIONS",
            "SIP_SUPPORT",
            "SLOB_RECEIVE_THRESHOLD",
            "SLOB_TRANSMIT_THRESHOLD",
            "SP_SPL",
            "SSLCIPHER",
            "SSLCRC",
            "SSLMODE",
            "SSLPROTOCOL",
            "STRICT_ENCODE",
            "STRICT_NAMES",
            "TCP",
            "TMODE",
            "TNANO",
            "TRUSTED_SQL",
            "TSNANO",
            "TYPE",
            "USER",
            "USEXVIEWS"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "PASSWORD", "NEW_PASSWORD"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new TeradataUrlParser(url);
    }
}
