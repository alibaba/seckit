package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;

import java.util.Map;

public class MongoDBUrlParser extends UserPassUrlParser {

    public MongoDBUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    @Override
    protected String getParamSeparator() {
        return "&|;";
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
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String authBlock = "";
        if (!this.user.isEmpty()) {
            authBlock = this.user;
            if (!this.password.isEmpty()) {
                authBlock += ":" + this.password;
            }
            authBlock += "@";
        }
        return this.scheme + "//" +
                authBlock +
                this.host +
                (this.port > -1 ? ":" + this.port : "") +
                (this.database != null ? "/" + this.database : "/") +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString());
    }
}
