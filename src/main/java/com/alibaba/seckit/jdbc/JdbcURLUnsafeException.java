package com.alibaba.seckit.jdbc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcURLUnsafeException extends JdbcURLException {
    public JdbcURLUnsafeException() {}

    public JdbcURLUnsafeException(String msg) {
        super(msg);
        log.warn("jdbc attack " + msg);
    }
}
