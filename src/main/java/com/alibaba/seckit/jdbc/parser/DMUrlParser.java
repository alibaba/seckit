package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.Map;

public class DMUrlParser extends DefaultUrlParser {

    public DMUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    private final static String SERVICE_NAME_KEY = "servicename";

    @Override
    protected void parseHost(String hostAddressesString) throws JdbcURLException {
        super.parseHost(hostAddressesString);
    }
    @Override
    protected String propertiesToString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equals(SERVICE_NAME_KEY)) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }
}
