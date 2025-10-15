package com.alibaba.seckit.jdbc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcURLNotSupportedException extends JdbcURLException {
    public JdbcURLNotSupportedException() {}

    public JdbcURLNotSupportedException(String msg) {
        super(msg);
        log.info(msg);
    }
}
