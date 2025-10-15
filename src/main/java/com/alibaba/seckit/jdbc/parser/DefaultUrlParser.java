package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;
import com.alibaba.seckit.jdbc.UrlParser;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class DefaultUrlParser implements UrlParser {

    @Getter
    protected String initialUrl;

    protected String scheme;

    protected String host;
    protected int port;
    protected String database;
    protected Map<String, String> properties = new HashMap<>();


    protected static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:\\[\\],]+$");
    protected static final Pattern URL_PARAMETER =
            Pattern.compile("(\\/([^\\?]*))?(\\?(.+))*", Pattern.DOTALL);
    private static final Pattern DATABASE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.\\p{script=Han}$]+$");

    private final String PARAM_MARKER = "?";
    private final String PARAM_SEPARATOR = "&";

    private Pattern hostPatternOverride = null;

    public DefaultUrlParser(String url) throws JdbcURLException {
        this.initialUrl = url;
    }

    protected String getParamSeparator() {
        return PARAM_SEPARATOR;
    }

    protected String getParamMarker() {
        return PARAM_MARKER;
    }

    protected Pattern getUrlParameterPattern() {
        return URL_PARAMETER;
    }

    protected Pattern getDatabasePattern() {
        return DATABASE_PATTERN;
    }
    protected Pattern getHostPattern() {
        return hostPatternOverride == null ? HOST_PATTERN : hostPatternOverride;
    }

    public void setHostPattern(Pattern pattern) {
        this.hostPatternOverride = pattern;
    }

    // refer to MariaDB-Connector-J
    public void parse() throws JdbcURLException {
        int separator = this.initialUrl.indexOf("//");
        if (separator == -1) {
            throw new JdbcURLUnsafeException(
                    "url parsing error : '//' is not present in the url " + this.initialUrl);
        }
        this.scheme = this.initialUrl.substring(0, separator);
        String urlSecondPart = this.initialUrl.substring(separator + 2);
        this.parseUrlWithoutScheme(urlSecondPart);
    }

    protected void parseUrlWithoutScheme(String urlSecondPart) throws JdbcURLException {
        int dbIndex = urlSecondPart.indexOf("/");
        int paramIndex = urlSecondPart.indexOf(this.getParamMarker());

        String hostAddressesString;
        String additionalParameters;
        if ((dbIndex < paramIndex && dbIndex < 0) || (dbIndex > paramIndex && paramIndex > -1)) {
            hostAddressesString = urlSecondPart.substring(0, paramIndex);
            additionalParameters = urlSecondPart.substring(paramIndex);
        } else if (dbIndex < paramIndex || dbIndex > paramIndex) {
            hostAddressesString = urlSecondPart.substring(0, dbIndex);
            additionalParameters = urlSecondPart.substring(dbIndex);
        } else {
            hostAddressesString = urlSecondPart;
            additionalParameters = null;
        }

        this.parseHost(hostAddressesString);
        this.parseAdditionalParameter(additionalParameters);
    }

    protected void parseHost(String hostAddressesString) throws JdbcURLException {
        int portIndex = hostAddressesString.lastIndexOf(":");
        int bracketIndex = hostAddressesString.indexOf("]");
        String hostString;
        String portString;
        if ((bracketIndex > -1 && bracketIndex < portIndex) ||
                (portIndex > -1 && hostAddressesString.indexOf(":") == portIndex)) {
            hostString = hostAddressesString.substring(0, portIndex);
            portString = hostAddressesString.substring(portIndex + 1);
        } else {
            hostString = hostAddressesString;
            portString = null;
        }

        if (!getHostPattern().matcher(hostString).matches()) {
            throw new JdbcURLUnsafeException("invalid host '" + hostString + "' in url: " + hostAddressesString);
        }
        this.host = hostString;
        if (portString != null && !portString.isEmpty()) {
            try {
                this.port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new JdbcURLUnsafeException("invalid host " + hostAddressesString  + " in url: " + this.initialUrl);
            }

            if (this.port < 0 || this.port > 65535) {
                throw new JdbcURLUnsafeException("invalid port " + portString + " in url: " + this.initialUrl);
            }
        } else {
            this.port = -1;
        }
    }

    protected void parseAdditionalParameter(String additionalParameters) throws JdbcURLException {
        if (additionalParameters != null) {
            Matcher matcher = this.getUrlParameterPattern().matcher(additionalParameters);
            boolean unused = matcher.find();
            this.database = matcher.group(2);

            this.parseProperties(matcher.group(4));
            if (this.database != null && this.database.isEmpty()) {
                this.database = null;
            }
        } else {
            this.database = null;
        }
        if (this.database != null && !getDatabasePattern().matcher(this.database).matches()) {
            throw new JdbcURLUnsafeException("invalid database in url: " + this.database);
        }
    }

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
                    properties.put(parameter.substring(0, pos).trim(), parameter.substring(pos + 1).trim());
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
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.scheme + "//" +
                this.host +
                (this.port > -1 ? ":" + this.port : "") +
                (this.database != null ? "/" + this.database : "") +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString());
    }

}
