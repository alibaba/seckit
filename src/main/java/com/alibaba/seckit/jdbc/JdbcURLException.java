package com.alibaba.seckit.jdbc;

import java.net.MalformedURLException;

public class JdbcURLException extends MalformedURLException {
    public JdbcURLException() {}

    public JdbcURLException(String msg) {
        super(msg);
    }
}
