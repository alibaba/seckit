package com.alibaba.seckit.sql;

public interface SqlTool {
    String mysqlRealEscapeString(String unescapedString);
    String trimSql(String sql);
    String trimSql4OrderBy(String sql);
}
