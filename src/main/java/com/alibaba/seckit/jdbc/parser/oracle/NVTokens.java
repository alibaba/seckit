package com.alibaba.seckit.jdbc.parser.oracle;

import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

/** A character cursor for descriptor syntax; it never interprets comments or mixed atoms. */
final class NVTokens {
    private final String input;
    private int position;

    NVTokens(String input) throws JdbcURLUnsafeException {
        this.input = input;
        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (Character.isISOControl(current) && !isWhitespace(current)) {
                throw error("Invalid control character");
            }
            if (Character.isHighSurrogate(current)) {
                if (index + 1 >= input.length() || !Character.isLowSurrogate(input.charAt(++index))) {
                    throw error("Unpaired surrogate");
                }
            } else if (Character.isLowSurrogate(current)) {
                throw error("Unpaired surrogate");
            }
        }
    }

    void skipWhitespace() {
        while (position < input.length() && isWhitespace(input.charAt(position))) {
            position++;
        }
    }

    boolean at(char expected) {
        return position < input.length() && input.charAt(position) == expected;
    }

    boolean atEnd() {
        return position == input.length();
    }

    void expect(char expected) throws JdbcURLUnsafeException {
        skipWhitespace();
        if (!at(expected)) {
            throw error("Expected '" + expected + "'");
        }
        position++;
    }

    String readName() throws JdbcURLUnsafeException {
        skipWhitespace();
        int start = position;
        while (position < input.length() && isNameCharacter(input.charAt(position))) {
            position++;
        }
        if (position == start) {
            throw error("Expected a descriptor key");
        }
        return input.substring(start, position);
    }

    NVPair readAtom(String name) throws JdbcURLUnsafeException {
        skipWhitespace();
        int start = position;
        if (at('\'') || at('"')) {
            char quote = input.charAt(position++);
            StringBuilder decoded = new StringBuilder();
            while (position < input.length()) {
                char current = input.charAt(position++);
                if (current == quote) {
                    return new NVPair(name, decoded.toString(), input.substring(start, position));
                }
                if (Character.isISOControl(current)) {
                    throw error("Control character in quoted atom");
                }
                if (current == '\\') {
                    if (atEnd()) {
                        throw error("Unfinished quoted escape");
                    }
                    current = input.charAt(position++);
                    if (current != quote && current != '\\') {
                        throw error("Only the quote delimiter and backslash may be escaped");
                    }
                }
                decoded.append(current);
            }
            throw error("Unterminated quoted atom");
        }
        while (position < input.length() && !at(')') && !isWhitespace(input.charAt(position))) {
            char current = input.charAt(position);
            if (current == '(' || current == '=' || current == '\'' || current == '"'
                    || current == '\\' || current == '#' || Character.isWhitespace(current)
                    || Character.isSpaceChar(current)) {
                throw error("Reserved character in unquoted atom");
            }
            position++;
        }
        String atom = input.substring(start, position);
        return new NVPair(name, atom, atom);
    }

    JdbcURLUnsafeException error(String message) {
        return new JdbcURLUnsafeException("Invalid TNS descriptor at offset " + position + ": " + message);
    }

    private static boolean isWhitespace(char value) {
        return value == ' ' || value == '\t' || value == '\r' || value == '\n';
    }

    private static boolean isNameCharacter(char value) {
        return (value >= 'A' && value <= 'Z') || (value >= 'a' && value <= 'z')
                || (value >= '0' && value <= '9') || value == '_' || value == '.' || value == '-';
    }
}
