package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;


public class SQLServerUrlParser extends SemicolonSeparatedUrlParser {

    public SQLServerUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    protected String instanceName;

    protected void parseUrlWithoutScheme(String urlSecondPart) throws JdbcURLException {
        String tmpUrl = urlSecondPart;
        StringBuilder result = new StringBuilder();
        String name = "";
        String value = "";

        int i;

        // Simple finite state machine.
        // always look at one char at a time
        final int inStart = 0;
        final int inServerName = 1;
        final int inPort = 2;
        final int inInstanceName = 3;
        final int inEscapedValueStart = 4;
        final int inEscapedValueEnd = 5;
        final int inValue = 6;
        final int inName = 7;

        int state = inStart;
        char ch;
        i = 0;
        while (i < tmpUrl.length()) {
            ch = tmpUrl.charAt(i);
            switch (state) {
                case inStart:
                    if (ch == ';') {
                        // done immediately
                        state = inName;
                    } else {
                        result.append(ch);
                        state = inServerName;
                    }
                    break;

                case inServerName:
                    if (ch == ';' || ch == ':' || ch == '\\') {
                        // non escaped trim the string
                        String property = result.toString().trim();

                        if (property.contains("=")) {
                            throw new JdbcURLUnsafeException("invalid url: " + urlSecondPart);
                        }

                        if (property.length() > 0) {
                            this.host = property;
                        }
                        result.setLength(0);

                        if (ch == ';')
                            state = inName;
                        else if (ch == ':')
                            state = inPort;
                        else
                            state = inInstanceName;
                    } else {
                        result.append(ch);
                        // same state
                    }
                    break;

                case inPort:
                    if (ch == ';') {
                        String property = result.toString().trim();
                        try {
                            this.port = Integer.parseInt(property);
                        } catch (NumberFormatException e) {
                            throw new JdbcURLUnsafeException("invalid host in url: " + urlSecondPart);
                        }

                        result.setLength(0);
                        state = inName;
                    } else {
                        result.append(ch);
                        // same state
                    }
                    break;

                case inInstanceName:
                    if (ch == ';' || ch == ':') {
                        // non escaped trim the string
                        String property = result.toString().trim();

                        this.instanceName = property;
                        result.setLength(0);

                        if (ch == ';')
                            state = inName;
                        else
                            state = inPort;
                    } else {
                        result.append(ch);
                        // same state
                    }
                    break;

                case inName:
                    if (ch == '=') {
                        // name is never escaped!
                        name = name.trim();
                        if (name.length() <= 0) {
                            throw new JdbcURLUnsafeException("error connection string: " + urlSecondPart);
                        }
                        state = inValue;
                    } else if (ch == ';') {
                        name = name.trim();
                        if (name.length() > 0) {
                            throw new JdbcURLUnsafeException("error connection string: " + urlSecondPart);
                        }
                        // same state
                    } else {
                        StringBuilder builder = new StringBuilder();
                        builder.append(name);
                        builder.append(ch);
                        name = builder.toString();
                        // same state
                    }
                    break;

                case inValue:
                    if (ch == ';') {
                        // simple value trim
                        value = value.trim();
                        this.properties.put(name, value);
                        name = "";
                        value = "";
                        state = inName;

                    } else if (ch == '{') {
                        state = inEscapedValueStart;
                        value = value.trim();
                        if (value.length() > 0) {
                            throw new JdbcURLUnsafeException("error connection string: " + urlSecondPart);
                        }
                        // add by minggao, to keep value as it is
                        StringBuilder builder = new StringBuilder();
                        builder.append(value);
                        builder.append(ch);
                        value = builder.toString();
                    } else {
                        StringBuilder builder = new StringBuilder();
                        builder.append(value);
                        builder.append(ch);
                        value = builder.toString();
                        // same state
                    }
                    break;

                case inEscapedValueStart:
                    /*
                     * check for escaped }. when we see a }, first check to see if this is before the end of the string
                     * to avoid index out of range exception then check if the character immediately after is also a }.
                     * if it is, then we have a }}, which is not the closing of the escaped state.
                     */
                    if (ch == '}' && i + 1 < tmpUrl.length() && tmpUrl.charAt(i + 1) == '}') {
                        StringBuilder builder = new StringBuilder();
                        builder.append(value);
                        builder.append(ch);
                        // add by minggao, to keep value as it is
                        builder.append(ch);
                        value = builder.toString();
                        i++; // escaped }} into a }, so increment the counter once more
                        // same state
                    } else {
                        if (ch == '}') {
                            // add by minggao, to keep value as it is
                            StringBuilder builder = new StringBuilder();
                            builder.append(value);
                            builder.append(ch);
                            value = builder.toString();

                            // no trimming use the value as it is.
                            this.properties.put(name, value);

                            name = "";
                            value = "";
                            // to eat the spaces until the ; potentially we could do without the state but
                            // it would not be clean
                            state = inEscapedValueEnd;
                        } else {
                            StringBuilder builder = new StringBuilder();
                            builder.append(value);
                            builder.append(ch);
                            value = builder.toString();
                            // same state
                        }
                    }
                    break;

                case inEscapedValueEnd:
                    if (ch == ';') // eat space chars till ; anything else is an error
                    {
                        state = inName;
                    } else if (ch != ' ') {
                        // error if the chars are not space
                        throw new JdbcURLUnsafeException("error connection string: " + urlSecondPart);
                    }
                    break;

                default:
                    assert false : "parseURL: Invalid state " + state;
            }
            i++;
        }

        // Exit
        switch (state) {
            case inServerName:
                String property = result.toString().trim();
                if (property.length() > 0) {
                    this.host = property;
                }
                break;
            case inPort:
                property = result.toString().trim();
                try {
                    this.port = Integer.parseInt(property);
                } catch (NumberFormatException e) {
                    throw new JdbcURLUnsafeException("invalid host in url: " + urlSecondPart);
                }
                break;
            case inInstanceName:
                property = result.toString().trim();
                this.instanceName = property;
                break;
            case inValue:
                // simple value trim
                value = value.trim();
                this.properties.put(name, value);

                break;
            case inEscapedValueEnd:
            case inStart:
                // do nothing!
                break;
            case inName: {
                name = name.trim();
                if (name.length() > 0) {
                    throw new JdbcURLUnsafeException("error connection string: " + urlSecondPart);
                }

                break;
            }
            default:
                throw new JdbcURLUnsafeException("error connection string: " + urlSecondPart);
        }

        // validate hosts、port、instancename
        if (!getHostPattern().matcher(this.host).matches()) {
            throw new JdbcURLUnsafeException("invalid host '" + this.host + "' in url: " + urlSecondPart);
        }

        if (this.instanceName != null && !this.instanceName.isEmpty() &&
                !getHostPattern().matcher(this.instanceName).matches()) {
            throw new JdbcURLUnsafeException("invalid host '" + this.host + "' in url: " + urlSecondPart);
        }

        if (this.port < 0 || this.port > 65535) {
            throw new JdbcURLUnsafeException("invalid port " + this.port + " in url: " + urlSecondPart);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.scheme).append("//").append(this.host);
        if (this.instanceName != null && !this.instanceName.isEmpty()) {
            builder.append('\\').append(this.instanceName);
        }
        if (this.port > 0) {
            builder.append(':').append(this.port);
        }
        if (!this.properties.isEmpty()) {
            builder.append(this.getParamMarker()).append(this.propertiesToString());
        }
        return builder.toString();
    }

}
