package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;
import lombok.Getter;

public class UserPassUrlParser extends DefaultUrlParser {

    public UserPassUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    @Getter
    protected String user = "";
    @Getter
    protected String password = "";

    @Override
    protected void parseHost(String hostAddressesString) throws JdbcURLException {
        int atIndex = hostAddressesString.lastIndexOf('@');
        String authBlock = "";
        if (atIndex >= 0) {
            authBlock = hostAddressesString.substring(0, atIndex);
            hostAddressesString = hostAddressesString.substring(atIndex + 1);
        }
        setAuth(authBlock);
        super.parseHost(hostAddressesString);
    }

    protected void setAuth(String authBlock) throws JdbcURLException {
        if (authBlock.isEmpty()) {
            return;
        }

        int colonCount = countOccurrences(authBlock, ":");
        if (authBlock.contains("@") || colonCount > 1) {
            throw new JdbcURLUnsafeException("The connection string contains invalid user information: " + this.initialUrl);
        }
        if (colonCount == 0) {
            user = authBlock;
        } else {
            int idx = authBlock.indexOf(":");
            user = authBlock.substring(0, idx);
            password = authBlock.substring(idx + 1);
        }
    }

    private int countOccurrences(final String haystack, final String needle) {
        return haystack.length() - haystack.replace(needle, "").length();
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
                (this.database != null ? "/" + this.database : "") +
                (this.properties.isEmpty() ? "" : this.getParamMarker() + this.propertiesToString());
    }
}
