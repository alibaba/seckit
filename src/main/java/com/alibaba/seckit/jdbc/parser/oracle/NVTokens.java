package com.alibaba.seckit.jdbc.parser.oracle;

import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.util.Vector;

public final class NVTokens {
    public static final int TKN_NONE = 0;
    public static final int TKN_LPAREN = 1;
    public static final int TKN_RPAREN = 2;
    public static final int TKN_COMMA = 3;
    public static final int TKN_EQUAL = 4;
    public static final int TKN_LITERAL = 8;
    public static final int TKN_EOS = 9;
    private static final char TKN_LPAREN_VALUE = '(';
    private static final char TKN_RPAREN_VALUE = ')';
    private static final char TKN_COMMA_VALUE = ',';
    private static final char TKN_EQUAL_VALUE = '=';
    private static final char TKN_BKSLASH_VALUE = '\\';
    private static final char TKN_DQUOTE_VALUE = '"';
    private static final char TKN_SQUOTE_VALUE = '\'';
    private static final char TKN_EOS_VALUE = '%';
    private static final char TKN_SPC_VALUE = ' ';
    private static final char TKN_TAB_VALUE = '\t';
    private static final char TKN_LF_VALUE = '\n';
    private static final char TKN_CR_VALUE = '\r';
    private Vector _tkType = null;
    private Vector _tkValue = null;
    private int _numTokens = 0;
    private int _tkPos = 0;

    public NVTokens() {
    }

    private static boolean _isWhiteSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static String _trimWhiteSpace(String str) {
        int len = str.length();
        int prefixIndex = 0;

        int suffixIndex;
        for(suffixIndex = len; prefixIndex < len && _isWhiteSpace(str.charAt(prefixIndex)); ++prefixIndex) {
        }

        while(prefixIndex < suffixIndex && _isWhiteSpace(str.charAt(suffixIndex - 1))) {
            --suffixIndex;
        }

        return str.substring(prefixIndex, suffixIndex);
    }

    public boolean parseTokens(String str) {
        this._numTokens = 0;
        this._tkPos = 0;
        this._tkType = new Vector(25, 25);
        this._tkValue = new Vector(25, 25);
        int len = str.length();
        boolean isValue = false;
        char[] bytes = str.toCharArray();
        int i = 0;

        while(true) {
            while(true) {
                do {
                    if (i >= len) {
                        this._addToken(TKN_EOS, '%');
                        return true;
                    }

                    while(i < len && _isWhiteSpace(bytes[i])) {
                        ++i;
                    }
                } while(i >= len);

                switch(bytes[i]) {
                    case '(':
                        isValue = false;
                        this._addToken(TKN_LPAREN, '(');
                        ++i;
                        continue;
                    case ')':
                        isValue = false;
                        this._addToken(TKN_RPAREN, ')');
                        ++i;
                        continue;
                    case ',':
                        isValue = false;
                        this._addToken(TKN_COMMA, ',');
                        ++i;
                        continue;
                    case '=':
                        isValue = true;
                        this._addToken(TKN_EQUAL, '=');
                        ++i;
                        continue;
                }

                int startPos = i;
                int endPos = -1;
                boolean isQuoted = false;
                char quoteMarker = '"';
                if (bytes[i] == '\'' || bytes[i] == '"') {
                    isQuoted = true;
                    quoteMarker = bytes[i];
                    ++i;
                }

                while(i < len) {
                    if (bytes[i] == '\\') {
                        i += 2;
                    } else {
                        if (isQuoted) {
                            if (bytes[i] == quoteMarker) {
                                ++i;
                                endPos = i;
                                break;
                            }
                        } else if (bytes[i] == '(' || bytes[i] == ')' || bytes[i] == ',' && !isValue || bytes[i] == '=' && !isValue) {
                            endPos = i;
                            break;
                        }

                        ++i;
                    }
                }

                if (endPos == -1) {
                    endPos = i;
                }

                this._addToken(TKN_LITERAL, _trimWhiteSpace(str.substring(startPos, endPos)));
            }
        }
    }

    public int getToken() throws JdbcURLUnsafeException {
        if (this._tkType == null) {
            throw new JdbcURLUnsafeException("ParseError-04604");
        } else if (this._tkPos < this._numTokens) {
            return (Integer)this._tkType.elementAt(this._tkPos);
        } else {
            throw new JdbcURLUnsafeException("NoLiterals-04610");
        }
    }

    public int popToken() throws JdbcURLUnsafeException {
        if (this._tkType == null) {
            throw new JdbcURLUnsafeException("ParseError-04604");
        } else if (this._tkPos < this._numTokens) {
            return (Integer)this._tkType.elementAt(this._tkPos++);
        } else {
            throw new JdbcURLUnsafeException("NoLiterals-04610");
        }
    }

    public String getLiteral() throws JdbcURLUnsafeException {
        if (this._tkValue == null) {
            throw new JdbcURLUnsafeException("ParseError-04604");
        } else if (this._tkPos < this._numTokens) {
            return (String)this._tkValue.elementAt(this._tkPos);
        } else {
            throw new JdbcURLUnsafeException("NoLiterals-04610");
        }
    }

    public String popLiteral() throws JdbcURLUnsafeException {
        if (this._tkValue == null) {
            throw new JdbcURLUnsafeException("ParseError-04604");
        } else if (this._tkPos < this._numTokens) {
            return (String)this._tkValue.elementAt(this._tkPos++);
        } else {
            throw new JdbcURLUnsafeException("NoLiterals-04610");
        }
    }

    public void eatToken() {
        if (this._tkPos < this._numTokens) {
            ++this._tkPos;
        }

    }

    public String toString() {
        if (this._tkType == null) {
            return "*NO TOKENS*";
        } else {
            StringBuilder str = new StringBuilder("Tokens");

            for(int var2 = 0; var2 < this._numTokens; ++var2) {
                str.append(" : ").append(this._tkValue.elementAt(var2));
            }

            return str.toString();
        }
    }

    public void println() {
        System.out.println(this.toString());
    }

    private void _addToken(int tokenType, char tokenValue) {
        this._addToken(tokenType, String.valueOf(tokenValue));
    }

    private void _addToken(int tokenType, String tokenValue) {
        this._tkType.addElement(new Integer(tokenType));
        this._tkValue.addElement(tokenValue);
        ++this._numTokens;
    }
}
