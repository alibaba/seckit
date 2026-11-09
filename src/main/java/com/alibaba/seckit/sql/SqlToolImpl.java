package com.alibaba.seckit.sql;

import com.alibaba.seckit.util.CharMasks;

import java.util.regex.Pattern;

public class SqlToolImpl implements SqlTool {
    private static final int SIZE = 128;
    private static final char CJK_START = '\u4e00';
    // Preserve the established accepted character range, which is narrower than the full CJK block.
    private static final char CJK_END = '\u9fbb';
    private static final CharMasks ESCAPE_MASKS = new CharMasks(SIZE, "\\\0\n\r'\"");
    private static final char BOUND = 0x1a;
    private static final String ESCAPED_BOUND = "\\Z";
    private static final Pattern CONSECUTIVE_HYPHENS = Pattern.compile("-{2,}");

    @Override
    public String mysqlRealEscapeString(String unescapedString) {
        return filterStatically(unescapedString);
    }

    private static final CharMasks FILTER_TRIM_MASKS = createCharMasks(false);
    private static final CharMasks FILTER_TRIM_ORDER_BY_MASKS = createCharMasks(true);

    private static CharMasks createCharMasks(boolean forOrderBy) {
        StringBuilder characters = new StringBuilder();
        for (char c = '0'; c <= '9'; c++) {
            characters.append(c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            characters.append(c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            characters.append(c);
        }
        characters.append("_-.");
        if (forOrderBy) {
            characters.append(" ,");
        }
        return new CharMasks(SIZE, characters.toString());
    }

    @Override
    public String trimSql(String sql) {
        return trimSqlStatically(sql);
    }

    @Override
    public String trimSql4OrderBy(String sql) {
        return trimSqlStatically(sql, true);
    }

    static String trimSqlStatically(String sql) {
        return trimSqlStatically(sql, false);
    }
    static String trimSqlStatically(String sql, boolean forOrderBy) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        CharMasks masks = forOrderBy ? FILTER_TRIM_ORDER_BY_MASKS : FILTER_TRIM_MASKS;

        StringBuilder sb = new StringBuilder();
        int lastPos = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c >= CJK_START && c <= CJK_END) {
                continue;
            }
            if (!masks.contains(c)) {
                sb.append(sql, lastPos, i);
                lastPos = i + 1;
            }
        }

        // Removing disallowed characters can join hyphens that were originally separated.
        String trimmed = sb.append(sql.substring(lastPos)).toString();
        return CONSECUTIVE_HYPHENS.matcher(trimmed).replaceAll("");
    }

    static String filterStatically(String sql) {

        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        StringBuilder sb = new StringBuilder();
        int lastPos = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (ESCAPE_MASKS.contains(c)) {
                sb.append(sql, lastPos, i);
                sb.append('\\').append(c);
                lastPos = i + 1;
            } else if (c == BOUND) {
                sb.append(sql, lastPos, i).append(ESCAPED_BOUND);
                lastPos = i + 1;
            }
        }
        return sb.append(sql.substring(lastPos)).toString();
    }

}
