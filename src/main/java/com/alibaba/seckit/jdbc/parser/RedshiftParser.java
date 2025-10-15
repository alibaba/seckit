package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedshiftParser extends DefaultUrlParser {

    public RedshiftParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final Pattern URL_PATTERN =
            Pattern.compile("(iam:)?//([^:/?]+)(:([^/?]*))?(/([^?;]*))?([?;](.*))?");

    private static final Pattern URL_DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\./%]+$");
    protected static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\],]+$");


    private boolean iamAuth;

    private String port;
    @Override
    protected void parseUrlWithoutScheme(String urlSecondPart) throws JdbcURLException {
        String url = this.getInitialUrl();
        String urlServer = url;
        String urlArgs = "";


        int qPos = url.indexOf('?');
        if (qPos != -1) {
            urlServer = url.substring(0, qPos);
            urlArgs = url.substring(qPos + 1);
        }
        else {
            qPos = url.indexOf(';');
            if (qPos != -1) {
                urlServer = url.substring(0, qPos);
                urlArgs = url.substring(qPos + 1);
            }
        }

        urlServer = urlServer.substring("jdbc:redshift:".length());

        if (urlServer.startsWith("iam:")) {
            String subname = urlServer;
            // Parse the IAM URL
            Matcher matcher = URL_PATTERN.matcher(subname);
            if (!matcher.matches())
            {
                // Host is a required value.
                throw new JdbcURLUnsafeException("illegal redshift url: " + this.getInitialUrl());
            }
            iamAuth = matcher.group(1) != null; // This must be true
            host = matcher.group(2);
            port = matcher.group(4);
            database = matcher.group(6);
            String queryString = matcher.group(8);

            if (database != null && !URL_DATABASE_PATTERN.matcher(database).matches()) {
                throw new JdbcURLUnsafeException("invalid database: " + database + " url: " + this.getInitialUrl());
            }

            if (queryString != null) {
                urlArgs = queryString;
            }
        } else {
            // format: //host/db
            urlServer = urlServer.substring(2);
            int slash = urlServer.indexOf('/');
            if (slash == -1) {
                throw new JdbcURLUnsafeException("illegal redshift url: " + this.getInitialUrl());
            }
            database = urlDecode(urlServer.substring(slash + 1));
            if (!database.isEmpty() && !getDatabasePattern().matcher(database).matches()) {
                throw new JdbcURLUnsafeException("invalid database: " + database + " url: " + this.getInitialUrl());
            }

            // host + port
            host = urlServer.substring(0, slash);
            if (!HOST_PATTERN.matcher(host).matches()) {
                throw new JdbcURLUnsafeException("invalid host: " + host + " url:" + this.getInitialUrl());
            }
            String[] addresses = host.split(",");
            // validate address
            for (String address : addresses) {
                int portIdx = address.lastIndexOf(':');
                if (portIdx != -1 && address.lastIndexOf(']') < portIdx) {
                    String portStr = address.substring(portIdx + 1);
                    try {
                        int port = Integer.parseInt(portStr);
                        if (port < 1 || port > 65535) {
                            throw new JdbcURLUnsafeException("invalid port, url: " + this.getInitialUrl());
                        }
                    } catch (NumberFormatException ignore) {
                        throw new JdbcURLUnsafeException("invalid port, url: " + this.getInitialUrl());
                    }

                }
            }
        }

        /*
         * parse urlArgs
         */
        String[] args = urlArgs.split("[;&?]");
        for (String token : args) {
            if (token.isEmpty()) {
                continue;
            }
            int pos = token.indexOf('=');
            if (pos == -1) {
                properties.put(token, "");
            } else {
                properties.put(token.substring(0, pos), urlDecode(token.substring(pos + 1)));
            }
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
                sb.append(";");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:redshift:");
        if (this.iamAuth) {
            url.append("iam:");
        }
        url.append("//");
        url.append(host);
        if (port != null && !port.isEmpty()) {
            url.append(":").append(port);
        }
        url.append("/");
        if (database != null && !database.isEmpty()) {

            if (this.iamAuth) {
                // might contain character '/'
                url.append(database);
            } else {
                url.append(urlEncode(database));
            }
        }
        if (!this.properties.isEmpty()) {
            url.append(";");
            url.append(propertiesToString());
        }
        return url.toString();
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
