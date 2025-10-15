package com.alibaba.seckit.jdbc.filters;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.UrlParser;
import com.alibaba.seckit.jdbc.parser.DMUrlParser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author mz.zmy
  */
public class DMFilter extends DefaultFilter {

    @Getter
    @Setter
    private Set<String> acceptedSchemes = new HashSet<>(Arrays.asList(
            "jdbc:dm:"
    ));

    @Getter
    @Setter
    private Set<String> acceptedPropertyKeys = new HashSet<>(Arrays.asList(
            "user",
            "password",
            "appName",
            "APP_NAME",
            "osName",
            "OS_NAME",
            "socketTimeout",
            "SOCKET_TIMEOUT",
            "sessionTimeout",
            "SESSION_TIMEOUT",
            "connectTimeout",
            "CONNECT_TIMEOUT",
            "StmtPoolSize",
            "STMT_POOL_SIZE",
            "PStmtPoolSize",
            "PSTMT_POOL_SIZE",
            "pstmtPoolValidTime",
            "PSTMT_POOL_VALID_TIME",
            "escapeProcess",
            "ESCAPE_PROCESS",
            "autoCommit",
            "AUTO_COMMIT",
            "alwaysAllowCommit",
            "ALWAYS_ALLOW_COMMIT",
            "alwayseAllowCommit",
            "ALWAYSE_ALLOW_COMMIT",
            "localTimezone",
            "LOCAL_TIME_ZONE",
            "TIMEZONE",
            "TIME_ZONE",
            "maxRows",
            "MAX_ROWS",
            "bufPrefetch",
            "BUF_PREFETCH",
            "LobMode",
            "LOB_MODE",
            "ignoreCase",
            "IGNORE_CASE",
            "continueBatchOnError",
            "CONTINUE_BATCH_ON_ERROR",
            "batchContinueOnError",
            "BATCH_CONTINUE_ON_ERROR",
            "batchType",
            "BATCH_TYPE",
            "resultSetType",
            "RESULT_SET_TYPE",
            "dbmdChkPrv",
            "DBMD_CHK_PRV",
            "isBdtaRS",
            "IS_BDTA_RS",
            "clobAsString",
            "CLOB_AS_STRING",
            "columnNameCase",
            "COLUMN_NAME_CASE",
            "columnNameUpperCase",
            "COLUMN_NAME_UPPER_CASE",
            "compatible_mode",
            "compatibleMode",
            "COMPATIBLE_MODE",
            "schema",
            "SCHEMA",
            "loginMode",
            "LOGIN_PRIMARY",
            "LOGIN_MODE",
            "loginStatus",
            "LOGIN_STATUS",
            "loginDscCtrl",
            "LOGIN_DSC_CTRL",
            "epSelector",
            "epSelection",
            "EP_SELECTION",
            "EP_SELECTOR",
            "epSelectorDynamic",
            "EP_SELECTOR_DYNAMIC",
            "reconnect",
            "doSwitch",
            "DO_SWITCH",
            "autoReconnect",
            "AUTO_RECONNECT",
            "switchTimes",
            "SWITCH_TIME",
            "SWITCH_TIMES",
            "switchInterval",
            "SWITCH_INTERVAL",
            "cluster",
            "dbAliveCheckFreq",
            "DB_ALIVE_CHECK_FREQ",
            "compress",
            "COMPRESS_MSG",
            "compressID",
            "COMPRESS_ID",
            "mppLocal",
            "MPP_LOCAL",
            "rwSeparate",
            "RW_SEPARATE",
            "rwPercent",
            "RW_PERCENT",
            "rwAutoDistribute",
            "RW_AUTO_DISTRIBUTE",
            "rwHA",
            "RW_HA",
            "rwStandbyRecoverTime",
            "RW_STANDBY_RECOVER_TIME",
            "enRsCache",
            "ENABLE_RS_CACHE",
            "EN_RS_CACHE",
            "rsCacheSize",
            "RS_CACHE_SIZE",
            "rsRefreshFreq",
            "RS_REFRESH_FREQ",
            "keyWords",
            "PRIMARY_KEY",
            "KEY_WORDS",
            "dbAliveCheckTimeout",
            "DB_ALIVE_CHECK_TIMEOUT",
            "checkFreq",
            "CHECK_FREQ",
            "prepareOptimize",
            "PREPARE_OPTIMIZE",
            "allowRange",
            "ALLOW_RANGE",
            "genKeyNameCase",
            "GEN_KEY_NAME_CASE",
            "afterGetMoreResults",
            "AFTER_GET_MORE_RESULTS",
            "checkExecType",
            "CHECK_EXEC_TYPE",
            "quoteReplace",
            "QUOTE_REPLACE",
            "ignoreWarnings",
            "IGNORE_WARNINGS",
            "paramBindMode",
            "PARAM_BIND_MODE",
            "language",
            "LANGUAGE",
            "errMap",
            "ERR_MAP",
            "reconnectErrors",
            "RECONNECT_ERRORS"
    ));

    @Getter
    @Setter
    private Set<String> propertyKeyWhiteList = new HashSet<>(Arrays.asList(
            "user", "username",
            "pass", "password",
            "CustomProperties"
    ));

    @Override
    public UrlParser createUrlParser(String url) throws JdbcURLException {
        return new DMUrlParser(url);
    }

    private static final Map<String, Pattern> propertyPatterns = new HashMap<>();
    static {
        Pattern serviceNamePattern = Pattern.compile("^(\"[a-zA-Z0-9_\\-\\.:\\[\\],]+\"|\\([a-zA-Z0-9_\\-\\.:\\[\\],]+\\)|[a-zA-Z0-9_\\-\\.:\\[\\]]+)$");
        propertyPatterns.put("servicename".toLowerCase(), serviceNamePattern);
    }
    protected Map<String, Pattern> getPropertyPatterns() {
        return propertyPatterns;
    }

    private static final Pattern PROPERTY_VALUE_PATTERN = Pattern.compile("^(\"[a-zA-Z0-9_\\-\\.:\\[\\]]+\"|\\([a-zA-Z0-9_\\-\\.:\\[\\]]+\\)|[a-zA-Z0-9_\\-\\.:\\[\\]]+)$");
    @Override
    protected Pattern getPropertyValuePattern() {
        return PROPERTY_VALUE_PATTERN;
    }
}
