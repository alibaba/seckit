package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;


public class SnowFlakeParser extends DefaultUrlParser {

    public SnowFlakeParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final String URL_PREFIX = "jdbc:snowflake://";

    static  boolean stringIsNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }


    @Override
    public void parse() throws JdbcURLException {
        if (initialUrl == null || !initialUrl.regionMatches(true, 0, URL_PREFIX, 0, URL_PREFIX.length())) {
            throw new JdbcURLUnsafeException("Connect strings must start with " + URL_PREFIX);
        }

        String afterPrefix = initialUrl.substring(URL_PREFIX.length());
        if (!afterPrefix.startsWith("http://") && !afterPrefix.startsWith("https://")) {
            afterPrefix = "snowflake://" + afterPrefix;
        }
        URI uri;
        try {
            uri = new URI(afterPrefix);
        } catch (URISyntaxException e) {
            throw new JdbcURLUnsafeException("URI parsing error " + afterPrefix);
        }
        String scheme = uri.getScheme();
        if (!"snowflake".equals(scheme) && !"http".equals(scheme) && !"https".equals(scheme)) {
            throw new JdbcURLUnsafeException("Connect strings must have a valid scheme: 'snowflake' or 'http' or 'https'");
        }
        String authority = uri.getRawAuthority();
        if (stringIsNullOrEmpty(authority)) {
            throw new JdbcURLUnsafeException("invalid host: " + authority);
        }
        this.parseHost(authority);

        String path = uri.getPath();
        if (!stringIsNullOrEmpty(path) && !"/".equals(path)) {
            throw new JdbcURLUnsafeException("Connect strings must have no path: expecting empty or null or '/'");
        }
        String queryData = uri.getRawQuery();
        if (!stringIsNullOrEmpty(queryData)) {
            String[] params = queryData.split("&");
            for (String p : params) {
                int separator = p.indexOf('=');
                if (separator < 0) {
                    continue; // ignore invalid pair of parameters.
                }
                try {
                    String k = URLDecoder.decode(p.substring(0, separator), "UTF-8");
                    String v = URLDecoder.decode(p.substring(separator + 1), "UTF-8");
                    properties.put(k, v);
                } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                    throw new JdbcURLUnsafeException("Failed to decode a parameter " + p);
                }
            }
        }
        this.scheme = scheme;
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

    @Override
    public String toString() {
        return URL_PREFIX +
                (this.scheme.equals("snowflake") ? "" : (this.scheme + "://")) +
                this.host +
                (this.port > -1 ? ":" + this.port : "") +
                "/" +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString());
    }
}
