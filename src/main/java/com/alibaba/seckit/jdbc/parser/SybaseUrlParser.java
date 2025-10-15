package com.alibaba.seckit.jdbc.parser;

import com.alibaba.seckit.jdbc.JdbcURLException;
import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

/**
 * see <a href="https://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.help.sqlanywhere.12.0.1/dbprogramming/url-using-jdbc.html">jConnector Documents</a>
 */
public class SybaseUrlParser extends DefaultUrlParser {

    public SybaseUrlParser(String url) throws JdbcURLException {
        super(url);
    }

    @Override
    public void parse() throws JdbcURLException {
        if (!initialUrl.startsWith("jdbc:sybase:Tds:")) {
            throw new JdbcURLUnsafeException("unsafe sybase url: " + initialUrl);
        }
        this.scheme = "jdbc:sybase:Tds:";
        parseUrlWithoutScheme(initialUrl.substring("jdbc:sybase:Tds:".length()));
    }

    @Override
    public String toString() {
        StringBuilder url = new StringBuilder();
        url.append(scheme);
        url.append(host);
        if (port > -1) {
            url.append(":").append(port);
        }
        if (database != null ) {
            url.append("/").append(database);
        }
        if (!properties.isEmpty()) {
            url.append(getParamMarker());
            url.append(propertiesToString());
        }
        return url.toString();
    }
}
