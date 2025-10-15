package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.SybaseUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * available properties at <a href="https://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.infocenter.dc39001.0707/html/prjdbc0707/prjdbc070714.htm">here</a>
 */
public class SybaseFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = null;

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            // "ALTERNATE_SERVER_NAME",
            "APPLICATIONNAME",
            "BE_AS_JDBC_COMPLIANT_AS_POSSIBLE",
            "CACHE_COLUMN_METADATA",
            "CANCEL_ALL",
            "CAPABILITY_TIME",
            "CAPABILITY_WIDETABLE",
            "CHARSET",
            "CRC",
            "DATABASE",
            "DEFAULT_QUERY_TIMEOUT",
            "DELETE_WARNINGS_FROM_EXCEPTION_CHAIN",
            "DISABLE_UNICHAR_SENDING",
            "DISABLE_UNPROCESSED_PARAM_WARNINGS",
            "DYNAMIC_PREPARE",
            "EARLY_BATCH_READ_THRESHOLD",
            "ENABLE_BULK_LOAD",
            "ENABLE_LOB_LOCATORS",
            "ENABLE_SERVER_PACKETSIZE",
            "ESCAPE_PROCESSING_DEFAULT",
            "EXECUTE_BATCH_PAST_ERRORS",
            "FAKE_METADATA",
            "GET_BY_NAME_USES_COLUMN_LABEL",
            "GET_COLUMN_LABEL_FOR_NAME",
            "HOMOGENEOUS_BATCH",
            "HOSTNAME",
            "HOSTPROC",
            "IGNORE_DONE_IN_PROC",
            "IGNORE_WARNINGS",
            "IMPLICIT_CURSOR_FETCH_SIZE",
            "INTERNAL_QUERY_TIMEOUT",
            "IS_CLOSED_TEST",
            "JAVA_CHARSET_MAPPING",
            "JCONNECT_VERSION",
            "LANGUAGE",
            "LANGUAGE_CURSOR",
            "LITERAL_PARAMS",
            "NEWPASSWORD",
            "OPTIMIZE_FOR_PERFORMANCE",
            "OPTIMIZE_STRING_CONVERSIONS",
            "PACKETSIZE",
            "PASSWORD",
            "PROMPT_FOR_NEWPASSWORD",
            "QUERY_TIMEOUT_CANCELS_ALL",
            "RELEASE_LOCKS_ON_CURSOR_CLOSE",
            "REPEAT_READ",
            "REQUEST_HA_SESSION",
            "REQUEST_KERBEROS_SESSION",
            "RETRY_WITH_NO_ENCRYPTION",
            "SECONDARY_SERVER_HOSTPORT",
            "SELECT_OPENS_CURSOR",
            "SEND_BATCH_IMMEDIATE",
            "SERIALIZE_REQUESTS",
            "SERVER_INITIATED_TRANSACTIONS",
            "SERVICENAME",
            "SERVERTYPE",
            "SERVICE_PRINCIPAL_NAME",
            "SESSION_ID",
            "SESSION_TIMEOUT",
            "SETMAXROWS_AFFECTS_SELECT_ONLY",
            "STREAM_CACHE_SIZE",
            "STRIP_BLANKS",
            "SUPPRESS_CONTROL_TOKEN",
            "SUPPRESS_PARAM_FORMAT",
            "SUPPRESS_ROW_FORMAT",
            "TEXTSIZE",
            "USER"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "USER", "PASSWORD",
            "NEWPASSWORD",
            "SECONDARY_SERVER_HOSTPORT",
            "SERVICE_ PRINCIPAL_NAME"
    ));

    @Getter
    @Setter
    private boolean ignoreCase = true;


    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new SybaseUrlParser(url);
    }

    @Override
    public boolean acceptURL(String url) {
        return url != null && url.startsWith("jdbc:sybase:");
    }

}
