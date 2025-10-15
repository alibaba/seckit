package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.util.regex.Pattern;

public class BigQueryParser extends DefaultUrlParser {

    public BigQueryParser(String url) throws JdbcURLException {
        super(url);
    }

    private static final String PARAM_MARKER = ";";
    private static final String PARAM_SEPARATOR = ";";

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:/]+$");


    @Override
    protected String getParamMarker() {
        return PARAM_MARKER;
    }

    @Override
    protected String getParamSeparator() {
        return PARAM_SEPARATOR;
    }

    @Override
    public Pattern getHostPattern() {
        return HOST_PATTERN;
    }

    @Override
    protected void parseUrlWithoutScheme(String urlSecondPart) throws JdbcURLException {
        int paramIndex = urlSecondPart.indexOf(this.getParamMarker());

        String hostAddressesString, additionalParameters;
        if (paramIndex > 0) {
            hostAddressesString = urlSecondPart.substring(0, paramIndex);
            additionalParameters = urlSecondPart.substring(paramIndex + 1);
        } else if (paramIndex < 0) {
            hostAddressesString = urlSecondPart;
            additionalParameters = null;
        } else {
            throw new JdbcURLUnsafeException("invalid jdbc url: " + this.initialUrl);
        }

        this.parseHost(hostAddressesString);
        this.parseProperties(additionalParameters);
    }



    @Override
    protected void parseHost(String hostAddressesString) throws JdbcURLException {
        int slashIndex = hostAddressesString.indexOf("/");
        int portIndex = 0;
        if (slashIndex >= 0) {
            portIndex = hostAddressesString.indexOf(":", slashIndex);
        } else {
            portIndex = hostAddressesString.indexOf(":");
        }
        String hostString, portString = null;
        if (portIndex == -1) {
            hostString = hostAddressesString;
        } else {
            hostString = hostAddressesString.substring(0, portIndex);
            portString = hostAddressesString.substring(portIndex + 1);
        }
        if (!getHostPattern().matcher(hostString).matches()) {
            throw new JdbcURLUnsafeException("invalid host '" + hostString + "' in url: " + this.initialUrl);
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

    @Override
    public String toString() {
        return this.scheme + "//" +
                this.host +
                (this.port > -1 ? ":" + this.port : "") +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString()+ ";");
    }
}
