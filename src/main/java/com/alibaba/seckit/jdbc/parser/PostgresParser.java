package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author mz.zmy
  */
public class PostgresParser extends DefaultUrlParser {
    private static final Pattern DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.\\p{script=Han}$@]+$");

    public PostgresParser(String url) throws JdbcURLException {
        super(url);
    }

    @Override
    protected Pattern getDatabasePattern() {
        return DATABASE_PATTERN;
    }

    @Override
    protected void parseProperties(String urlParameters) {
        if (urlParameters != null && !urlParameters.isEmpty()) {
            String[] parameters = urlParameters.split(this.getParamSeparator());
            for (String parameter : parameters) {
                int pos = parameter.indexOf('=');
                if (pos == -1) {
                    if (!properties.containsKey(parameter)) {
                        properties.put(parameter.trim(), "");
                    }
                } else {
                    properties.put(parameter.substring(0, pos).trim(), urlDecode(parameter.substring(pos + 1).trim()));
                }
            }
        }
    }

    protected String propertiesToString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(this.getParamSeparator());
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private static String urlDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return encoded;
        }
    }

    private static String urlEncode(String plain) {
        try {
            return URLEncoder.encode(plain, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return plain;
        }
    }

}
