package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;
import lombok.Getter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hive2Parser extends DefaultUrlParser {

    private static final String URL_PREFIX = "jdbc:hive2://";

    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\],]+$");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([^;]*)=([^;]*)[;]?");

    @Getter
    private String authority;

    @Getter
    private final Map<String, String> sessionVars = new HashMap<>();
    @Getter
    private final Map<String, String> hiveConfs = new HashMap<>();
    @Getter
    private final Map<String, String> hiveVars = new HashMap<>();

    public Hive2Parser(String url) throws JdbcURLException {
        super(url);
    }

    @Override
    public void parse() throws JdbcURLException {

        if (!initialUrl.startsWith(URL_PREFIX)) {
            throw new JdbcURLUnsafeException("url not starts with jdbc:hive2://");
        }
        parseAuthority();

        URI jdbcURI = URI.create(initialUrl.substring("jdbc:".length()));
        String sessVars = jdbcURI.getPath();
        if (sessVars != null && !sessVars.isEmpty()) {
            parseSessVars(sessVars);
        }

        String confStr = jdbcURI.getQuery();
        if (confStr != null) {
            parseConfStr(confStr);
        }

        String varStr = jdbcURI.getFragment();
        if (varStr != null) {
            parseHiveVars(varStr);
        }

    }

    private void parseAuthority() throws JdbcURLException {
        int fromIndex = URL_PREFIX.length();
        int toIndex = -1;
        ArrayList<String> toIndexChars = new ArrayList<String>(Arrays.asList("/", "?", "#"));
        for (String toIndexChar : toIndexChars) {
            toIndex = initialUrl.indexOf(toIndexChar, fromIndex);
            if (toIndex > 0) {
                break;
            }
        }
        if (toIndex < 0) {
            authority = initialUrl.substring(fromIndex);
        } else {
            authority = initialUrl.substring(fromIndex, toIndex);
        }
        if (authority.isEmpty() || !AUTHORITY_PATTERN.matcher(authority).matches()) {
            throw new JdbcURLUnsafeException();
        }
    }

    private void parseSessVars(String sessVars) throws JdbcURLException {
        String dbName = "";
        // removing leading '/' returned by getPath()
        sessVars = sessVars.substring(1);
        if (!sessVars.contains(";")) {
            // only dbname is provided
            dbName = sessVars;
        } else {
            // we have dbname followed by session parameters
            dbName = sessVars.substring(0, sessVars.indexOf(';'));
            sessVars = sessVars.substring(sessVars.indexOf(';') + 1);
            if (sessVars != null) {
                Matcher sessMatcher = KEY_VALUE_PATTERN.matcher(sessVars);
                while (sessMatcher.find()) {
                    if (sessionVars.put(sessMatcher.group(1),
                            sessMatcher.group(2)) != null) {
                        throw new JdbcURLUnsafeException(
                                "Bad URL format: Multiple values for property " + sessMatcher.group(1));
                    }
                }
            }
        }
        if (!dbName.isEmpty()) {
            database = dbName;
            if (!getDatabasePattern().matcher(database).matches()) {
                throw new JdbcURLUnsafeException();
            }
        }
    }

    private void parseConfStr(String confStr) {
        Matcher confMatcher = KEY_VALUE_PATTERN.matcher(confStr);
        while (confMatcher.find()) {
            hiveConfs.put(confMatcher.group(1), confMatcher.group(2));
        }
    }

    private void parseHiveVars(String varStr) {
        Matcher varMatcher = KEY_VALUE_PATTERN.matcher(varStr);
        while (varMatcher.find()) {
            hiveVars.put(varMatcher.group(1), varMatcher.group(2));
        }
    }

    private String joinMap(Map<String, String> map, String delimiter) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry :
                map.entrySet()) {
            if (!isFirst) {
                sb.append(delimiter);
            }
            isFirst = false;
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(URL_PREFIX).append(authority).append("/");
        if (database != null && !database.isEmpty()) {
            urlBuilder.append(database);
        }
        if (!sessionVars.isEmpty()) {
            urlBuilder.append(";").append(joinMap(sessionVars, ";"));
        }
        if (!hiveConfs.isEmpty()) {
            if (sessionVars.isEmpty()) {
                urlBuilder.append(";");
            }
            urlBuilder.append("?").append(joinMap(hiveConfs, ";"));
        }
        if (!hiveVars.isEmpty()) {
            urlBuilder.append("#").append(joinMap(hiveVars, ";"));
        }
        return urlBuilder.toString();
    }
}
