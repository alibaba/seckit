package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;


public class SnowFlakeParser extends DefaultUrlParser {

    public SnowFlakeParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final String URL_PREFIX = "jdbc:snowflake://";

    static  boolean stringIsNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }


    public void parse(String url) throws JdbcURLException {
        this.initialUrl = url;

        String afterPrefix = url.substring(URL_PREFIX.length());
        String host = null;
        int port = -1;
        if (!afterPrefix.startsWith("http://") && !afterPrefix.startsWith("https://")) {
            afterPrefix = url.substring(url.indexOf("snowflake:"));
        }
        URI uri = null;
        try {
            uri = new URI(afterPrefix);
        } catch (URISyntaxException e) {
            throw new JdbcURLUnsafeException("URI parsing error " + afterPrefix);
        }
        String scheme = uri.getScheme();
        String authority = uri.getRawAuthority();
        String[] hostAndPort = authority.split(":");
        if (hostAndPort.length == 2) {
            host = hostAndPort[0];
            try {
                port = Integer.parseInt(hostAndPort[1]);
            } catch (NumberFormatException e) {
                throw new JdbcURLUnsafeException("invalid host: " + authority);
            }
        } else if (hostAndPort.length == 1) {
            host = hostAndPort[0];
        }
        if (stringIsNullOrEmpty(host) || !getHostPattern().matcher(host).matches()) {
            throw new JdbcURLUnsafeException("invalid host: " + authority);
        }

        String queryData = uri.getRawQuery();
        if (!scheme.equals("snowflake") && !scheme.equals("http") && !scheme.equals("https")) {//解析协议
            throw new JdbcURLUnsafeException("Connect strings must have a valid scheme: 'snowflake' or 'http' or 'https'");
        } else {
            String path = uri.getPath();
            if (!stringIsNullOrEmpty(path) && !"/".equals(path)) {
                throw new JdbcURLUnsafeException("Connect strings must have no path: expecting empty or null or '/'");
            }
            if (!stringIsNullOrEmpty(queryData)) {
                String[] params = queryData.split("&");
                for (String p : params) {
                    String[] keyVals = p.split("=");
                    if (keyVals.length != 2) {
                        continue; // ignore invalid pair of parameters.
                    }
                    try {
                        String k = URLDecoder.decode(keyVals[0], "UTF-8");
                        String v = URLDecoder.decode(keyVals[1], "UTF-8");
                        properties.put(k, v);
                    } catch (UnsupportedEncodingException ex0) {
                        throw new JdbcURLUnsafeException("Failed to decode a parameter "+ Arrays.toString(new Object[]{p}) +". Ignored.");
                    }
                }
            }

            this.scheme = scheme;
            this.host = host;
            this.port = port;
        }
    }

    @Override
    protected String propertiesToString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(urlEncode(entry.getKey()));
            sb.append("=");
            sb.append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private static String urlEncode(String plain) {
        try {
            return URLEncoder.encode(plain, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return plain;
        }
    }

    public String toString() {
        return URL_PREFIX +
                (this.scheme.equals("snowflake") ? "" : (this.scheme + "://")) +
                this.host +
                (this.port > -1 ? ":" + this.port : "") +
                "/" +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString());
    }
}
